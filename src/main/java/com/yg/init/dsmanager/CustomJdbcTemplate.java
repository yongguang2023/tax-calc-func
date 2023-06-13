package com.yg.init.dsmanager;

import com.alibaba.fastjson.JSON;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.sun.rowset.CachedRowSetImpl;
import com.yg.cache.SqlExecutionCache;
import com.yg.exception.BizRuntimeException;
import com.yg.init.fmmanager.AviatorCacheManager;
import com.yg.init.fmmanager.ThreadLocalHolder;
import com.yg.util.TransmittableThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Slf4j
public class CustomJdbcTemplate extends JdbcTemplate {

  private Integer dataSourceId;

  private static final Map<String, ParsedSql> PARSED_SQL_CACHE = new ParsedSqlCache(512);

  public CustomJdbcTemplate(Integer dataSourceId) {
    super();
    this.dataSourceId = dataSourceId;
  }

  /**
   * 自定义获取数据源
   *
   * @return
   */
  @Override
  public DataSource getDataSource() {
    ShardingDataSource pools = DataSourcePool.getInstance().getSubPool(dataSourceId);
    if (pools == null || MapUtils.isEmpty(pools.getDataSources())) {
      throw new BizRuntimeException("this source id do not have any data source;data source: "
          + dataSourceId);
    }
    Map<Integer, DataSource> pool = pools.getDataSources();
    int index = pool.size() == 1 ? 0 : this.getIndex(pools.getIndexExp()).intValue();
    final DataSource dataSource = pool.get(index);
    if (dataSource == null) {
      throw new BizRuntimeException("this source id do not have the sub data source;data source: "
          + dataSourceId + "sub index: " + index);
    }
    return dataSource;
  }


