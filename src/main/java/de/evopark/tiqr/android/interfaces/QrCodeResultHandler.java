package de.evopark.tiqr.android.interfaces;

import de.evopark.tiqr.android.ZxingQrScanner;

/**
 * Invoked when ZxingQrScanner finds a code
 * @see ZxingQrScanner
 */
public abstract class QrCodeResultHandler {
  /**
   * Invoked when a QR code is found
   * @param code the contents of the code as a String
   */
  public abstract void codeFound(String code);

}
