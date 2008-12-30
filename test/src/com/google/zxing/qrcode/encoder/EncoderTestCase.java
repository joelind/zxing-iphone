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

package com.google.zxing.qrcode.encoder;

import com.google.zxing.WriterException;
import com.google.zxing.common.ByteArray;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Mode;
import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

/**
 * @author satorux@google.com (Satoru Takabayashi) - creator
 * @author mysen@google.com (Chris Mysen) - ported from C++
 */
public final class EncoderTestCase extends TestCase {

  public void testGetAlphanumericCode() {
    // The first ten code points are numbers.
    for (int i = 0; i < 10; ++i) {
      assertEquals(i, Encoder.getAlphanumericCode('0' + i));
    }

    // The next 26 code points are capital alphabet letters.
    for (int i = 10; i < 36; ++i) {
      assertEquals(i, Encoder.getAlphanumericCode('A' + i - 10));
    }

    // Others are symbol letters
    assertEquals(36, Encoder.getAlphanumericCode(' '));
    assertEquals(37, Encoder.getAlphanumericCode('$'));
    assertEquals(38, Encoder.getAlphanumericCode('%'));
    assertEquals(39, Encoder.getAlphanumericCode('*'));
    assertEquals(40, Encoder.getAlphanumericCode('+'));
    assertEquals(41, Encoder.getAlphanumericCode('-'));
    assertEquals(42, Encoder.getAlphanumericCode('.'));
    assertEquals(43, Encoder.getAlphanumericCode('/'));
    assertEquals(44, Encoder.getAlphanumericCode(':'));

    // Should return -1 for other letters;
    assertEquals(-1, Encoder.getAlphanumericCode('a'));
    assertEquals(-1, Encoder.getAlphanumericCode('#'));
    assertEquals(-1, Encoder.getAlphanumericCode('\0'));
  }

  public void testChooseMode() throws WriterException {
    // Numeric mode.
    assertEquals(Mode.NUMERIC, Encoder.chooseMode("0"));
    assertEquals(Mode.NUMERIC, Encoder.chooseMode("0123456789"));
    // Alphanumeric mode.
    assertEquals(Mode.ALPHANUMERIC, Encoder.chooseMode("A"));
    assertEquals(Mode.ALPHANUMERIC,
        Encoder.chooseMode("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"));
    // 8-bit byte mode.
    assertEquals(Mode.BYTE, Encoder.chooseMode("a"));
    assertEquals(Mode.BYTE, Encoder.chooseMode("#"));
    assertEquals(Mode.BYTE, Encoder.chooseMode(""));
    // Kanji mode.  We used to use MODE_KANJI for these, but we stopped
    // doing that as we cannot distinguish Shift_JIS from other encodings
    // from data bytes alone.  See also comments in qrcode_encoder.h.

    // AIUE in Hiragana in Shift_JIS
    assertEquals(Mode.BYTE, Encoder.chooseMode(shiftJISString(new byte[] {0x8,0xa,0x8,0xa,0x8,0xa,0x8,(byte)0xa6})));

    // Nihon in Kanji in Shift_JIS.
    assertEquals(Mode.BYTE, Encoder.chooseMode(shiftJISString(new byte[] {0x9,0xf,0x9,0x7b})));

    // Sou-Utsu-Byou in Kanji in Shift_JIS.
    assertEquals(Mode.BYTE, Encoder.chooseMode(shiftJISString(new byte[] {0xe,0x4,0x9,0x5,0x9,0x61})));
  }

