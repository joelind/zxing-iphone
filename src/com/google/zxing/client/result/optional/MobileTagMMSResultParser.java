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

package com.google.zxing.client.result.optional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.SMSParsedResult;

/**
 * <p>Represents a "MMS" result encoded according to section 4.7 of the
 * MobileTag Reader International Specification.</p>
 *
 * @author srowen@google.com (Sean Owen)
 */
final class MobileTagMMSResultParser extends AbstractMobileTagResultParser {

  public static final String SERVICE_TYPE = "05";

  public static SMSParsedResult parse(Result result) {
    if (!result.getBarcodeFormat().equals(BarcodeFormat.DATAMATRIX)) {
      return null;
    }
    String rawText = result.getText();
    if (!rawText.startsWith(SERVICE_TYPE)) {
      return null;
    }

    String[] matches = matchDelimitedFields(rawText.substring(2), 4);
    if (matches == null) {
      return null;
    }
    String to = matches[0];
    String subject = matches[1];
    String body = matches[2];
    String title = matches[3];

    return new SMSParsedResult("sms:" + to, to, null, subject, body, title);
  }

}