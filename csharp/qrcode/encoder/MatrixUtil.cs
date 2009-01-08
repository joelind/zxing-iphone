﻿/*
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
using System;
using System.Text;
using com.google.zxing;
using com.google.zxing.common;
using com.google.zxing.qrcode.decoder;
using com.google.zxing.qrcode;

namespace com.google.zxing.qrcode.encoder
{
    public sealed class MatrixUtil 
    { 
          private MatrixUtil() {
            // do nothing
          }

         private static int[][] POSITION_DETECTION_PATTERN =  new int[][]{
              new int[]{1, 1, 1, 1, 1, 1, 1},
              new int[]{1, 0, 0, 0, 0, 0, 1},
              new int[]{1, 0, 1, 1, 1, 0, 1},
              new int[]{1, 0, 1, 1, 1, 0, 1},
              new int[]{1, 0, 1, 1, 1, 0, 1},
              new int[]{1, 0, 0, 0, 0, 0, 1},
              new int[]{1, 1, 1, 1, 1, 1, 1},
          };

          private static  int[][] HORIZONTAL_SEPARATION_PATTERN = new int[][]{
              new int[]{0, 0, 0, 0, 0, 0, 0, 0},
          };

          private static int[][] VERTICAL_SEPARATION_PATTERN = new int[][]{
              new int[]{0}, new int[]{0}, new int[]{0}, new int[]{0}, new int[]{0}, new int[]{0}, new int[]{0},
          };

          private static int[][] POSITION_ADJUSTMENT_PATTERN = new int[][]{
              new int[]{1, 1, 1, 1, 1},
              new int[]{1, 0, 0, 0, 1},
              new int[]{1, 0, 1, 0, 1},
              new int[]{1, 0, 0, 0, 1},
              new int[]{1, 1, 1, 1, 1},
          };

          // From Appendix E. Table 1, JIS0510X:2004 (p 71). The table was double-checked by komatsu.
          private static int[][] POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE = new int[][]{
              new int[]{-1, -1, -1, -1,  -1,  -1,  -1},  // Version 1
              new int[]{ 6, 18, -1, -1,  -1,  -1,  -1},  // Version 2
              new int[]{ 6, 22, -1, -1,  -1,  -1,  -1},  // Version 3
              new int[]{ 6, 26, -1, -1,  -1,  -1,  -1},  // Version 4
              new int[]{ 6, 30, -1, -1,  -1,  -1,  -1},  // Version 5
              new int[]{ 6, 34, -1, -1,  -1,  -1,  -1},  // Version 6
              new int[]{ 6, 22, 38, -1,  -1,  -1,  -1},  // Version 7
              new int[]{ 6, 24, 42, -1,  -1,  -1,  -1},  // Version 8
              new int[]{ 6, 26, 46, -1,  -1,  -1,  -1},  // Version 9
              new int[]{ 6, 28, 50, -1,  -1,  -1,  -1},  // Version 10
              new int[]{ 6, 30, 54, -1,  -1,  -1,  -1},  // Version 11
              new int[]{ 6, 32, 58, -1,  -1,  -1,  -1},  // Version 12
              new int[]{ 6, 34, 62, -1,  -1,  -1,  -1},  // Version 13
              new int[]{ 6, 26, 46, 66,  -1,  -1,  -1},  // Version 14
              new int[]{ 6, 26, 48, 70,  -1,  -1,  -1},  // Version 15
              new int[]{ 6, 26, 50, 74,  -1,  -1,  -1},  // Version 16
              new int[]{ 6, 30, 54, 78,  -1,  -1,  -1},  // Version 17
              new int[]{ 6, 30, 56, 82,  -1,  -1,  -1},  // Version 18
              new int[]{ 6, 30, 58, 86,  -1,  -1,  -1},  // Version 19
              new int[]{ 6, 34, 62, 90,  -1,  -1,  -1},  // Version 20
              new int[]{ 6, 28, 50, 72,  94,  -1,  -1},  // Version 21
              new int[]{ 6, 26, 50, 74,  98,  -1,  -1},  // Version 22
              new int[]{ 6, 30, 54, 78, 102,  -1,  -1},  // Version 23
              new int[]{ 6, 28, 54, 80, 106,  -1,  -1},  // Version 24
              new int[]{ 6, 32, 58, 84, 110,  -1,  -1},  // Version 25
              new int[]{ 6, 30, 58, 86, 114,  -1,  -1},  // Version 26
              new int[]{ 6, 34, 62, 90, 118,  -1,  -1},  // Version 27
              new int[]{ 6, 26, 50, 74,  98, 122,  -1},  // Version 28
              new int[]{ 6, 30, 54, 78, 102, 126,  -1},  // Version 29
              new int[]{ 6, 26, 52, 78, 104, 130,  -1},  // Version 30
              new int[]{ 6, 30, 56, 82, 108, 134,  -1},  // Version 31
              new int[]{ 6, 34, 60, 86, 112, 138,  -1},  // Version 32
              new int[]{ 6, 30, 58, 86, 114, 142,  -1},  // Version 33
              new int[]{ 6, 34, 62, 90, 118, 146,  -1},  // Version 34
              new int[]{ 6, 30, 54, 78, 102, 126, 150},  // Version 35
              new int[]{ 6, 24, 50, 76, 102, 128, 154},  // Version 36
              new int[]{ 6, 28, 54, 80, 106, 132, 158},  // Version 37
              new int[]{ 6, 32, 58, 84, 110, 136, 162},  // Version 38
              new int[]{ 6, 26, 54, 82, 110, 138, 166},  // Version 39
              new int[]{ 6, 30, 58, 86, 114, 142, 170},  // Version 40
          };

          // Type info cells at the left top corner.
          private static int[][] TYPE_INFO_COORDINATES = new int[][]{
              new int[]{8, 0},
              new int[]{8, 1},
              new int[]{8, 2},
              new int[]{8, 3},
              new int[]{8, 4},
              new int[]{8, 5},
              new int[]{8, 7},
              new int[]{8, 8},
              new int[]{7, 8},
              new int[]{5, 8},
              new int[]{4, 8},
              new int[]{3, 8},
              new int[]{2, 8},
              new int[]{1, 8},
              new int[]{0, 8},
          };

          // From Appendix D in JISX0510:2004 (p. 67)
          private static  int VERSION_INFO_POLY = 0x1f25;  // 1 1111 0010 0101

          // From Appendix C in JISX0510:2004 (p.65).
          private static  int TYPE_INFO_POLY = 0x537;
          private static  int TYPE_INFO_MASK_PATTERN = 0x5412;

          // Set all cells to -1.  -1 means that the cell is empty (not set yet).
          //
          // JAVAPORT: We shouldn't need to do this at all. The code should be rewritten to begin encoding
          // with the ByteMatrix initialized all to zero.
          public static void clearMatrix(ByteMatrix matrix) {
              matrix.clear((sbyte)(-1));
          }

          // Build 2D matrix of QR Code from "dataBits" with "ecLevel", "version" and "getMaskPattern". On
          // success, store the result in "matrix" and return true.
          public static void buildMatrix(BitVector dataBits, ErrorCorrectionLevel ecLevel, int version,int maskPattern, ByteMatrix matrix) {
               try{
                    clearMatrix(matrix);
                    embedBasicPatterns(version, matrix);
                    // Type information appear with any version.
                    embedTypeInfo(ecLevel, maskPattern, matrix);
                    // Version info appear if version >= 7.
                    maybeEmbedVersionInfo(version, matrix);
                    // Data should be embedded at end.
                    embedDataBits(dataBits, maskPattern, matrix);
               }catch(Exception e){
                    throw  new WriterException(e.Message);
               }
              
          }

          // Embed basic patterns. On success, modify the matrix and return true.
          // The basic patterns are:
          // - Position detection patterns
          // - Timing patterns
          // - Dark dot at the left bottom corner
          // - Position adjustment patterns, if need be
          public static void embedBasicPatterns(int version, ByteMatrix matrix){
              try
              {
                    // Let's get started with embedding big squares at corners.
                    embedPositionDetectionPatternsAndSeparators(matrix);
                    // Then, embed the dark dot at the left bottom corner.
                    embedDarkDotAtLeftBottomCorner(matrix);

                    // Position adjustment patterns appear if version >= 2.
                    maybeEmbedPositionAdjustmentPatterns(version, matrix);
                    // Timing patterns should be embedded after position adj. patterns.
                    embedTimingPatterns(matrix);
              }catch(Exception e){
                throw new WriterException (e.Message);
              }
          }

          // Embed type information. On success, modify the matrix.
          public static void embedTypeInfo(ErrorCorrectionLevel ecLevel, int maskPattern, ByteMatrix matrix)
          {
            BitVector typeInfoBits = new BitVector();
            makeTypeInfoBits(ecLevel, maskPattern, typeInfoBits);

            for (int i = 0; i < typeInfoBits.size(); ++i) {
              // Place bits in LSB to MSB order.  LSB (least significant bit) is the last value in
              // "typeInfoBits".
              int bit = typeInfoBits.at(typeInfoBits.size() - 1 - i);

              // Type info bits at the left top corner. See 8.9 of JISX0510:2004 (p.46).
              int x1 = TYPE_INFO_COORDINATES[i][0];
              int y1 = TYPE_INFO_COORDINATES[i][1];
              matrix.set(y1, x1, bit);

              if (i < 8) {
                // Right top corner.
                int x2 = matrix.width() - i - 1;
                int y2 = 8;
                matrix.set(y2, x2, bit);
              } else {
                // Left bottom corner.
                int x2 = 8;
                int y2 = matrix.height() - 7 + (i - 8);
                matrix.set(y2, x2, bit);
              }
            }
          }

          // Embed version information if need be. On success, modify the matrix and return true.
          // See 8.10 of JISX0510:2004 (p.47) for how to embed version information.
          public static void maybeEmbedVersionInfo(int version, ByteMatrix matrix){
            if (version < 7) {  // Version info is necessary if version >= 7.
              return;  // Don't need version info.
            }
            BitVector versionInfoBits = new BitVector();
            makeVersionInfoBits(version, versionInfoBits);

            int bitIndex = 6 * 3 - 1;  // It will decrease from 17 to 0.
            for (int i = 0; i < 6; ++i) {
              for (int j = 0; j < 3; ++j) {
                // Place bits in LSB (least significant bit) to MSB order.
                int bit = versionInfoBits.at(bitIndex);
                bitIndex--;
                // Left bottom corner.
                matrix.set(matrix.height() - 11 + j, i, bit);
                // Right bottom corner.
                matrix.set(i, matrix.height() - 11 + j, bit);
              }
            }
          }

          // Embed "dataBits" using "getMaskPattern". On success, modify the matrix and return true.
          // For debugging purposes, it skips masking process if "getMaskPattern" is -1.
          // See 8.7 of JISX0510:2004 (p.38) for how to embed data bits.
          public static void embedDataBits(BitVector dataBits, int maskPattern, ByteMatrix matrix)
          {
            int bitIndex = 0;
            int direction = -1;
            // Start from the right bottom cell.
            int x = matrix.width() - 1;
            int y = matrix.height() - 1;
            while (x > 0) {
              // Skip the vertical timing pattern.
              if (x == 6) {
                x -= 1;
              }
              while (y >= 0 && y < matrix.height()) {
                for (int i = 0; i < 2; ++i) {
                  int xx = x - i;
                  // Skip the cell if it's not empty.
                  if (!isEmpty(matrix.get(y, xx))) {
                    continue;
                  }
                  int bit;
                  if (bitIndex < dataBits.size()) {
                    bit = dataBits.at(bitIndex);
                    ++bitIndex;
                  } else {
                    // Padding bit. If there is no bit left, we'll fill the left cells with 0, as described
                    // in 8.4.9 of JISX0510:2004 (p. 24).
                    bit = 0;
                  }

                  // Skip masking if mask_pattern is -1.
                  if (maskPattern != -1) {
                    int mask = MaskUtil.getDataMaskBit(maskPattern, xx, y);
                    bit ^= mask;
                  }
                  matrix.set(y, xx, bit);
                }
                y += direction;
              }
              direction = -direction;  // Reverse the direction.
              y += direction;
              x -= 2;  // Move to the left.
            }
            // All bits should be consumed.
            if (bitIndex != dataBits.size()) {
              throw new WriterException("Not all bits consumed: " + bitIndex + '/' + dataBits.size());
            }
          }

          // Return the position of the most significant bit set (to one) in the "value". The most
          // significant bit is position 32. If there is no bit set, return 0. Examples:
          // - findMSBSet(0) => 0
          // - findMSBSet(1) => 1
          // - findMSBSet(255) => 8
          public static int findMSBSet(int value) {
            int numDigits = 0;
            while (value != 0) {
              value >>= 1;
              ++numDigits;
            }
            return numDigits;
          }

          // Calculate BCH (Bose-Chaudhuri-Hocquenghem) code for "value" using polynomial "poly". The BCH
          // code is used for encoding type information and version information.
          // Example: Calculation of version information of 7.
          // f(x) is created from 7.
          //   - 7 = 000111 in 6 bits
          //   - f(x) = x^2 + x^2 + x^1
          // g(x) is given by the standard (p. 67)
          //   - g(x) = x^12 + x^11 + x^10 + x^9 + x^8 + x^5 + x^2 + 1
          // Multiply f(x) by x^(18 - 6)
          //   - f'(x) = f(x) * x^(18 - 6)
          //   - f'(x) = x^14 + x^13 + x^12
          // Calculate the remainder of f'(x) / g(x)
          //         x^2
          //         __________________________________________________
          //   g(x) )x^14 + x^13 + x^12
          //         x^14 + x^13 + x^12 + x^11 + x^10 + x^7 + x^4 + x^2
          //         --------------------------------------------------
          //                              x^11 + x^10 + x^7 + x^4 + x^2
          //
          // The remainder is x^11 + x^10 + x^7 + x^4 + x^2
          // Encode it in binary: 110010010100
          // The return value is 0xc94 (1100 1001 0100)
          //
          // Since all coefficients in the polynomials are 1 or 0, we can do the calculation by bit
          // operations. We don't care if cofficients are positive or negative.
          public static int calculateBCHCode(int value, int poly) {
            // If poly is "1 1111 0010 0101" (version info poly), msbSetInPoly is 13. We'll subtract 1
            // from 13 to make it 12.
            int msbSetInPoly = findMSBSet(poly);
            value <<= msbSetInPoly - 1;
            // Do the division business using exclusive-or operations.
            while (findMSBSet(value) >= msbSetInPoly) {
              value ^= poly << (findMSBSet(value) - msbSetInPoly);
            }
            // Now the "value" is the remainder (i.e. the BCH code)
            return value;
          }

          // Make bit vector of type information. On success, store the result in "bits" and return true.
          // Encode error correction level and mask pattern. See 8.9 of
          // JISX0510:2004 (p.45) for details.
          public static void makeTypeInfoBits(ErrorCorrectionLevel ecLevel, int maskPattern, BitVector bits)
          {
            if (!QRCode.isValidMaskPattern(maskPattern)) {
              throw new WriterException("Invalid mask pattern");
            }
            int typeInfo = (ecLevel.getBits() << 3) | maskPattern;
            bits.appendBits(typeInfo, 5);

            int bchCode = calculateBCHCode(typeInfo, TYPE_INFO_POLY);
            bits.appendBits(bchCode, 10);

            BitVector maskBits = new BitVector();
            maskBits.appendBits(TYPE_INFO_MASK_PATTERN, 15);
            bits.xor(maskBits);

            if (bits.size() != 15) {  // Just in case.
              throw new WriterException("should not happen but we got: " + bits.size());
            }
          }

          // Make bit vector of version information. On success, store the result in "bits" and return true.
          // See 8.10 of JISX0510:2004 (p.45) for details.
          public static void makeVersionInfoBits(int version, BitVector bits){
            bits.appendBits(version, 6);
            int bchCode = calculateBCHCode(version, VERSION_INFO_POLY);
            bits.appendBits(bchCode, 12);

            if (bits.size() != 18) {  // Just in case.
              throw new WriterException("should not happen but we got: " + bits.size());
            }
          }

          // Check if "value" is empty.
          private static bool isEmpty(int value) {
            return value == -1;
          }

          // Check if "value" is valid.
          private static bool isValidValue(int value) {
            return (value == -1 ||  // Empty.
                value == 0 ||  // Light (white).
                value == 1);  // Dark (black).
          }

          private static void embedTimingPatterns(ByteMatrix matrix) {
            // -8 is for skipping position detection patterns (size 7), and two horizontal/vertical
            // separation patterns (size 1). Thus, 8 = 7 + 1.
            for (int i = 8; i < matrix.width() - 8; ++i) {
              int bit = (i + 1) % 2;
              // Horizontal line.
              if (!isValidValue(matrix.get(6, i))) {
                throw new WriterException();
              }
              if (isEmpty(matrix.get(6, i))) {
                matrix.set(6, i, bit);
              }
              // Vertical line.
              if (!isValidValue(matrix.get(i, 6))) {
                throw new WriterException();
              }
              if (isEmpty(matrix.get(i, 6))) {
                matrix.set(i, 6, bit);
              }
            }
          }

          // Embed the lonely dark dot at left bottom corner. JISX0510:2004 (p.46)
          private static void embedDarkDotAtLeftBottomCorner(ByteMatrix matrix){
            if (matrix.get(matrix.height() - 8, 8) == 0) {
              throw new WriterException();
            }
            matrix.set(matrix.height() - 8, 8, 1);
          }

          private static void embedHorizontalSeparationPattern(int xStart, int yStart,ByteMatrix matrix) {
            // We know the width and height.
            if (HORIZONTAL_SEPARATION_PATTERN[0].Length != 8 || HORIZONTAL_SEPARATION_PATTERN.Length != 1) {
              throw new WriterException("Bad horizontal separation pattern");
            }
            for (int x = 0; x < 8; ++x) {
              if (!isEmpty(matrix.get(yStart, xStart + x))) {
                throw new WriterException();
              }
              matrix.set(yStart, xStart + x, HORIZONTAL_SEPARATION_PATTERN[0][x]);
            }
          }

          private static void embedVerticalSeparationPattern(int xStart, int yStart,ByteMatrix matrix){
            // We know the width and height.
            if (VERTICAL_SEPARATION_PATTERN[0].Length != 1 || VERTICAL_SEPARATION_PATTERN.Length != 7) {
              throw new WriterException("Bad vertical separation pattern");
            }
            for (int y = 0; y < 7; ++y) {
              if (!isEmpty(matrix.get(yStart + y, xStart))) {
                throw new WriterException();
              }
              matrix.set(yStart + y, xStart, VERTICAL_SEPARATION_PATTERN[y][0]);
            }
          }

          // Note that we cannot unify the function with embedPositionDetectionPattern() despite they are
          // almost identical, since we cannot write a function that takes 2D arrays in different sizes in
          // C/C++. We should live with the fact.
          private static void embedPositionAdjustmentPattern(int xStart, int yStart,ByteMatrix matrix){
            // We know the width and height.
            if (POSITION_ADJUSTMENT_PATTERN[0].Length != 5 || POSITION_ADJUSTMENT_PATTERN.Length != 5) {
              throw new WriterException("Bad position adjustment");
            }
            for (int y = 0; y < 5; ++y) {
              for (int x = 0; x < 5; ++x) {
                if (!isEmpty(matrix.get(yStart + y, xStart + x))) {
                  throw new WriterException();
                }
                matrix.set(yStart + y, xStart + x, POSITION_ADJUSTMENT_PATTERN[y][x]);
              }
            }
          }

          private static void embedPositionDetectionPattern(int xStart, int yStart,ByteMatrix matrix){
            // We know the width and height.
            if (POSITION_DETECTION_PATTERN[0].Length != 7 || POSITION_DETECTION_PATTERN.Length != 7) {
              throw new WriterException("Bad position detection pattern");
            }
            for (int y = 0; y < 7; ++y) {
              for (int x = 0; x < 7; ++x) {
                if (!isEmpty(matrix.get(yStart + y, xStart + x))) {
                  throw new WriterException();
                }
                matrix.set(yStart + y, xStart + x, POSITION_DETECTION_PATTERN[y][x]);
              }
            }
          }

          // Embed position detection patterns and surrounding vertical/horizontal separators.
          private static void embedPositionDetectionPatternsAndSeparators(ByteMatrix matrix) {
            // Embed three big squares at corners.
            int pdpWidth = POSITION_DETECTION_PATTERN[0].Length;
            // Left top corner.
            embedPositionDetectionPattern(0, 0, matrix);
            // Right top corner.
            embedPositionDetectionPattern(matrix.width() - pdpWidth, 0, matrix);
            // Left bottom corner.
            embedPositionDetectionPattern(0, matrix.width() - pdpWidth, matrix);

            // Embed horizontal separation patterns around the squares.
            int hspWidth = HORIZONTAL_SEPARATION_PATTERN[0].Length;
            // Left top corner.
            embedHorizontalSeparationPattern(0, hspWidth - 1, matrix);
            // Right top corner.
            embedHorizontalSeparationPattern(matrix.width() - hspWidth,
                hspWidth - 1, matrix);
            // Left bottom corner.
            embedHorizontalSeparationPattern(0, matrix.width() - hspWidth, matrix);

            // Embed vertical separation patterns around the squares.
            int vspSize = VERTICAL_SEPARATION_PATTERN.Length;
            // Left top corner.
            embedVerticalSeparationPattern(vspSize, 0, matrix);
            // Right top corner.
            embedVerticalSeparationPattern(matrix.height() - vspSize - 1, 0, matrix);
            // Left bottom corner.
            embedVerticalSeparationPattern(vspSize, matrix.height() - vspSize,
                matrix);
          }

          // Embed position adjustment patterns if need be.
          private static void maybeEmbedPositionAdjustmentPatterns(int version, ByteMatrix matrix)
          {
            if (version < 2) {  // The patterns appear if version >= 2
              return;
            }
            int index = version - 1;
            int[] coordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index];
            int numCoordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index].Length;
            for (int i = 0; i < numCoordinates; ++i) {
              for (int j = 0; j < numCoordinates; ++j) {
                int y = coordinates[i];
                int x = coordinates[j];
                if (x == -1 || y == -1) {
                  continue;
                }
                // If the cell is unset, we embed the position adjustment pattern here.
                if (isEmpty(matrix.get(y, x))) {
                  // -2 is necessary since the x/y coordinates point to the center of the pattern, not the
                  // left top corner.
                  embedPositionAdjustmentPattern(x - 2, y - 2, matrix);
                }
              }
            }
          }

    
    
    }


}