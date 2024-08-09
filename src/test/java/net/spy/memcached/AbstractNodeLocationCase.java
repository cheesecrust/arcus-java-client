package net.spy.memcached;

import java.util.Iterator;

import junit.framework.TestCase;

import org.jmock.Mockery;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public abstract class AbstractNodeLocationCase extends TestCase {

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
    assertEquals("Incorrect sequence size for " + k, seq.length, pos);
  }

  public final void testCloningGetPrimary() {
    setupNodes(5);
    assertInstanceOf(MemcachedNodeROImpl.class,
            locator.getReadonlyCopy().getPrimary("hi"));
  }

  public final void testCloningGetAll() {
    setupNodes(5);
    assertInstanceOf(MemcachedNodeROImpl.class,
            locator.getReadonlyCopy().getAll().iterator().next());
  }

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
    context = new Mockery();

    for (int i = 0; i < nodeMocks.length; i++) {
      nodeMocks[i] = context.mock(MemcachedNode.class, "node#" + i);
      nodes[i] = nodeMocks[i];
    }
  }
}
