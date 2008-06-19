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

package com.google.zxing.oned;

import com.google.zxing.DecodeHintType;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitArray;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author dswitkin@google.com (Daniel Switkin)
 * @author srowen@google.com (Sean Owen)
 */
public final class MultiFormatOneDReader extends AbstractOneDReader {

  public Result decodeRow(int rowNumber, BitArray row, Hashtable hints) throws ReaderException {

    Vector possibleFormats = hints == null ? null : (Vector) hints.get(DecodeHintType.POSSIBLE_FORMATS);
    Vector readers = new Vector();
    if (possibleFormats != null) {
      if (possibleFormats.contains(BarcodeFormat.EAN_13) ||
          possibleFormats.contains(BarcodeFormat.UPC_A) ||
          possibleFormats.contains(BarcodeFormat.EAN_8) ||
          possibleFormats.contains(BarcodeFormat.UPC_E)) {
        readers.addElement(new MultiFormatUPCEANReader());
      }
      if (possibleFormats.contains(BarcodeFormat.CODE_39)) {
        readers.addElement(new Code39Reader());
      }
      if (possibleFormats.contains(BarcodeFormat.CODE_128)) {
        readers.addElement(new Code128Reader());
      }
    }
    if (readers.isEmpty()) {
      readers.addElement(new MultiFormatUPCEANReader());
      readers.addElement(new Code39Reader());
      readers.addElement(new Code128Reader());
    }

    for (int i = 0; i < readers.size(); i++) {
      OneDReader reader = (OneDReader) readers.elementAt(i);
      try {
        return reader.decodeRow(rowNumber, row, hints);
      } catch (ReaderException re) {
        // continue
      }
    }

    throw new ReaderException("No barcode was detected in this image.");
  }

}