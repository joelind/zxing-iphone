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

package com.google.zxing.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.common.AbstractBlackBoxTestCase;

/**
 * @author Sean Owen
 */
public final class QRCodeBlackBox1TestCase extends AbstractBlackBoxTestCase {

  public QRCodeBlackBox1TestCase() {
    super("test/data/blackbox/qrcode-1", new MultiFormatReader(), BarcodeFormat.QR_CODE);
    addTest(17, 17, 0.0f);
    addTest(13, 13, 90.0f);
    addTest(16, 16, 180.0f);
    addTest(14, 14, 270.0f);
  }

}
