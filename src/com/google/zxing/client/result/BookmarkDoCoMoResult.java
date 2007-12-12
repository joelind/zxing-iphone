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

package com.google.zxing.client.result;

/**
 * @author srowen@google.com (Sean Owen)
 */
public final class BookmarkDoCoMoResult extends AbstractDoCoMoResult {

  private final String title;
  private final String uri;

  public BookmarkDoCoMoResult(String rawText) {
    super(ParsedReaderResultType.BOOKMARK);
    if (!rawText.startsWith("MEBKM:")) {
      throw new IllegalArgumentException("Does not begin with MEBKM");
    }
    title = matchSinglePrefixedField("TITLE:", rawText);
    String uriString = matchRequiredPrefixedField("URL:", rawText)[0];
    if (!URIParsedResult.isBasicallyValidURI(uriString)) {
      throw new IllegalArgumentException("Invalid URI: " + uriString);
    }
    uri = uriString;
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