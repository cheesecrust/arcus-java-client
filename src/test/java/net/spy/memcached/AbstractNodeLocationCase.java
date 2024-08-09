package net.spy.memcached;

import java.util.Iterator;

import org.junit.Test;

import org.jmock.Mockery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractNodeLocationCase {

  protected MemcachedNode[] nodes;
  protected MemcachedNode[] nodeMocks;
  protected NodeLocator locator;
  protected Mockery context;

  private void runSequenceAssertion(NodeLocator l, String k, int... seq) {
    int pos = 0;
    for (Iterator<MemcachedNode> i = l.getSequence(k); i.hasNext(); ) {
      assertEquals("At position " + pos, nodes[seq[pos]].toString(),
              i.next().toString());
      try {
        i.remove();
        fail("Allowed a removal from a sequence.");
      } catch (UnsupportedOperationException e) {
        // pass
      }
      pos++;
    }
    assertEquals(seq.length, pos, "Incorrect sequence size for " + k);
  }

  @Test
  public final void testCloningGetPrimary() {
    setupNodes(5);
    assertInstanceOf(MemcachedNodeROImpl.class,
            locator.getReadonlyCopy().getPrimary("hi"));
  }

  @Test
  public final void testCloningGetAll() {
    setupNodes(5);
    assertInstanceOf(MemcachedNodeROImpl.class,
            locator.getReadonlyCopy().getAll().iterator().next());
  }

  @Test
  public final void testCloningGetSequence() {
    setupNodes(5);
    assertInstanceOf(MemcachedNodeROImpl.class,
            locator.getReadonlyCopy().getSequence("hi").next());
  }

  protected final void assertSequence(String k, int... seq) {
    runSequenceAssertion(locator, k, seq);
    runSequenceAssertion(locator.getReadonlyCopy(), k, seq);
  }

  protected void setupNodes(int n) {
    nodes = new MemcachedNode[n];
    nodeMocks = new MemcachedNode[nodes.length];

    for (int i = 0; i < nodeMocks.length; i++) {
      nodeMocks[i] = context.mock(MemcachedNode.class, "node#" + i);
      nodes[i] = nodeMocks[i];
    }
  }
}
