package de.evopark.tiqr.android;

import android.graphics.ImageFormat;
import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import de.evopark.tiqr.android.interfaces.QrCodeResultHandler;
import de.evopark.tiqr.android.interfaces.PreviewDataProcessor;

/**
 * Scans data received from the camera preview for QR codes using Zxing
 * Once a code has been recognized it is delivered to a ResultHandler as a string
 */
public class ZxingQrScanner extends PreviewDataProcessor {

  private final static String LTAG = ZxingQrScanner.class.getSimpleName();

  private QrCodeResultHandler resultHandler = null;
  private QRCodeReader qrCodeReader = null;

  public ZxingQrScanner(QrCodeResultHandler resultHandler) {
    if (resultHandler == null) {
      throw new IllegalArgumentException("Result handler must not be null");
    }
    this.resultHandler = resultHandler;
    qrCodeReader = new QRCodeReader();
  }

  @Override
  public void processBitmap(int width, int height, int format, byte[] data) {
    if (resultHandler == null) {
      return;
    }

    Result result = null;
    com.google.zxing.LuminanceSource luminanceSource = null;
    if (format == ImageFormat.YUY2 || format == ImageFormat.YV12) {
      luminanceSource = new PlanarYUVLuminanceSource(data,
          width, height, 0, 0, width, height, false);
    }
    if (luminanceSource != null) {
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
      try {
        result = qrCodeReader.decode(bitmap);
      } catch (NotFoundException e) {
        return;
      } catch (Exception e) {
        Log.e(LTAG, "Exception when scanning for code", e);
        return;
      }
    } else {
      Log.e(LTAG, String.format("Could not handle image format %d", format));
    }
    if (result != null) {
      String resultData = result.getText();
      resultHandler.codeFound(resultData);
    }
  }
}