  public void testEncode() throws WriterException {
    QRCode qrCode = new QRCode();
    Encoder.encode("ABCDEF", ErrorCorrectionLevel.H, qrCode);
    // The following is a valid QR Code that can be read by cell phones.
    String expected =
      "<<\n" +
      " mode: ALPHANUMERIC\n" +
      " ecLevel: H\n" +
      " version: 1\n" +
      " matrixWidth: 21\n" +
      " maskPattern: 0\n" +
      " numTotalBytes: 26\n" +
      " numDataBytes: 9\n" +
      " numECBytes: 17\n" +
      " numRSBlocks: 1\n" +
      " matrix:\n" +
      " 1 1 1 1 1 1 1 0 1 1 1 1 0 0 1 1 1 1 1 1 1\n" +
      " 1 0 0 0 0 0 1 0 0 1 1 1 0 0 1 0 0 0 0 0 1\n" +
      " 1 0 1 1 1 0 1 0 0 1 0 1 1 0 1 0 1 1 1 0 1\n" +
      " 1 0 1 1 1 0 1 0 1 1 1 0 1 0 1 0 1 1 1 0 1\n" +
      " 1 0 1 1 1 0 1 0 0 1 1 1 0 0 1 0 1 1 1 0 1\n" +
      " 1 0 0 0 0 0 1 0 0 1 0 0 0 0 1 0 0 0 0 0 1\n" +
      " 1 1 1 1 1 1 1 0 1 0 1 0 1 0 1 1 1 1 1 1 1\n" +
      " 0 0 0 0 0 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0\n" +
      " 0 0 1 0 1 1 1 0 1 1 0 0 1 1 0 0 0 1 0 0 1\n" +
      " 1 0 1 1 1 0 0 1 0 0 0 1 0 1 0 0 0 0 0 0 0\n" +
      " 0 0 1 1 0 0 1 0 1 0 0 0 1 0 1 0 1 0 1 1 0\n" +
      " 1 1 0 1 0 1 0 1 1 1 0 1 0 1 0 0 0 0 0 1 0\n" +
      " 0 0 1 1 0 1 1 1 1 0 0 0 1 0 1 0 1 1 1 1 0\n" +
      " 0 0 0 0 0 0 0 0 1 0 0 1 1 1 0 1 0 1 0 0 0\n" +
      " 1 1 1 1 1 1 1 0 0 0 1 0 1 0 1 1 0 0 0 0 1\n" +
      " 1 0 0 0 0 0 1 0 1 1 1 1 0 1 0 1 1 1 1 0 1\n" +
      " 1 0 1 1 1 0 1 0 1 0 1 1 0 1 0 1 0 0 0 0 1\n" +
      " 1 0 1 1 1 0 1 0 0 1 1 0 1 1 1 1 0 1 0 1 0\n" +
      " 1 0 1 1 1 0 1 0 1 0 0 0 1 0 1 0 1 1 1 0 1\n" +
      " 1 0 0 0 0 0 1 0 0 1 1 0 1 1 0 1 0 0 0 1 1\n" +
      " 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 1 0 1 0 1\n" +
      ">>\n";
    assertEquals(expected, qrCode.toString());
  }

  public void testAppendModeInfo() {
    BitVector bits = new BitVector();
    Encoder.appendModeInfo(Mode.NUMERIC, bits);
    assertEquals("0001", bits.toString());
  }

  public void testAppendLengthInfo() throws WriterException {
    {
      BitVector bits = new BitVector();
      Encoder.appendLengthInfo(1,  // 1 letter (1/1).
						  1,  // version 1.
						  Mode.NUMERIC,
						  bits);
      assertEquals("0000000001", bits.toString());  // 10 bits.
    }
    {
      BitVector bits = new BitVector();
      Encoder.appendLengthInfo(2,  // 2 letters (2/1).
						  10,  // version 10.
						  Mode.ALPHANUMERIC,
						  bits);
      assertEquals("00000000010", bits.toString());  // 11 bits.
    }
    {
      BitVector bits = new BitVector();
      Encoder.appendLengthInfo(255,  // 255 letter (255/1).
						  27,  // version 27.
						  Mode.BYTE,
						  bits);
      assertEquals("0000000011111111", bits.toString());  // 16 bits.
    }
    {
      BitVector bits = new BitVector();
      Encoder.appendLengthInfo(512,  // 512 letters (1024/2).
						  40,  // version 40.
						  Mode.KANJI,
						  bits);
      assertEquals("001000000000", bits.toString());  // 12 bits.
    }
  }

