package de.evopark.tiqr.android.processing;

import android.graphics.ImageFormat;
import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import de.evopark.tiqr.android.interfaces.PreviewDataProcessor;
import de.evopark.tiqr.android.interfaces.QrCodeResultHandler;

/**
 * Scans data received from the camera preview for QR codes using Zxing
 * Once a code has been recognized it is delivered to a ResultHandler as a string
 */
public class ZxingQrScanner implements PreviewDataProcessor {

  private final static String LTAG = ZxingQrScanner.class.getSimpleName();

  private final QrCodeResultHandler resultHandler;
  private final QRCodeReader qrCodeReader;

  public ZxingQrScanner(QrCodeResultHandler resultHandler) {
    if (resultHandler == null) {
      throw new IllegalArgumentException("Result handler must not be null");
    }
    this.resultHandler = resultHandler;
    qrCodeReader = new QRCodeReader();
  }

  /**
   * Checks for a QR code in the given bitmap data
   *
   * @param width  width of the picture in {data} in pixels
   * @param height height of the picture in {data} in pixels
   * @param format format of the data in {data}
   * @param data   image data buffer
   */
  @Override
  public void processBitmap(int width, int height, int format, byte[] data) {
    if (resultHandler == null) {
      return;
    }

    Result result = null;
    com.google.zxing.LuminanceSource luminanceSource = null;
    if (format == ImageFormat.YUY2 || format == ImageFormat.YV12 ||
        format == ImageFormat.NV21 || format == ImageFormat.NV16) {
      luminanceSource = new PlanarYUVLuminanceSource(data,
          width, height, 0, 0, width, height, false);
    }
    if (luminanceSource != null) {
      final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
      try {
        result = qrCodeReader.decode(bitmap);
      } catch (NotFoundException e) {
        return;
      } catch(com.google.zxing.FormatException e) {
        // happens for example when the picture is completely black
        return;
      } catch (com.google.zxing.ChecksumException e) {
        // happens when the picture is blurry so the code can't be recognized properly
        return;
      } catch (Exception e) {
        Log.e(LTAG, "Exception when scanning for code", e);
        return;
      }
    } else {
      Log.e(LTAG, String.format("Could not handle image format %d", format));
    }
    if (result != null) {
      final String resultData = result.getText();
      resultHandler.codeFound(resultData);
    }
  }
}
