/*
 * Copyright (C) 2008 ZXing authors
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

package com.google.zxing.client.androidtest;

import android.os.Message;
import android.util.Log;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Vector;

final class BenchmarkThread extends Thread {

  private static final String TAG = "BenchmarkThread";
  private static final int RUNS = 10;

  private BenchmarkActivity mActivity;
  private String mPath;
  private MultiFormatReader mMultiFormatReader;

  BenchmarkThread(BenchmarkActivity activity, String path) {
    mActivity = activity;
    mPath = path;
  }

  @Override
  public void run() {
    mMultiFormatReader = new MultiFormatReader();
    mMultiFormatReader.setHints(null);

    Vector<BenchmarkItem> items = new Vector<BenchmarkItem>();
    walkTree(mPath, items);
    Message message = Message.obtain(mActivity.mHandler, R.id.benchmark_done);
    message.obj = items;
    message.sendToTarget();
  }

  // Recurse to allow subdirectories
  private void walkTree(String path, Vector<BenchmarkItem> items) {
    File file = new File(path);
    if (file.isDirectory()) {
      String[] files = file.list();
      for (int x = 0; x < files.length; x++) {
        walkTree(file.getAbsolutePath() + "/" + files[x], items);
      }
    } else {
      BenchmarkItem item = decode(path);
      if (item != null) {
        items.addElement(item);
      }
    }
  }

  private BenchmarkItem decode(String path) {
    RGBMonochromeBitmapSource source = null;
    try {
      source = new RGBMonochromeBitmapSource(path);
    } catch (FileNotFoundException e) {
      Log.e(TAG, e.toString());
      return null;
    }

    BenchmarkItem item = new BenchmarkItem(path, RUNS);
    for (int x = 0; x < RUNS; x++) {
      Date startDate = new Date();
      boolean success;
      Result result = null;
      try {
        result = mMultiFormatReader.decodeWithState(source);
        success = true;
      } catch (ReaderException e) {
        success = false;
      }
      Date endDate = new Date();
      if (x == 0) {
        item.setDecoded(success);
        item.setFormat(result != null ? result.getBarcodeFormat() : null);
      }
      item.addResult((int) (endDate.getTime() - startDate.getTime()));
    }
    return item;
  }

}