  public void testAppendBytes() throws WriterException {
    {
      // Should use appendNumericBytes.
      // 1 = 01 = 0001 in 4 bits.
      BitVector bits = new BitVector();
      Encoder.appendBytes("1", Mode.NUMERIC, bits);
      assertEquals("0001" , bits.toString());
    }
    {
      // Should use appendAlphanumericBytes.
      // A = 10 = 0xa = 001010 in 6 bits
      BitVector bits = new BitVector();
      Encoder.appendBytes("A", Mode.ALPHANUMERIC, bits);
      assertEquals("001010" , bits.toString());
      // Lower letters such as 'a' cannot be encoded in MODE_ALPHANUMERIC.
      try {
        Encoder.appendBytes("a", Mode.ALPHANUMERIC, bits);
      } catch (WriterException we) {
        // good
      }
    }
    {
      // Should use append8BitBytes.
      // 0x61, 0x62, 0x63
      BitVector bits = new BitVector();
      Encoder.appendBytes("abc", Mode.BYTE, bits);
      assertEquals("011000010110001001100011", bits.toString());
      // Anything can be encoded in QRCode.MODE_8BIT_BYTE.
      Encoder.appendBytes("\0", Mode.BYTE, bits);
    }
    {
      // Should use appendKanjiBytes.
      // 0x93, 0x5f
      BitVector bits = new BitVector();
      Encoder.appendBytes(shiftJISString(new byte[] {(byte)0x93,0x5f}), Mode.KANJI, bits);
      assertEquals("0110110011111", bits.toString());
    }
  }

  public void testTerminateBits() throws WriterException {
    {
      BitVector v = new BitVector();
      Encoder.terminateBits(0, v);
      assertEquals("", v.toString());
    }
    {
      BitVector v = new BitVector();
      Encoder.terminateBits(1, v);
      assertEquals("00000000", v.toString());
    }
    {
      BitVector v = new BitVector();
      v.appendBits(0, 3);  // Append 000
      Encoder.terminateBits(1, v);
      assertEquals("00000000", v.toString());
    }
    {
      BitVector v = new BitVector();
      v.appendBits(0, 5);  // Append 00000
      Encoder.terminateBits(1, v);
      assertEquals("00000000", v.toString());
    }
    {
      BitVector v = new BitVector();
      v.appendBits(0, 8);  // Append 00000000
      Encoder.terminateBits(1, v);
      assertEquals("00000000", v.toString());
    }
    {
      BitVector v = new BitVector();
      Encoder.terminateBits(2, v);
      assertEquals("0000000011101100", v.toString());
    }
    {
      BitVector v = new BitVector();
      v.appendBits(0, 1);  // Append 0
      Encoder.terminateBits(3, v);
      assertEquals("000000001110110000010001", v.toString());
    }
  }

  public void testGetNumDataBytesAndNumECBytesForBlockID() throws WriterException {
    int[] numDataBytes = new int[1];
    int[] numEcBytes = new int[1];
    // Version 1-H.
    Encoder.getNumDataBytesAndNumECBytesForBlockID(26, 9, 1, 0, numDataBytes, numEcBytes);
    assertEquals(9, numDataBytes[0]);
    assertEquals(17, numEcBytes[0]);

    // Version 3-H.  2 blocks.
    Encoder.getNumDataBytesAndNumECBytesForBlockID(70, 26, 2, 0, numDataBytes, numEcBytes);
    assertEquals(13, numDataBytes[0]);
    assertEquals(22, numEcBytes[0]);
    Encoder.getNumDataBytesAndNumECBytesForBlockID(70, 26, 2, 1, numDataBytes, numEcBytes);
    assertEquals(13, numDataBytes[0]);
    assertEquals(22, numEcBytes[0]);

    // Version 7-H. (4 + 1) blocks.
    Encoder.getNumDataBytesAndNumECBytesForBlockID(196, 66, 5, 0, numDataBytes, numEcBytes);
    assertEquals(13, numDataBytes[0]);
    assertEquals(26, numEcBytes[0]);
    Encoder.getNumDataBytesAndNumECBytesForBlockID(196, 66, 5, 4, numDataBytes, numEcBytes);
    assertEquals(14, numDataBytes[0]);
    assertEquals(26, numEcBytes[0]);

    // Version 40-H. (20 + 61) blocks.
    Encoder.getNumDataBytesAndNumECBytesForBlockID(3706, 1276, 81, 0, numDataBytes, numEcBytes);
    assertEquals(15, numDataBytes[0]);
    assertEquals(30, numEcBytes[0]);
    Encoder.getNumDataBytesAndNumECBytesForBlockID(3706, 1276, 81, 20, numDataBytes, numEcBytes);
    assertEquals(16, numDataBytes[0]);
    assertEquals(30, numEcBytes[0]);
    Encoder.getNumDataBytesAndNumECBytesForBlockID(3706, 1276, 81, 80, numDataBytes, numEcBytes);
    assertEquals(16, numDataBytes[0]);
    assertEquals(30, numEcBytes[0]);
  }

