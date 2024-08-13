/*
 * arcus-java-client : Arcus Java client
 * Copyright 2010-2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spy.memcached.plugin;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.spy.memcached.compat.log.Logger;
import net.spy.memcached.compat.log.LoggerFactory;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

/**
 * Local cache storage based on ehcache.
 */
public class LocalCacheManager {

  private static CacheManager cacheManager;
  private Logger logger = LoggerFactory.getLogger(getClass());
  protected Cache<String, Object> cache;
  protected String name;

  private static CacheManager getCacheManager() {
    if (cacheManager != null) {
      return cacheManager;
    }
    
    synchronized (CacheManager.class) {
      if (cacheManager == null) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
      }
      return cacheManager;
    }
  }

  public LocalCacheManager(String name) {
    this.name = name;
    // create a undecorated Cache object.
    this.cache = getCacheManager().getCache(name, String.class, Object.class);
  }

  public LocalCacheManager(String name, int max, int exptime) {
    this.cache = getCacheManager().getCache(name, String.class, Object.class);

    if (this.cache == null) {
      CacheConfiguration<String, Object> config =
              CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                      ResourcePoolsBuilder.heap(max))
                      .withExpiry(ExpiryPolicyBuilder
                              .timeToLiveExpiration(Duration.of(exptime, ChronoUnit.SECONDS)))
                      .withExpiry(ExpiryPolicyBuilder
                              .timeToIdleExpiration(Duration.of(exptime, ChronoUnit.SECONDS)))
                      .build();
      this.cache = getCacheManager().createCache(name, config);

      logger.info("Arcus k/v local cache is enabled : ", cache.toString());
    }
  }

  public <T> T get(String key) {
    if (cache == null) {
      return null;
    }

    try {
      Object value = cache.get(key);
      if (null != value) {
        logger.debug("ArcusFrontCache: local cache hit for %s", key);
        @SuppressWarnings("unchecked") T ret = (T) value;
        return ret;
      }
    } catch (Exception e) {
      logger.info("failed to get from the local cache : %s", e.getMessage());
      return null;
    }

    return null;
  }

  public <T> Future<T> asyncGet(final String key) {
    Task<T> task = new Task<>(new Callable<T>() {
      public T call() throws Exception {
        return get(key);
      }
    });
    return task;
  }

  public Map.Entry<String, Object> getElement(String key) {
    Object value = cache.get(key);
    if (null != value) {
      logger.debug("ArcusFrontCache: local cache hit for %s", key);
    }
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  public <T> boolean put(String k, T v) {
    if (v == null) {
      return false;
    }

    try {
      cache.put(k, v);
      return true;
    } catch (Exception e) {
      logger.info("failed to put to the local cache : %s", e.getMessage());
      return false;
    }
  }

  public <T> boolean put(String k, Future<T> future, long timeout) {
    if (future == null) {
      return false;
    }

    try {
      T v = future.get(timeout, TimeUnit.MILLISECONDS);
      return put(k, v);
    } catch (Exception e) {
      logger.info("failed to put to the local cache : %s", e.getMessage());
      return false;
    }
  }

  public void delete(String k) {
    try {
      cache.remove(k);
    } catch (Exception e) {
      logger.info("failed to remove the locally cached item : %s", e.getMessage());
    }
  }

  public static class Task<T> extends FutureTask<T> {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public Task(Callable<T> callable) {
      super(callable);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
      this.run();
      return super.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
      this.run();
      return super.get(timeout, unit);
    }

    @Override
    public void run() {
      if (this.isRunning.compareAndSet(false, true)) {
        super.run();
      }
    }
  }

  @Override
  public String toString() {
    return cache.toString();
  }

}
