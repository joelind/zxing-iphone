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

package com.google.zxing;

import java.util.Hashtable;

/**
 * <p>Encapsulates the result of decoding a barcode within an image.</p>
 *
 * @author Sean Owen
 */
public final class Result {

  private final String text;
  private final byte[] rawBytes;
  private final ResultPoint[] resultPoints;
  private final BarcodeFormat format;
  private Hashtable resultMetadata;

  public Result(String text,
                byte[] rawBytes,
                ResultPoint[] resultPoints,
                BarcodeFormat format) {
    if (text == null && rawBytes == null) {
      throw new IllegalArgumentException("Text and bytes are null");
    }
    this.text = text;
    this.rawBytes = rawBytes;
    this.resultPoints = resultPoints;
    this.format = format;
    this.resultMetadata = null;
  }

  /**
   * @return raw text encoded by the barcode, if applicable, otherwise <code>null</code>
   */
  public String getText() {
    return text;
  }

  /**
   * @return raw bytes encoded by the barcode, if applicable, otherwise <code>null</code>
   */
  public byte[] getRawBytes() {
    return rawBytes;
  }

  /**
   * @return points related to the barcode in the image. These are typically points
   *         identifying finder patterns or the corners of the barcode. The exact meaning is
   *         specific to the type of barcode that was decoded.
   */
  public ResultPoint[] getResultPoints() {
    return resultPoints;
  }

  /**
   * @return {@link BarcodeFormat} representing the format of the barcode that was decoded
   */
  public BarcodeFormat getBarcodeFormat() {
    return format;
  }

  /**
   * @return {@link Hashtable} mapping {@link ResultMetadataType} keys to values. May be
   *   <code>null</code>. This contains optional metadata about what was detected about the barcode,
   *   like orientation.
   */
  public Hashtable getResultMetadata() {
    return resultMetadata;
  }

  public void putMetadata(ResultMetadataType type, Object value) {
    if (resultMetadata == null) {
      resultMetadata = new Hashtable(3);
    }
    resultMetadata.put(type, value);
  }

  public String toString() {
    if (text == null) {
      return "[" + rawBytes.length + " bytes]";
    } else {
      return text;
  }
  }

}
