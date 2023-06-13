package com.yg.init.dsmanager;

import com.yg.entity.Datasource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


@Data
public class ShardingDataSource {

  private String indexExp;

  private String tableExp;

  private Set<String> tables;

  private Map<Integer, javax.sql.DataSource> dataSources;

  public ShardingDataSource(){}

  public ShardingDataSource(Datasource source){
    this.indexExp = "0";
    this.tableExp = source.getTableExp();
    if(StringUtils.isNotBlank(source.getTables())){
      String[] arr = source.getTables().split(",");
      this.tables = new HashSet<>(Arrays.asList(arr));
    }
    this.dataSources = new HashMap<>();
  }
}
