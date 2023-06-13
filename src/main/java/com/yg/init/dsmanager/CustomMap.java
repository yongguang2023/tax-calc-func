package com.yg.init.dsmanager;

import java.util.HashMap;
import java.util.Map;


public class CustomMap<K extends String, V> extends HashMap<String, V> {

  public CustomMap(int size) {
    super(size);
  }

  public CustomMap(Map<? extends K, ? extends V> m) {
    super(m);
  }

  public V putByLowerCase(String key, V value) {
    return super.put(key, value);
  }

  @Override
  public V put(String key, V value) {
    return super.put(key.toLowerCase(), value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends V> m) {
    m.forEach(this::put);
  }

  @Override
  public V get(Object key) {
    V v;
    return (v = super.get(key)) == null && (key instanceof String) ?
        super.get(((String) key).toLowerCase()) : v;
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    V v;
    return (v = this.get(key)) == null ? defaultValue : v;
  }

  @Override
  public boolean containsKey(Object key) {
    return super.containsKey(key) ||
        ((key instanceof String) && super.containsKey(((String) key).toLowerCase()));
  }

  @Override
  public V remove(Object key) {
    V v;
    return (v = super.remove(key)) == null && (key instanceof String) ?
        super.remove(((String) key).toLowerCase()) : v;
  }

  @Override
  public boolean remove(Object key, Object value) {
    return super.remove(key, value) ||
        ((key instanceof String) && super.remove(((String) key).toLowerCase(), value));
  }

  @Override
  public Map<K, V> clone(){
    return (Map<K, V>) super.clone();
  }
}
