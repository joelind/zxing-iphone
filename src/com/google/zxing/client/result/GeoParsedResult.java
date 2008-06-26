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

/**
 * @author srowen@google.com (Sean Owen)
 */
public final class GeoParsedResult extends ParsedResult {

  private final String geoURI;
  private final float latitude;
  private final float longitude;
  private final float altitude;

  GeoParsedResult(String geoURI, float latitude, float longitude, float altitude) {
    super(ParsedResultType.GEO);
    this.geoURI = geoURI;
    this.latitude = latitude;
    this.longitude = longitude;
    this.altitude = altitude;
  }

  public String getGeoURI() {
    return geoURI;
  }

  /**
   * @return latitude in degrees
   */
  public float getLatitude() {
    return latitude;
  }

  /**
   * @return longitude in degrees
   */
  public float getLongitude() {
    return longitude;
  }

  /**
   * @return altitude in meters. If not specified, in the geo URI, returns 0.0
   */
  public float getAltitude() {
    return altitude;
  }

  public String getDisplayResult() {
    StringBuffer result = new StringBuffer(50);
    result.append(latitude);
    result.append(" deg N, ");
    result.append(longitude);
    result.append(" deg E");
    if (altitude > 0.0f) {
      result.append(", ");
      result.append(altitude);
      result.append('m');
    }
    return result.toString();
  }

  /**
   * @return a URI link to Google Maps which display the point on the Earth described
   *  by this instance, and sets the zoom level in a way that roughly reflects the
   *  altitude, if specified
   */
  /*
  public String getGoogleMapsURI() {
    StringBuffer result = new StringBuffer(50);
    result.append("http://maps.google.com/?ll=");
    result.append(latitude);
    result.append(',');
    result.append(longitude);
    if (altitude > 0.0f) {
      // Map altitude to zoom level, cleverly. Roughly, zoom level 19 is like a
      // view from 1000ft, 18 is like 2000ft, 17 like 4000ft, and so on.
      float altitudeInFeet = altitude * 3.28f;
      int altitudeInKFeet = (int) (altitudeInFeet / 1000.0f);
      // No Math.log() available here, so compute log base 2 the old fashioned way
      // Here logBaseTwo will take on a value between 0 and 18 actually
      int logBaseTwo = 0;
      while (altitudeInKFeet > 1 && logBaseTwo < 18) {
        altitudeInKFeet >>= 1;
        logBaseTwo++;
      }
      int zoom = 19 - logBaseTwo;
      result.append("&z=");
      result.append(zoom);
    }
    return result.toString();
  }
   */

}