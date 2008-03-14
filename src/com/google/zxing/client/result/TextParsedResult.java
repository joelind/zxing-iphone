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

import com.google.zxing.Result;

/**
 * @author srowen@google.com (Sean Owen)
 */
public final class TextParsedResult extends ParsedReaderResult {

  private final String text;

  private TextParsedResult(String text) {
    super(ParsedReaderResultType.TEXT);
    this.text = text;
  }

  public static TextParsedResult parse(Result result) {
    return new TextParsedResult(result.getText());
  }

  public String getText() {
    return text;
  }

  public String getDisplayResult() {
    return text;
  }

}
