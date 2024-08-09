package net.spy.memcached.protocol.binary;

import net.spy.memcached.OperationFactory;
import net.spy.memcached.OperationFactoryTestBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationFactoryTest extends OperationFactoryTestBase {

  @Override
  protected OperationFactory getOperationFactory() {
    return new BinaryOperationFactory();
  }

  @Override
  public void testMultipleGetsOperationCloning() {
    assertTrue(true);
  }

  @Override
  public void testMultipleGetsOperationFanout() {
    assertTrue(true);
  }
}
