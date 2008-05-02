/*
 * Copyright 2008 Google Inc.
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
import com.google.zxing.client.result.ParsedReaderResultType;

/**
 * <p>Represents a "simple web" result encoded according to section 4.11 of the
 * MobileTag Reader International Specification.</p>
 *
 * @author srowen@google.com (Sean Owen)
 */
public final class MobileTagSimpleWebParsedResult extends AbstractMobileTagParsedResult {

  public static final String SERVICE_TYPE = "04";
  private static final String[] URI_PREFIXES = {
      null,
      "http://",
      "http://www.",
      "https://",
      "https://www.",
      "rtsp://",
  };

  private final String title;
  private final String uri;

  private MobileTagSimpleWebParsedResult(String title, String uri) {
    super(ParsedReaderResultType.MOBILETAG_SIMPLE_WEB);
    this.title = title;
    this.uri = uri;
  }

  public static MobileTagSimpleWebParsedResult parse(Result result) {
    if (!result.getBarcodeFormat().equals(BarcodeFormat.DATAMATRIX)) {
      return null;
    }
    String rawText = result.getText();
    if (!rawText.startsWith(SERVICE_TYPE)) {
      return null;
    }

    String[] matches = matchDelimitedFields(rawText.substring(2), 2);
    if (matches == null) {
      return null;
    }
    String uri = matches[0];
    String title = matches[1];

    char maybePrefixChar = uri.charAt(2);
    if (maybePrefixChar >= '0' && maybePrefixChar <= '9') {
      int prefixIndex = maybePrefixChar - '0';
      // Note that '0' is reserved
      if (prefixIndex >= 1 && prefixIndex < URI_PREFIXES.length) {
        uri = URI_PREFIXES[prefixIndex] + uri.substring(1);
      } else {
        uri = uri.substring(1);
      }
    }

    return new MobileTagSimpleWebParsedResult(title, uri);
  }

  public String getTitle() {
    return title;
  }

  public String getURI() {
    return uri;
  }

  public String getDisplayResult() {
    if (title == null) {
      return uri;
    } else {
      return title + '\n' + uri;
    }
  }

}