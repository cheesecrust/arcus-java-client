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
package net.spy.memcached.emptycollection;

import net.spy.memcached.collection.BTreeDelete;
import net.spy.memcached.collection.ElementFlagFilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProtocolBTreeDeleteTest {

  @Test
  public void testStringify() {
    // default setting : dropIfEmpty = true

    Assertions.assertEquals("10 drop",
            (new BTreeDelete(10, false)).stringify());

    Assertions.assertEquals("10", (new BTreeDelete(10, false, false,
            ElementFlagFilter.DO_NOT_FILTER)).stringify());
    Assertions.assertEquals("10 drop", (new BTreeDelete(10, false,
            true, ElementFlagFilter.DO_NOT_FILTER)).stringify());

    Assertions.assertEquals("10..20 1", (new BTreeDelete(10, 20, 1,
            false, false, ElementFlagFilter.DO_NOT_FILTER)).stringify());
    Assertions.assertEquals("10..20 1 drop", (new BTreeDelete(10, 20,
            1, false, true, ElementFlagFilter.DO_NOT_FILTER)).stringify());

    Assertions.assertEquals("10 drop noreply", (new BTreeDelete(10,
            true)).stringify());

    Assertions.assertEquals("10 noreply", (new BTreeDelete(10, true,
            false, ElementFlagFilter.DO_NOT_FILTER)).stringify());
    Assertions.assertEquals("10 drop noreply", (new BTreeDelete(10,
            true, true, ElementFlagFilter.DO_NOT_FILTER)).stringify());

    Assertions.assertEquals("10..20 1 noreply", (new BTreeDelete(10,
            20, 1, true, false, ElementFlagFilter.DO_NOT_FILTER))
            .stringify());
    Assertions.assertEquals("10..20 1 drop noreply", (new BTreeDelete(
            10, 20, 1, true, true, ElementFlagFilter.DO_NOT_FILTER))
            .stringify());
  }
}
