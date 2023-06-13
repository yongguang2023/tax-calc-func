package com.yg.init.dsmanager;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.yg.entity.Datasource;
import com.yg.exception.BizRuntimeException;
import com.yg.service.DatasourceService;
import com.yg.util.DesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.druid.pool.DruidDataSourceFactory.*;


@Slf4j
@Service
public class DataSourceManagerService {

  @Resource
  private DatasourceService dataSourceService;

  @Value("${data.source.query.timeout:120}")
  private int timeOut;

  public void initDataSource() {
    log.info(
        "***************************************DataSourceManagerServiceImpl initDataSource start***************************************");
    List<Datasource> datasourceList = dataSourceService.list();
    this.putSource2Pool(datasourceList);
    log.info(
        "***************************************DataSourceManagerServiceImpl initDataSource success***************************************");
  }


  /**
   * 清除指定的数据库连接池
   *
   * @param dataSourceId
   */
  public void deletePool(Integer dataSourceId) {
    DataSourcePool.getInstance().delete(dataSourceId);
    JdbcTemplatePool.delete(dataSourceId);
  }


  /**
   * 构建将数据源放入资源池
   *
   * @param sources
   */
  public void putSource2Pool(List<Datasource> sources) {
    if (CollectionUtils.isEmpty(sources)) {
      log.warn("FormulaInit initGlobalDataset warn. can't load any dataset ...");
      return;
    }

    Map<Integer, JdbcTemplate> templates = new HashMap<>();
    Map<Integer, ShardingDataSource> pools = new HashMap<>();
    sources.forEach(s -> {
      Integer datasourceId = s.getId();
      templates.computeIfAbsent(datasourceId, v -> new CustomJdbcTemplate(datasourceId));
      ShardingDataSource sds = pools.computeIfAbsent(datasourceId, v -> new ShardingDataSource(s));
      sds.getDataSources().computeIfAbsent(0, v1 -> this.createDataSource(s));
    });

    //初始化数据库连接池
    DataSourcePool.getInstance().initPool(pools);
    //初始化jdbc连接池
    JdbcTemplatePool.initPool(templates);
    log.info("FormulaInit initGlobalDataset success！");
  }

  /**
   * 根据参数构建数据源
   *
   * @param dataSource
   * @return
   */
  private javax.sql.DataSource createDataSource(Datasource dataSource) {
    try {
      Map<String, Object> map = new HashMap<>();
      map.put(PROP_URL, dataSource.getUrl());
      map.put(PROP_USERNAME, dataSource.getUsername());
      // 对密码进行解密
      map.put(PROP_PASSWORD, dataSource.getPassword());
      // 最大活动连接数
      map.put(PROP_MAXACTIVE, "200");
      // 最大等待时间（毫秒）
      map.put(PROP_MAXWAIT, "10000");
      // 检查空闲连接并释放的时间间隔
      map.put(PROP_TIMEBETWEENEVICTIONRUNSMILLIS, "30000");
      // 检查连接是否可用
      map.put(PROP_TESTWHILEIDLE, "true");
      // 是否每次取出之前都测试链接
      map.put(PROP_TESTONBORROW, "false");
      map.put("type", "mysql");
      map.put("driver-class-name", Class.forName("com.mysql.jdbc.Driver"));

      // modify by neo for connect time out begin
      javax.sql.DataSource ds = DruidDataSourceFactory.createDataSource(map);
      if (ds instanceof DruidDataSource) {
        DruidDataSource druidDataSource = (DruidDataSource) ds;
        druidDataSource.setConnectionErrorRetryAttempts(15);
        //druidDataSource.setBreakAfterAcquireFailure(true);
        druidDataSource.setQueryTimeout(timeOut);
        // 慢sql统计
      /*  List<Filter> filters = new ArrayList<>();
        SlowSqlFilter statFilter = new SlowSqlFilter();
        statFilter.setLogSlowSql(true);
        statFilter.setSlowSqlMillis(200);
        filters.add(statFilter);
        druidDataSource.setProxyFilters(filters);*/
      }
      return ds;
    } catch (Exception e) {
      log.error("create datasource error,params : {},msg : {}", dataSource, e);
      throw new BizRuntimeException("create datasource error,msg : {}", e.getMessage());
    }
  }

}
