// Copyright (c)  2006  Dustin Sallings <dustin@spy.net>

package net.spy.memcached.compat;

import junit.framework.TestCase;

import org.junit.Before;

import org.jmock.Mockery;

/**
 * Base test case for mock object tests.
 */
public abstract class BaseMockCase extends TestCase {
  protected Mockery context;

  @Before
  public void setUp() {
    context = new Mockery();
  }
}
