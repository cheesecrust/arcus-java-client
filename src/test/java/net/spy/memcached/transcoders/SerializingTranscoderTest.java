// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>

package net.spy.memcached.transcoders;

import java.util.Arrays;
import java.util.Calendar;

import net.spy.memcached.CachedData;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the serializing transcoder.
 */
public class SerializingTranscoderTest extends BaseTranscoderCase {

  private SerializingTranscoder tc;
  private TranscoderUtils tu;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tc = new SerializingTranscoder();
    setTranscoder(tc);
    tu = new TranscoderUtils(true);
  }

  @Test
  public void testNonserializable() throws Exception {
    try {
      tc.encode(new Object());
      fail("Processed a non-serializable object.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  public void testCompressedStringNotSmaller() throws Exception {
    String s1 = "This is a test simple string that will not be compressed.";
    // Reduce the compression threshold so it'll attempt to compress it.
    tc.setCompressionThreshold(8);
    CachedData cd = tc.encode(s1);
    // This should *not* be compressed because it is too small
    assertEquals(0, cd.getFlags());
    assertTrue(Arrays.equals(s1.getBytes(), cd.getData()));
    assertEquals(s1, tc.decode(cd));
  }

  @Test
  public void testCompressedString() throws Exception {
    // This one will actually compress
    String s1 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    tc.setCompressionThreshold(8);
    CachedData cd = tc.encode(s1);
    assertEquals(SerializingTranscoder.COMPRESSED, cd.getFlags());
    assertFalse(Arrays.equals(s1.getBytes(), cd.getData()));
    assertEquals(s1, tc.decode(cd));
  }

  @Test
  public void testObject() throws Exception {
    Calendar c = Calendar.getInstance();
    CachedData cd = tc.encode(c);
    assertEquals(SerializingTranscoder.SERIALIZED, cd.getFlags());
    assertEquals(c, tc.decode(cd));
  }

  @Test
  public void testCompressedObject() throws Exception {
    tc.setCompressionThreshold(8);
    Calendar c = Calendar.getInstance();
    CachedData cd = tc.encode(c);
    assertEquals(SerializingTranscoder.SERIALIZED
            | SerializingTranscoder.COMPRESSED, cd.getFlags());
    assertEquals(c, tc.decode(cd));
  }

  @Test
  public void testUnencodeable() throws Exception {
    try {
      CachedData cd = tc.encode(new Object());
      fail("Should fail to serialize, got" + cd);
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  public void testUndecodeable() throws Exception {
    CachedData cd = new CachedData(
            Integer.MAX_VALUE &
                    ~(SerializingTranscoder.COMPRESSED
                            | SerializingTranscoder.SERIALIZED),
            tu.encodeInt(Integer.MAX_VALUE),
            tc.getMaxSize());
    assertNull(tc.decode(cd));
  }

  @Test
  public void testUndecodeableSerialized() throws Exception {
    CachedData cd = new CachedData(SerializingTranscoder.SERIALIZED,
            tu.encodeInt(Integer.MAX_VALUE),
            tc.getMaxSize());
    assertNull(tc.decode(cd));
  }

  @Test
  public void testUndecodeableCompressed() throws Exception {
    CachedData cd = new CachedData(
            SerializingTranscoder.COMPRESSED,
            tu.encodeInt(Integer.MAX_VALUE),
            tc.getMaxSize());
    System.out.println("got " + tc.decode(cd));
    assertNull(tc.decode(cd));
  }

  @Override
  protected int getStringFlags() {
    return 0;
  }

}
