package net.spy.memcached;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class OptimizeTest {

  @Test
  void testParallelGet() throws Throwable {
    ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
    builder.setShouldOptimize(true);
    // Get a connection with the get optimization.
    ArcusClientPool client =
            ArcusClient.createArcusClientPool("127.0.0.1:2181", "test", builder, 1);

    final List<String> keys = new ArrayList<>(10000);
    for (int i = 0; i < 100; i++) {
      keys.add("k" + i);
      Boolean b = client.set(keys.get(i), 0, "value" + i).get();
      Assertions.assertEquals(true, b);
    }

    List<Future<Object>> results = new ArrayList<>(10000);
    for (int i = 0; i < 100; i++) {
      results.add(client.asyncGet(keys.get(i)));
    }

    for (int i = 0; i < 100; i++) {
      Object o = results.get(i).get();
      Assertions.assertEquals("value" + i, o);
    }
  }
}