  public void testInterleaveWithECBytes() throws WriterException {
    {
      byte[] dataBytes = {32, 65, (byte)205, 69, 41, (byte)220, 46, (byte)128, (byte)236};
      BitVector in = new BitVector();
      for (byte dataByte: dataBytes) {
        in.appendBits(dataByte, 8);
      }
      BitVector out = new BitVector();
      Encoder.interleaveWithECBytes(in, 26, 9, 1, out);
      byte[] expected = {
          // Data bytes.
          32, 65, (byte)205, 69, 41, (byte)220, 46, (byte)128, (byte)236,
          // Error correction bytes.
          42, (byte)159, 74, (byte)221, (byte)244, (byte)169, (byte)239, (byte)150, (byte)138, 70,
          (byte)237, 85, (byte)224, 96, 74, (byte)219, 61,
      };
      assertEquals(expected.length, out.sizeInBytes());
      byte[] outArray = out.getArray();
      // Can't use Arrays.equals(), because outArray may be longer than out.sizeInBytes()
      for (int x = 0; x < expected.length; x++) {
        assertEquals(expected[x], outArray[x]);
      }
    }
    // Numbers are from http://www.swetake.com/qr/qr8.html
    {
      byte[] dataBytes = {
          67, 70, 22, 38, 54, 70, 86, 102, 118, (byte)134, (byte)150, (byte)166, (byte)182,
          (byte)198, (byte)214, (byte)230, (byte)247, 7, 23, 39, 55, 71, 87, 103, 119, (byte)135,
          (byte)151, (byte)166, 22, 38, 54, 70, 86, 102, 118, (byte)134, (byte)150, (byte)166,
          (byte)182, (byte)198, (byte)214, (byte)230, (byte)247, 7, 23, 39, 55, 71, 87, 103, 119,
          (byte)135, (byte)151, (byte)160, (byte)236, 17, (byte)236, 17, (byte)236, 17, (byte)236,
          17
      };
      BitVector in = new BitVector();
      for (byte dataByte: dataBytes) {
        in.appendBits(dataByte, 8);
      }
      BitVector out = new BitVector();
      Encoder.interleaveWithECBytes(in, 134, 62, 4, out);
      byte[] expected = {
          // Data bytes.
          67, (byte)230, 54, 55, 70, (byte)247, 70, 71, 22, 7, 86, 87, 38, 23, 102, 103, 54, 39,
          118, 119, 70, 55, (byte)134, (byte)135, 86, 71, (byte)150, (byte)151, 102, 87, (byte)166,
          (byte)160, 118, 103, (byte)182, (byte)236, (byte)134, 119, (byte)198, 17, (byte)150,
          (byte)135, (byte)214, (byte)236, (byte)166, (byte)151, (byte)230, 17, (byte)182,
          (byte)166, (byte)247, (byte)236, (byte)198, 22, 7, 17, (byte)214, 38, 23, (byte)236, 39,
          17,
          // Error correction bytes.
          (byte)175, (byte)155, (byte)245, (byte)236, 80, (byte)146, 56, 74, (byte)155, (byte)165,
          (byte)133, (byte)142, 64, (byte)183, (byte)132, 13, (byte)178, 54, (byte)132, 108, 45,
          113, 53, 50, (byte)214, 98, (byte)193, (byte)152, (byte)233, (byte)147, 50, 71, 65,
          (byte)190, 82, 51, (byte)209, (byte)199, (byte)171, 54, 12, 112, 57, 113, (byte)155, 117,
          (byte)211, (byte)164, 117, 30, (byte)158, (byte)225, 31, (byte)190, (byte)242, 38,
          (byte)140, 61, (byte)179, (byte)154, (byte)214, (byte)138, (byte)147, 87, 27, 96, 77, 47,
          (byte)187, 49, (byte)156, (byte)214,
      };
      assertEquals(expected.length, out.sizeInBytes());
      byte[] outArray = out.getArray();
      for (int x = 0; x < expected.length; x++) {
        assertEquals(expected[x], outArray[x]);
      }
    }
  }