  @Override
  public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, SqlRowSet.class),
        super::queryForRowSet, Type.QUERY_FOR_ROWSET);
  }

  @Override
  public SqlRowSet queryForRowSet(String sql, @Nullable Object... args)
      throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, args, SqlRowSet.class),
        s -> super.queryForRowSet(s, args), Type.QUERY_FOR_ROWSET);
  }

  @Override
  public Map<String, Object> queryForMap(String sql) throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, Map.class),
        super::queryForMap, Type.QUERY_FOR_MAP);
  }

  @Override
  public Map<String, Object> queryForMap(String sql, @Nullable Object... args)
      throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, args, Map.class),
        s -> super.queryForMap(s, args), Type.QUERY_FOR_MAP);
  }

  @Override
  public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, List.class),
        super::queryForList, Type.QUERY_FOR_LIST);
  }

  @Override
  public List<Map<String, Object>> queryForList(String sql, @Nullable Object... args)
      throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, args, List.class),
        s -> super.queryForList(s, args), Type.QUERY_FOR_LIST);
  }

  @Override
  public <T> T queryForObject(String sql, Class<T> type) throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, type),
        s -> super.queryForObject(s, type), Type.QUERY_FOR_OBJECT);
  }

  @Override
  public <T> T queryForObject(String sql, Object[] args, Class<T> type)
      throws DataAccessException {
    return this.execute(sql, s -> this.getKey(s, args, type),
        s -> super.queryForObject(s, args, type), Type.QUERY_FOR_OBJECT);
  }

  public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) {
    final RowMapper<Map<String, Object>> mapper = this.getColumnMapRowMapper();
    final SqlParameterSource source = new MapSqlParameterSource(paramMap);
    return this.execute(sql, s -> this.getKey(s, paramMap, Map.class),
            s -> DataAccessUtils.nullableSingleResult(super.query(this.getPreparedStatementCreator(s, source), mapper)),
            Type.QUERY_FOR_MAP);
  }

  /**
   * 扩展named Query方法
   *
   * @param sql
   * @param paramMap
   * @return
   * @throws DataAccessException
   */
  public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap)
      throws DataAccessException {
    final RowMapper<Map<String, Object>> mapper = this.getColumnMapRowMapper();
    final SqlParameterSource source = new MapSqlParameterSource(paramMap);
    return this.execute(sql, s -> this.getKey(s, paramMap, List.class),
        s -> super.query(this.getPreparedStatementCreator(s, source), mapper), Type.QUERY_FOR_LIST);
  }

  /**
   * 扩展named Query方法
   *
   * @param sql
   * @param paramMap
   * @return
   * @throws DataAccessException
   */
  public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType)
          throws DataAccessException {
    RowMapper<T> mapper = this.getSingleColumnRowMapper(elementType);
    final SqlParameterSource source = new MapSqlParameterSource(paramMap);
    return this.execute(sql, s -> this.getKey(s, paramMap, elementType),
            s -> super.query(this.getPreparedStatementCreator(s, source), mapper), Type.QUERY_FOR_LIST);
  }

  /**
   * 执行sql并缓存
   *
   * @param sql
   * @param create
   * @param action
   * @param <T>
   * @return
   * @throws DataAccessException
   */
  private <T> T execute(String sql, KeyAction create, ValueAction<T> action, Type type) {
    try {
      String sub = this.subTable(sql);
      String gid = ThreadLocalHolder.GID.get();
      //如果gid不为空且开启缓存
      boolean cache = StringUtils.isNotBlank(gid);
      //对结果进行复制，避免线程安全问题
      return cache ? this.runWithCache(sub, create, action, type) : action.action(sub);
    } catch (ExecutionException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof DataAccessException) {
        throw (DataAccessException) cause;
      } else {
        throw new BizRuntimeException("execute sql with cache error", cause);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BizRuntimeException("execute sql with cache error", e);
    } catch (Exception e) {
      log.error("CustomJdbcTemplate excute sql error,msg : {}", e.getMessage());
      throw e;
    }
  }

  /**
   * 根据分表逻辑替换表名
   *
   * @param sql
   * @return
   */
  private String subTable(String sql) {
    ShardingDataSource pools = DataSourcePool.getInstance().getSubPool(dataSourceId);
    Set<String> tables = pools.getTables();
    if (CollectionUtils.isEmpty(tables)) {
      return sql;
    }
    long index = this.getIndex(pools.getTableExp());
    for (String table : tables) {
      String f = " " + table + " ";
      if (sql.contains(f)) {
        String t = " " + table + "_" + index + " ";
        sql = sql.replace(f, t);
      }
    }
    return sql;
  }

  /**
   * 根据配置的下标表达式计算分库的下标
   *
   * @return
   */
  private Long getIndex(String expression) {
    try {
      if (StringUtils.isBlank(expression)) {
        throw new BizRuntimeException("分库/分表逻辑表达式不能为空");
      }
      Map<String, Object> params = TransmittableThreadLocalUtil.paramsLocal.get();
      AviatorEvaluatorInstance instance = AviatorCacheManager.getInstance().getAviatorInstance();
      Expression ex = instance.compile(expression, false);
      return (Long) ex.execute(params);
    } catch (Exception e) {
      log.error("执行分库表达式出错 : {} >> ", dataSourceId, e);
      throw new BizRuntimeException("执行分库表达式出错 >> " + expression, e);
    }
  }

  /**
   * @param sub
   * @param create
   * @param action
   * @param <T>
   * @return
   * @throws Exception
   */
  private <T> T runWithCache(String sub, KeyAction create, ValueAction<T> action, Type type)
      throws ExecutionException, InterruptedException {
    String key = create.create(sub);
    AtomicBoolean run = new AtomicBoolean(false);
    FutureTask<Object> task = this.getCache(key, v -> {
      run.set(true);
      return new FutureTask<>(() -> action.action(sub));
    });
    if (task == null) {
      return action.action(sub);
    }
    if (run.get()) {
      task.run();
    }
    return this.copyResult((T) task.get(), type);
  }

  /**
   * 获取缓存值
   *
   * @param key
   * @param function
   * @return
   */
  private FutureTask<Object> getCache(String key, Function<String, FutureTask<Object>> function) {
    try {
      String gid = ThreadLocalHolder.GID.get();
      return SqlExecutionCache.getInstance().getCache(gid, key, function);
    } catch (Exception e) {
      log.error("get cache error,msg : {}", e.getMessage());
    }
    return null;
  }

  /**
   * 对查询结果进行复制，避免线程安全问题
   *
   * @param object
   * @param <T>
   * @return
   */
  private <T> T copyResult(T object, Type type) {
    try {
      if (object != null) {
        //复制查询结果，避免线程安全问题
        if (Type.QUERY_FOR_ROWSET.equals(type)) {
          ResultSet set = ((ResultSetWrappingSqlRowSet) object).getResultSet();
          if (set instanceof CachedRowSetImpl) {
            CachedRowSet rowSet = ((CachedRowSetImpl) set).createCopy();
            //这种结果对象特殊，特殊处理
            return (T) new ResultSetWrappingSqlRowSet(rowSet);
          }
        }
        //list结果集直接转化成不可操作对象，防止用户操作缓存对象
        if (Type.QUERY_FOR_LIST.equals(type)) {
          final List<Map<String, Object>> list = (List<Map<String, Object>>) object;
          final List<Map<String, Object>> result = new ArrayList<>(list.size());
          list.forEach(v -> result.add(this.ignoreCaseMap(v)));
          return (T) result;
        }
        //map直接重新new一个新的对象
        if (Type.QUERY_FOR_MAP.equals(type)) {
          final Map<String, Object> map = (Map<String, Object>) object;
          return (T) this.ignoreCaseMap(map);
        }
        //其他小对象直接复制一个
        return this.copyByStream(object);
      }
    } catch (Exception e) {
      log.error("result copy error,msg : {}", e.getMessage());
    }
    return object;
  }

  /**
   * 用对象流复制对象
   *
   * @param obj
   * @param <T>
   * @return
   */
  private <T> T copyByStream(T obj) {
    try (ByteArrayOutputStream outb = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outb)) {
      out.writeObject(obj);
      try (ByteArrayInputStream inb = new ByteArrayInputStream(outb.toByteArray());
          ObjectInputStream in = new ObjectInputStream(inb)) {
        return (T) in.readObject();
      }
    } catch (Exception e) {
      log.error("stream copy error,msg : {}", e.getMessage());
    }
    return obj;
  }

  /**
   * 根据参数获取唯一值
   *
   * @param params
   * @return
   */
  private String getKey(Object... params) {
    try {
      String str = JSON.toJSONString(params);
      return DigestUtils.md5DigestAsHex(str.getBytes());
    } catch (Exception e) {
      log.error("create key error:{}", e.getMessage());
    }
    return null;
  }

  /**
   * 复制一个忽略大小写的map
   *
   * @param map
   * @return
   */
  private Map<String, Object> ignoreCaseMap(Map<String, Object> map) {
    if (map instanceof LinkedCaseInsensitiveMap) {
      return ((LinkedCaseInsensitiveMap) map).clone();
    } else {
      return new HashMap<>(map);
    }
  }

  /**
   * @param sql
   * @param paramSource
   * @return
   */
  private PreparedStatementCreator getPreparedStatementCreator(String sql,
      SqlParameterSource paramSource) {
    ParsedSql parsedSql = this.getParsedSql(sql);
    String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
    List<SqlParameter> declaredParameters = NamedParameterUtils
        .buildSqlParameterList(parsedSql, paramSource);
    PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse,
        declaredParameters);
    Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
    return pscf.newPreparedStatementCreator(params);
  }

  /**
   * 获取带LUR淘汰策略的SQL缓存
   *
   * @param sql
   * @return
   */
  private ParsedSql getParsedSql(String sql) {
    synchronized (this.PARSED_SQL_CACHE) {
      ParsedSql parsedSql = this.PARSED_SQL_CACHE.get(sql);
      if (parsedSql == null) {
        parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        this.PARSED_SQL_CACHE.put(sql, parsedSql);
      }
      return parsedSql;
    }
  }


  /**
   * 查询类型
   */
  private enum Type {
    QUERY_FOR_ROWSET,
    QUERY_FOR_MAP,
    QUERY_FOR_LIST,
    QUERY_FOR_OBJECT
  }


  /**
   * 自定义回调函数，主要执行被重写方法的原方法
   */
  @FunctionalInterface
  private interface KeyAction {

    /**
     * 执行具体方法
     *
     * @return
     */
    String create(String sql);
  }


  /**
   * 自定义回调函数，主要执行被重写方法的原方法
   *
   * @param <T>
   */
  @FunctionalInterface
  private interface ValueAction<T> {

    /**
     * 执行具体方法
     *
     * @return
     */
    T action(String sql);
  }

  /**
   * 自定义mapper转化规则
   */
  private static class CustomRowMapper implements RowMapper<Map<String, Object>> {

    /**
     * 用于缓存列名
     */
    private static final Map<Integer, String> COLUMN = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnCount = rsmd.getColumnCount();
      CustomMap<String, Object> mapOfColumnValues = new CustomMap<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        final String key = this.getColumnKey(i, rsmd);
        final Object value = JdbcUtils.getResultSetValue(rs, i);
        mapOfColumnValues.putByLowerCase(key, value);
      }
      return mapOfColumnValues;
    }

    private String getColumnKey(int i, ResultSetMetaData rsmd) {
      return COLUMN.computeIfAbsent(i, v -> {
        try {
          final String column = JdbcUtils.lookupColumnName(rsmd, i);
          return StringUtils.isNotBlank(column) ? column.toLowerCase() : column;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
