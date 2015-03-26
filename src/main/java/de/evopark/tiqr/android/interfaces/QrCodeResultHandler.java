package de.evopark.tiqr.android.interfaces;

import de.evopark.tiqr.android.processing.ZxingQrScanner;

/**
 * Invoked when ZxingQrScanner finds a code
 *
 * @see ZxingQrScanner
 */
public interface QrCodeResultHandler {
  /**
   * Invoked when a QR code is found
   *
   * @param code the contents of the code as a String
   */
  void codeFound(String code);

}