  public void testAppendNumericBytes() {
    {
      // 1 = 01 = 0001 in 4 bits.
      BitVector bits = new BitVector();
      Encoder.appendNumericBytes("1", bits);
      assertEquals("0001" , bits.toString());
    }
    {
      // 12 = 0xc = 0001100 in 7 bits.
      BitVector bits = new BitVector();
      Encoder.appendNumericBytes("12", bits);
      assertEquals("0001100" , bits.toString());
    }
    {
      // 123 = 0x7b = 0001111011 in 10 bits.
      BitVector bits = new BitVector();
      Encoder.appendNumericBytes("123", bits);
      assertEquals("0001111011" , bits.toString());
    }
    {
      // 1234 = "123" + "4" = 0001111011 + 0100
      BitVector bits = new BitVector();
      Encoder.appendNumericBytes("1234", bits);
      assertEquals("0001111011" + "0100" , bits.toString());
    }
    {
      // Empty.
      BitVector bits = new BitVector();
      Encoder.appendNumericBytes("", bits);
      assertEquals("" , bits.toString());
    }
  }

  public void testAppendAlphanumericBytes() throws WriterException {
    {
      // A = 10 = 0xa = 001010 in 6 bits
      BitVector bits = new BitVector();
      Encoder.appendAlphanumericBytes("A", bits);
      assertEquals("001010" , bits.toString());
    }
    {
      // AB = 10 * 45 + 11 = 461 = 0x1cd = 00111001101 in 11 bits
      BitVector bits = new BitVector();
      Encoder.appendAlphanumericBytes("AB", bits);
      assertEquals("00111001101", bits.toString());
    }
    {
      // ABC = "AB" + "C" = 00111001101 + 001100
      BitVector bits = new BitVector();
      Encoder.appendAlphanumericBytes("ABC", bits);
      assertEquals("00111001101" + "001100" , bits.toString());
    }
    {
      // Empty.
      BitVector bits = new BitVector();
      Encoder.appendAlphanumericBytes("", bits);
      assertEquals("" , bits.toString());
    }
    {
      // Invalid data.
      BitVector bits = new BitVector();
      try {
        Encoder.appendAlphanumericBytes("abc", bits);
      } catch (WriterException we) {
        // good
      }
    }
  }

  public void testAppend8BitBytes() throws WriterException {
    {
      // 0x61, 0x62, 0x63
      BitVector bits = new BitVector();
      Encoder.append8BitBytes("abc", bits);
      assertEquals("01100001" + "01100010" + "01100011", bits.toString());
    }
    {
      // Empty.
      BitVector bits = new BitVector();
      Encoder.append8BitBytes("", bits);
      assertEquals("", bits.toString());
    }
  }

  // Numbers are from page 21 of JISX0510:2004
  public void testAppendKanjiBytes() throws WriterException {
      BitVector bits = new BitVector();
      Encoder.appendKanjiBytes(shiftJISString(new byte[] {(byte)0x93,0x5f}), bits);
      assertEquals("0110110011111", bits.toString());
      Encoder.appendKanjiBytes(shiftJISString(new byte[] {(byte)0xe4,(byte)0xaa}), bits);
      assertEquals("0110110011111" + "1101010101010", bits.toString());
  }

