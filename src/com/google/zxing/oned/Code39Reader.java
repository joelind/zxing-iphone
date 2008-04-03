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

package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.GenericResultPoint;

import java.util.Hashtable;

/**
 * <p>Decodes Code 39 barcodes. This does not supported "Full ASCII Code 39" yet.</p>
 *
 * @author srowen@google.com (Sean Owen)
 */
public final class Code39Reader extends AbstractOneDReader {

  private static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%";
  private static final char[] ALPHABET = ALPHABET_STRING.toCharArray();

  /**
   * These represent the encodings of characters, as patterns of wide and narrow bars.
   * The 9 least-significant bits of each int correspond to the pattern of wide and narrow,
   * with 1s representing "wide" and 0s representing narrow.
   */
  private static final int[] CHARACTER_ENCODINGS = {
      0x034, 0x121, 0x061, 0x160, 0x031, 0x130, 0x070, 0x025, 0x124, 0x064, // 0-9
      0x109, 0x049, 0x148, 0x019, 0x118, 0x058, 0x00D, 0x10C, 0x04C, 0x01C, // A-J
      0x103, 0x043, 0x142, 0x013, 0x112, 0x052, 0x007, 0x106, 0x046, 0x016, // K-T
      0x181, 0x0C1, 0x1C0, 0x091, 0x190, 0x0D0, 0x085, 0x184, 0x0C4, 0x094, // U-*
      0x0A8, 0x0A2, 0x08A, 0x02A // $-%
  };

  private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[39];

  private final boolean usingCheckDigit;
  private final boolean extendedMode;

  /**
   * Creates a reader that assumes all encoded data is data, and does not treat the final
   * character as a check digit. It will not decoded "extended Code 39" sequences.
   */
  public Code39Reader() {
    usingCheckDigit = false;
    extendedMode = false;
  }

  /**
   * Creates a reader that can be configured to check the last character as a check digit.
   * It will not decoded "extended Code 39" sequences.
   *
   * @param usingCheckDigit if true, treat the last data character as a check digit, not
   * data, and verify that the checksum passes.
   */
  public Code39Reader(boolean usingCheckDigit) {
    this.usingCheckDigit = usingCheckDigit;
    this.extendedMode = false;
  }

  /**
   * Creates a reader that can be configured to check the last character as a check digit,
   * or optionally attempt to decode "extended Code 39" sequences that are used to encode
   * the full ASCII character set.
   *
   * @param usingCheckDigit if true, treat the last data character as a check digit, not
   * data, and verify that the checksum passes.
   * @param extendedMode if true, will attempt to decode extended Code 39 sequences in the
   * text.
   */
  public Code39Reader(boolean usingCheckDigit, boolean extendedMode) {
    this.usingCheckDigit = usingCheckDigit;
    this.extendedMode = extendedMode;
  }

  public Result decodeRow(int rowNumber, BitArray row, Hashtable hints) throws ReaderException {

    int[] start = findAsteriskPattern(row);

    int nextStart = start[1];

    int end = row.getSize();

    // Read off white space
    while (nextStart < end && !row.get(nextStart)) {
      nextStart++;
    }

    StringBuffer result = new StringBuffer();
    int[] counters = new int[9];
    char decodedChar;
    int lastStart;
    do {
      recordPattern(row, nextStart, counters);
      int pattern = toNarrowWidePattern(counters);
      decodedChar = patternToChar(pattern);
      result.append(decodedChar);
      lastStart = nextStart;
      for (int i = 0; i < counters.length; i++) {
        nextStart += counters[i];
      }
      // Read off white space
      while (nextStart < end && !row.get(nextStart)) {
        nextStart++;
      }
    } while (decodedChar != '*');
    result.deleteCharAt(result.length() - 1); // remove asterisk

    if (usingCheckDigit) {
      int max = result.length() - 1;
      int total = 0;
      for (int i = 0; i < max; i++) {
        total += ALPHABET_STRING.indexOf(result.charAt(i));
      }
      if (total % 43 != ALPHABET_STRING.indexOf(result.charAt(max))) {
        throw new ReaderException("Checksum failed");
      }
      result.deleteCharAt(max);
    }

    String resultString = result.toString();
    if (extendedMode) {
      resultString = decodeExtended(resultString);
    }
    float left = (float) (start[1] + start[0]) / 2.0f;
    float right = (float) (nextStart + lastStart) / 2.0f;
    return new Result(
        resultString,
        null,
        new ResultPoint[]{
            new GenericResultPoint(left, (float) rowNumber),
            new GenericResultPoint(right, (float) rowNumber)},
        BarcodeFormat.CODE_39);

  }

