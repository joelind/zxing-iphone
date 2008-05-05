/*
 * Copyright 2007 Google Inc.
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

package com.google.zxing.qrcode.detector;

import com.google.zxing.ResultPoint;

/**
 * <p>Encapsulates a finder pattern, which are the three square patterns found in
 * the corners of QR Codes. It also encapsulates a count of similar finder patterns,
 * as a convenience to the finder's bookkeeping.</p>
 *
 * @author srowen@google.com (Sean Owen)
 */
public final class FinderPattern implements ResultPoint {

  private final float posX;
  private final float posY;
  private final float estimatedModuleSize;
  private int count;

  FinderPattern(float posX, float posY, float estimatedModuleSize) {
    this.posX = posX;
    this.posY = posY;
    this.estimatedModuleSize = estimatedModuleSize;
    this.count = 1;
  }

  public float getX() {
    return posX;
  }

  public float getY() {
    return posY;
  }

  float getEstimatedModuleSize() {
    return estimatedModuleSize;
  }

  int getCount() {
    return count;
  }

  void incrementCount() {
    this.count++;
  }

  /**
   * <p>Determines if this finder pattern "about equals" a finder pattern at the stated
   * position and size -- meaning, it is at nearly the same center with nearly the same size.</p>
   */
  boolean aboutEquals(float moduleSize, float i, float j) {
    return Math.abs(i - posY) <= moduleSize &&
        Math.abs(j - posX) <= moduleSize &&
        (Math.abs(moduleSize - estimatedModuleSize) <= 1.0f ||
            Math.abs(moduleSize - estimatedModuleSize) / estimatedModuleSize <= 0.1f);
  }

}