  // Numbers are from http://www.swetake.com/qr/qr3.html and
  // http://www.swetake.com/qr/qr9.html
  public void testGenerateECBytes() {
    {
      byte[] dataBytes = {32, 65, (byte)205, 69, 41, (byte)220, 46, (byte)128, (byte)236};
      ByteArray ecBytes = Encoder.generateECBytes(new ByteArray(dataBytes), 17);
      int[] expected = {
          42, 159, 74, 221, 244, 169, 239, 150, 138, 70, 237, 85, 224, 96, 74, 219, 61
      };
      assertEquals(expected.length, ecBytes.size());
      for (int x = 0; x < expected.length; x++) {
        assertEquals(expected[x], ecBytes.at(x));
      }
    }
    {
      byte[] dataBytes = {67, 70, 22, 38, 54, 70, 86, 102, 118,
          (byte)134, (byte)150, (byte)166, (byte)182, (byte)198, (byte)214};
      ByteArray ecBytes = Encoder.generateECBytes(new ByteArray(dataBytes), 18);
      int[] expected = {
          175, 80, 155, 64, 178, 45, 214, 233, 65, 209, 12, 155, 117, 31, 140, 214, 27, 187
      };
      assertEquals(expected.length, ecBytes.size());
      for (int x = 0; x < expected.length; x++) {
        assertEquals(expected[x], ecBytes.at(x));
      }
    }
    {
      // High-order zero cofficient case.
      byte[] dataBytes = {32, 49, (byte)205, 69, 42, 20, 0, (byte)236, 17};
      ByteArray ecBytes = Encoder.generateECBytes(new ByteArray(dataBytes), 17);
      int[] expected = {
          0, 3, 130, 179, 194, 0, 55, 211, 110, 79, 98, 72, 170, 96, 211, 137, 213
      };
      assertEquals(expected.length, ecBytes.size());
      for (int x = 0; x < expected.length; x++) {
        assertEquals(expected[x], ecBytes.at(x));
      }
    }
  }

  public void testBugInBitVectorNumBytes() throws WriterException {
    // There was a bug in BitVector.sizeInBytes() that caused it to return a
    // smaller-by-one value (ex. 1465 instead of 1466) if the number of bits
    // in the vector is not 8-bit aligned.  In QRCodeEncoder::InitQRCode(),
    // BitVector::sizeInBytes() is used for finding the smallest QR Code
    // version that can fit the given data.  Hence there were corner cases
    // where we chose a wrong QR Code version that cannot fit the given
    // data.  Note that the issue did not occur with MODE_8BIT_BYTE, as the
    // bits in the bit vector are always 8-bit aligned.
    //
    // Before the bug was fixed, the following test didn't pass, because:
    //
    // - MODE_NUMERIC is chosen as all bytes in the data are '0'
    // - The 3518-byte numeric data needs 1466 bytes
    //   - 3518 / 3 * 10 + 7 = 11727 bits = 1465.875 bytes
    //   - 3 numeric bytes are encoded in 10 bits, hence the first
    //     3516 bytes are encoded in 3516 / 3 * 10 = 11720 bits.
    //   - 2 numeric bytes can be encoded in 7 bits, hence the last
    //     2 bytes are encoded in 7 bits.
    // - The version 27 QR Code with the EC level L has 1468 bytes for data.
    //   - 1828 - 360 = 1468
    // - In InitQRCode(), 3 bytes are reserved for a header.  Hence 1465 bytes
    //   (1468 -3) are left for data.
    // - Because of the bug in BitVector::sizeInBytes(), InitQRCode() determines
    //   the given data can fit in 1465 bytes, despite it needs 1466 bytes.
    // - Hence QRCodeEncoder.encode() failed and returned false.
    //   - To be precise, it needs 11727 + 4 (getMode info) + 14 (length info) =
    //     11745 bits = 1468.125 bytes are needed (i.e. cannot fit in 1468
    //     bytes).
    StringBuilder builder = new StringBuilder(3518);
    for (int x = 0; x < 3518; x++) {
      builder.append('0');
    }
    QRCode qrCode = new QRCode();
    Encoder.encode(builder.toString(), ErrorCorrectionLevel.L, qrCode);
  }

  private static String shiftJISString(byte[] bytes) throws WriterException {
    try {
      return new String(bytes, "Shift_JIS");
    } catch (UnsupportedEncodingException uee) {
      throw new WriterException(uee.toString());
    }
  }

}
