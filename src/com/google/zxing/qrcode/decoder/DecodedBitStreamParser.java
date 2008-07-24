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

package com.google.zxing.qrcode.decoder;

import com.google.zxing.ReaderException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.CharacterSetECI;

import java.io.UnsupportedEncodingException;

/**
 * <p>QR Codes can encode text as bits in one of several modes, and can use multiple modes
 * in one QR Code. This class decodes the bits back into text.</p>
 *
 * <p>See ISO 18004:2006, 6.4.3 - 6.4.7</p>
 *
 * @author srowen@google.com (Sean Owen)
 */
final class DecodedBitStreamParser {

  /**
   * See ISO 18004:2006, 6.4.4 Table 5
   */
  private static final char[] ALPHANUMERIC_CHARS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
      'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      ' ', '$', '%', '*', '+', '-', '.', '/', ':'
  };
  private static final String SHIFT_JIS = "SJIS";
  private static final String EUC_JP = "EUC_JP";
  private static final boolean ASSUME_SHIFT_JIS;
  private static final String UTF8 = "UTF8";
  private static final String ISO88591 = "ISO8859_1";

  static {
    String platformDefault = System.getProperty("file.encoding");
    ASSUME_SHIFT_JIS = SHIFT_JIS.equalsIgnoreCase(platformDefault) || EUC_JP.equalsIgnoreCase(platformDefault);
  }

  private DecodedBitStreamParser() {
  }

  static String decode(byte[] bytes, Version version) throws ReaderException {
    BitSource bits = new BitSource(bytes);
    StringBuffer result = new StringBuffer();
    CharacterSetECI currentCharacterSetECI = null;
    boolean fc1InEffect = false;
    Mode mode;
    do {
      // While still another segment to read...
      if (bits.available() == 0) {
        // OK, assume we're done. Really, a TERMINATOR mode should have been recorded here
        mode = Mode.TERMINATOR;
      } else {
        mode = Mode.forBits(bits.readBits(4)); // mode is encoded by 4 bits
      }
      if (!mode.equals(Mode.TERMINATOR)) {
        if (mode.equals(Mode.FNC1_FIRST_POSITION) || mode.equals(Mode.FNC1_SECOND_POSITION)) {
          // We do little with FNC1 except alter the parsed result a bit according to the spec
          fc1InEffect = true;
        } else if (mode.equals(Mode.ECI)) {
          // Count doesn't apply to ECI
          int value = parseECIValue(bits);
          try {
            currentCharacterSetECI = CharacterSetECI.getCharacterSetECIByValue(value);
          } catch (IllegalArgumentException iae) {
            // unsupported... just continue?
          }
        } else {
          // How many characters will follow, encoded in this mode?
         int count = bits.readBits(mode.getCharacterCountBits(version));
          if (mode.equals(Mode.NUMERIC)) {
            decodeNumericSegment(bits, result, count);
          } else if (mode.equals(Mode.ALPHANUMERIC)) {
            decodeAlphanumericSegment(bits, result, count, fc1InEffect);
          } else if (mode.equals(Mode.BYTE)) {
            decodeByteSegment(bits, result, count, currentCharacterSetECI);
          } else if (mode.equals(Mode.KANJI)) {
            decodeKanjiSegment(bits, result, count);
          } else {
            throw new ReaderException("Unsupported mode indicator");
          }
        }
      }
    } while (!mode.equals(Mode.TERMINATOR));

    // I thought it wasn't allowed to leave extra bytes after the terminator but it happens
    /*
    int bitsLeft = bits.available();
    if (bitsLeft > 0) {
      if (bitsLeft > 6 || bits.readBits(bitsLeft) != 0) {
        throw new ReaderException("Excess bits or non-zero bits after terminator mode indicator");
      }
    }
     */
    return result.toString();
  }

  private static void decodeKanjiSegment(BitSource bits,
                                         StringBuffer result,
                                         int count) throws ReaderException {
    // Each character will require 2 bytes. Read the characters as 2-byte pairs
    // and decode as Shift_JIS afterwards
    byte[] buffer = new byte[2 * count];
    int offset = 0;
    while (count > 0) {
      // Each 13 bits encodes a 2-byte character
      int twoBytes = bits.readBits(13);
      int assembledTwoBytes = ((twoBytes / 0x0C0) << 8) | (twoBytes % 0x0C0);
      if (assembledTwoBytes < 0x01F00) {
        // In the 0x8140 to 0x9FFC range
        assembledTwoBytes += 0x08140;
      } else {
        // In the 0xE040 to 0xEBBF range
        assembledTwoBytes += 0x0C140;
      }
      buffer[offset] = (byte) (assembledTwoBytes >> 8);
      buffer[offset + 1] = (byte) assembledTwoBytes;
      offset += 2;
      count--;
    }
    // Shift_JIS may not be supported in some environments:
    try {
      result.append(new String(buffer, SHIFT_JIS));
    } catch (UnsupportedEncodingException uee) {
      throw new ReaderException(SHIFT_JIS + " encoding is not supported on this device");
    }
  }

  private static void decodeByteSegment(BitSource bits,
                                        StringBuffer result,
                                        int count,
                                        CharacterSetECI currentCharacterSetECI) throws ReaderException {
    byte[] readBytes = new byte[count];
    if (count << 3 > bits.available()) {
      throw new ReaderException("Count too large: " + count);
    }
    for (int i = 0; i < count; i++) {
      readBytes[i] = (byte) bits.readBits(8);
    }
    String encoding;
    if (currentCharacterSetECI == null) {
    // The spec isn't clear on this mode; see
    // section 6.4.5: t does not say which encoding to assuming
    // upon decoding. I have seen ISO-8859-1 used as well as
    // Shift_JIS -- without anything like an ECI designator to
    // give a hint.
      encoding = guessEncoding(readBytes);
    } else {
      encoding = currentCharacterSetECI.getEncodingName();
    }
    try {
      result.append(new String(readBytes, encoding));
    } catch (UnsupportedEncodingException uce) {
      throw new ReaderException(uce.toString());
    }
  }

  private static void decodeAlphanumericSegment(BitSource bits,
                                                StringBuffer result,
                                                int count,
                                                boolean fc1InEffect) {
    // Read two characters at a time
    int start = result.length();
    while (count > 1) {
      int nextTwoCharsBits = bits.readBits(11);
      result.append(ALPHANUMERIC_CHARS[nextTwoCharsBits / 45]);
      result.append(ALPHANUMERIC_CHARS[nextTwoCharsBits % 45]);
      count -= 2;
    }
    if (count == 1) {
      // special case: one character left
      result.append(ALPHANUMERIC_CHARS[bits.readBits(6)]);
    }
    // See section 6.4.8.1, 6.4.8.2
    if (fc1InEffect) {
      // We need to massage the result a bit if in an FNC1 mode:
      for (int i = start; i < result.length(); i++) {
        if (result.charAt(i) == '%') {
          if (i < result.length() - 1 && result.charAt(i + 1) == '%') {
            // %% is rendered as %
            result.deleteCharAt(i + 1);
          } else {
            // In alpha mode, % should be converted to FNC1 separator 0x1D
            result.setCharAt(i, (char) 0x1D);
          }
        }
      }
    }
  }

  private static void decodeNumericSegment(BitSource bits,
                                           StringBuffer result,
                                           int count) throws ReaderException {
    // Read three digits at a time
    while (count >= 3) {
      // Each 10 bits encodes three digits
      int threeDigitsBits = bits.readBits(10);
      if (threeDigitsBits >= 1000) {
        throw new ReaderException("Illegal value for 3-digit unit: " + threeDigitsBits);
      }
      result.append(ALPHANUMERIC_CHARS[threeDigitsBits / 100]);
      result.append(ALPHANUMERIC_CHARS[(threeDigitsBits / 10) % 10]);
      result.append(ALPHANUMERIC_CHARS[threeDigitsBits % 10]);
      count -= 3;
    }
    if (count == 2) {
      // Two digits left over to read, encoded in 7 bits
      int twoDigitsBits = bits.readBits(7);
      if (twoDigitsBits >= 100) {
        throw new ReaderException("Illegal value for 2-digit unit: " + twoDigitsBits);
      }
      result.append(ALPHANUMERIC_CHARS[twoDigitsBits / 10]);
      result.append(ALPHANUMERIC_CHARS[twoDigitsBits % 10]);
    } else if (count == 1) {
      // One digit left over to read
      int digitBits = bits.readBits(4);
      if (digitBits >= 10) {
        throw new ReaderException("Illegal value for digit unit: " + digitBits);
      }
      result.append(ALPHANUMERIC_CHARS[digitBits]);
    }
  }

  private static String guessEncoding(byte[] bytes) {
    if (ASSUME_SHIFT_JIS) {
      return SHIFT_JIS;
    }
    // Does it start with the UTF-8 byte order mark? then guess it's UTF-8
    if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
      return UTF8;
    }
    // For now, merely tries to distinguish ISO-8859-1, UTF-8 and Shift_JIS,
    // which should be by far the most common encodings. ISO-8859-1
    // should not have bytes in the 0x80 - 0x9F range, while Shift_JIS
    // uses this as a first byte of a two-byte character. If we see this
    // followed by a valid second byte in Shift_JIS, assume it is Shift_JIS.
    // If we see something else in that second byte, we'll make the risky guess
    // that it's UTF-8.
    int length = bytes.length;
    boolean canBeISO88591 = true;
    boolean lastWasPossibleDoubleByteStart = false;
    for (int i = 0; i < length; i++) {
      int value = bytes[i] & 0xFF;
      if (value >= 0x80 && value <= 0x9F && i < length - 1) {
        canBeISO88591 = false;
        // ISO-8859-1 shouldn't use this, but before we decide it is Shift_JIS,
        // just double check that it is followed by a byte that's valid in
        // the Shift_JIS encoding
        if (lastWasPossibleDoubleByteStart) {
          // If we just checked this and the last byte for being a valid double-byte
          // char, don't check starting on this byte. If the this and the last byte
          // formed a valid pair, then this shouldn't be checked to see if it starts
          // a double byte pair of course.
          lastWasPossibleDoubleByteStart = false;
        } else {
          // ... otherwise do check to see if this plus the next byte form a valid
          // double byte pair encoding a character.
          lastWasPossibleDoubleByteStart = true;
          int nextValue = bytes[i + 1] & 0xFF;
          if ((value & 0x1) == 0) {
            // if even, next value should be in [0x9F,0xFC]
            // if not, we'll guess UTF-8
            if (nextValue < 0x9F || nextValue > 0xFC) {
              return UTF8;
            }
          } else {
            // if odd, next value should be in [0x40,0x9E]
            // if not, we'll guess UTF-8
            if (nextValue < 0x40 || nextValue > 0x9E) {
              return UTF8;
            }
          }
        }
      }
    }
    return canBeISO88591 ? ISO88591 : SHIFT_JIS;
  }
  
  private static int parseECIValue(BitSource bits) {
    int firstByte = bits.readBits(8);
    if ((firstByte & 0x80) == 0) {
      // just one byte
      return firstByte & 0x7F;
    } else if ((firstByte & 0xC0) == 0x80) {
      // two bytes
      int secondByte = bits.readBits(8);
      return ((firstByte & 0x3F) << 8) | secondByte;
    } else if ((firstByte & 0xE0) == 0xC0) {
      // three bytes
      int secondThirdBytes = bits.readBits(16);
      return ((firstByte & 0x1F) << 16) | secondThirdBytes;
    }
    throw new IllegalArgumentException("Bad ECI bits starting with byte " + firstByte);
  }

}
