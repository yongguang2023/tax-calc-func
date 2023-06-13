package com.yg.init.dsmanager;

import org.springframework.jdbc.core.namedparam.ParsedSql;

import java.util.LinkedHashMap;
import java.util.Map;


public class ParsedSqlCache extends LinkedHashMap<String, ParsedSql> {

  private int limit = 256;

  public ParsedSqlCache() {
  }

  public ParsedSqlCache(int limit) {
    this.limit = limit;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, ParsedSql> eldest) {
    return this.size() > limit;
  }
}
