package net.spy.memcached.transcoders;

import java.util.Arrays;

import net.spy.memcached.CachedData;

import org.junit.Before;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhalinV1TranscoderTest extends BaseTranscoderCase {

  @Before
  public void setUp() {
    super.setUp();
    setTranscoder(new WhalinV1Transcoder());
  }

  @Override
  public void testByteArray() throws Exception {
    byte[] a = {'a', 'b', 'c'};

    CachedData cd = getTranscoder().encode(a);
    byte[] decoded = (byte[]) getTranscoder().decode(cd);
    assertNotNull(decoded);
    assertTrue(Arrays.equals(a, decoded));
  }

  @Override
  protected int getStringFlags() {
    // Flags are not used by this transcoder.
    return 0;
  }

}