  private static int[] findAsteriskPattern(BitArray row) throws ReaderException {
    int width = row.getSize();
    int rowOffset = 0;
    while (rowOffset < width) {
      if (row.get(rowOffset)) {
        break;
      }
      rowOffset++;
    }

    int counterPosition = 0;
    int[] counters = new int[9];
    int patternStart = rowOffset;
    boolean isWhite = false;
    int patternLength = counters.length;

    for (int i = rowOffset; i < width; i++) {
      boolean pixel = row.get(i);
      if ((!pixel && isWhite) || (pixel && !isWhite)) {
        counters[counterPosition]++;
      } else {
        if (counterPosition == patternLength - 1) {
          try {
            if (toNarrowWidePattern(counters) == ASTERISK_ENCODING) {
              return new int[]{patternStart, i};
            }
          } catch (ReaderException re) {
            // no match, continue
          }
          patternStart += counters[0] + counters[1];
          for (int y = 2; y < patternLength; y++) {
            counters[y - 2] = counters[y];
          }
          counters[patternLength - 2] = 0;
          counters[patternLength - 1] = 0;
          counterPosition--;
        } else {
          counterPosition++;
        }
        counters[counterPosition] = 1;
        isWhite = !isWhite;
      }
    }
    throw new ReaderException("Can't find pattern");
  }

  private static int toNarrowWidePattern(int[] counters) throws ReaderException {
    int numCounters = counters.length;
    int maxNarrowCounter = 0;
    int wideCounters;
    do {
      int minCounter = Integer.MAX_VALUE;
      for (int i = 0; i < numCounters; i++) {
        int counter = counters[i];
        if (counter < minCounter && counter > maxNarrowCounter) {
          minCounter = counter;
        }
      }
      maxNarrowCounter = minCounter;
      wideCounters = 0;
      int pattern = 0;
      for (int i = 0; i < numCounters; i++) {
        if (counters[i] > maxNarrowCounter) {
          pattern |= 1 << (numCounters - 1 - i);
          wideCounters++;
        }
      }
      if (wideCounters == 3) {
        return pattern;
      }
    } while (wideCounters > 3);
    throw new ReaderException("Can't find 3 wide bars/spaces out of 9");
  }

  private static char patternToChar(int pattern) throws ReaderException {
    for (int i = 0; i < CHARACTER_ENCODINGS.length; i++) {
      if (CHARACTER_ENCODINGS[i] == pattern) {
        return ALPHABET[i];
      }
    }
    throw new ReaderException("Pattern did not match character encoding");
  }

  private static String decodeExtended(String encoded) throws ReaderException {
    int length = encoded.length();
    StringBuffer decoded = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      char c = encoded.charAt(i);
      if (c == '+' || c == '$' || c == '%' || c == '/') {
        char next = encoded.charAt(i + 1);
        char decodedChar = '\0';
        switch (c) {
          case '+':
            // +A to +Z map to a to z
            if (next >= 'A' && next <= 'Z') {
              decodedChar = (char) (next + 32);
            } else {
              throw new ReaderException("Invalid extended code 39 sequence: " + c + next);
            }
            break;
          case '$':
            // $A to $Z map to control codes SH to SB
            if (next >= 'A' && next <= 'Z') {
              decodedChar = (char) (next - 64);
            } else {
              throw new ReaderException("Invalid extended code 39 sequence: " + c + next);
            }
            break;
          case '%':
            // %A to %E map to control codes ESC to US
            if (next >= 'A' && next <= 'E') {
              decodedChar = (char) (next - 38);
            } else if (next >= 'F' && next <= 'W') {
              decodedChar = (char) (next - 11);
            } else {
              throw new ReaderException("Invalid extended code 39 sequence: " + c + next);
            }
            break;
          case '/':
            // /A to /O map to ! to , and /Z maps to :
            if (next >= 'A' && next <= 'O') {
              decodedChar = (char) (next - 32);
            } else if (next == 'Z') {
              decodedChar = ':';
            } else {
              throw new ReaderException("Invalid extended sequence: " + c + next);
            }
            break;
        }
        decoded.append(decodedChar);
        // bump up i again since we read two characters
        i++;
      } else {
        decoded.append(c);
      }
    }
    return decoded.toString();
  }

}
