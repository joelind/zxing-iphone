/*
 * Copyright 2008 ZXing authors
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

import com.google.zxing.Result;

/**
 * Parses a "geo:" URI result, which specifies a location on the surface of
 * the Earth as well as an optional altitude above the surface. See
 * <a href="http://tools.ietf.org/html/draft-mayrhofer-geo-uri-00">
 * http://tools.ietf.org/html/draft-mayrhofer-geo-uri-00</a>.
 *
 * @author Sean Owen
 */
final class GeoResultParser extends ResultParser {

  private GeoResultParser() {
  }

  public static GeoParsedResult parse(Result result) {
    String rawText = result.getText();
    if (rawText == null || (!rawText.startsWith("geo:") && !rawText.startsWith("GEO:"))) {
      return null;
    }
    // Drop geo, query portion
    int queryStart = rawText.indexOf('?', 4);
    String geoURIWithoutQuery = queryStart < 0 ? rawText.substring(4) : rawText.substring(4, queryStart);
    int latitudeEnd = geoURIWithoutQuery.indexOf(',');
    if (latitudeEnd < 0) {
      return null;
    }
    int longitudeEnd = geoURIWithoutQuery.indexOf(',', latitudeEnd + 1);    
    double latitude, longitude, altitude;
    try {
      latitude = Double.parseDouble(geoURIWithoutQuery.substring(0, latitudeEnd));
      if (longitudeEnd < 0) {
        longitude = Double.parseDouble(geoURIWithoutQuery.substring(latitudeEnd + 1));
        altitude = 0.0;
      } else {
        longitude = Double.parseDouble(geoURIWithoutQuery.substring(latitudeEnd + 1, longitudeEnd));
        altitude = Double.parseDouble(geoURIWithoutQuery.substring(longitudeEnd + 1));
      }
    } catch (NumberFormatException nfe) {
      return null;
    }
    return new GeoParsedResult(rawText.startsWith("GEO:") ? "geo:" + rawText.substring(4) : rawText,
                               latitude, longitude, altitude);
  }

}