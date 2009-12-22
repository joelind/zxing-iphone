/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.result;

import junit.framework.TestCase;
import com.google.zxing.Result;
import com.google.zxing.BarcodeFormat;

/**
 * Tests {@link com.google.zxing.client.result.GeoParsedResult}.
 *
 * @author Sean Owen
 */
public final class GeoParsedResultTestCase extends TestCase {

  public void testGeo() {
    doTest("geo:1,2", 1.0, 2.0, 0.0);
    doTest("geo:100.33,-32.3344,3.35", 100.33, -32.3344, 3.35);
  }

  private static void doTest(String contents, double latitude, double longitude, double altitude) {
    Result fakeResult = new Result(contents, null, null, BarcodeFormat.QR_CODE);
    ParsedResult result = ResultParser.parseResult(fakeResult);
    assertSame(ParsedResultType.GEO, result.getType());
    GeoParsedResult geoResult = (GeoParsedResult) result;
    assertEquals(latitude, geoResult.getLatitude());
    assertEquals(longitude, geoResult.getLongitude());
    assertEquals(altitude, geoResult.getAltitude());
  }

}