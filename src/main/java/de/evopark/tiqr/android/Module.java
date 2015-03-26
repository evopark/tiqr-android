package de.evopark.tiqr.android;

import android.util.Log;
import de.evopark.tiqr.android.interfaces.QrCodeResultHandler;
import de.evopark.tiqr.android.processing.ZxingQrScanner;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

/**
 * Entry point for the QR scanner Titanium module
 * <p>
 * Licensed under the terms of the Apache Public License 2.0
 * Please see the LICENSE included with this distribution for details.
 */

@Kroll.module(name = ModuleInfo.MODULE_NAME, id = ModuleInfo.MODULE_ID)
public class Module extends KrollModule {

  private final static String LTAG = ModuleInfo.MODULE_NAME;
  private boolean scanning = false;
  private KrollFunction onCodeReceived = null;
  private ZxingQrScanner qrScanner = null;

  public Module() {
    super();
  }

  /**
   * Get the callback function that should be invoked on the JS-side when a code has been detected
   *
   * @return function previously set via {setOnCodeReceived}
   */
  @Kroll.method
  @Kroll.getProperty(name = "onCodeReceived")
  public KrollFunction getOnCodeReceived() {
    return onCodeReceived;
  }

  /**
   * Set a function that should be invoked when a code has been detected
   * The function will receive the detected code as a string as its single argument
   *
   * @param callback a KrollFunction that should be invoked when detecting codes
   */
  @Kroll.method
  @Kroll.setProperty(name = "onCodeReceived")
  public void setOnCodeReceived(KrollFunction callback) {
    onCodeReceived = callback;
  }

  /**
   * Allows reading the "scanning" property from JavaScript
   *
   * @return
   */
  @Kroll.method
  @Kroll.getProperty(name = "scanning")
  public boolean isScanning() {
    return scanning;
  }

  /**
   * Start or stop scanning by setting the modules "scanning" property to true or false respectively
   *
   * @param scanning whether the module should scan for codes
   */
  @Kroll.method
  @Kroll.setProperty(name = "scanning")
  public void setScanning(boolean scanning) {
    if (scanning == this.scanning) {
      return;
    }
    this.scanning = scanning;
    if (scanning) {
      Log.d(LTAG, "Starting scanner");
      if (qrScanner == null) {
        qrScanner = new ZxingQrScanner(new QrCodeCallbackInvoker());
        CameraManager.getInstance().addPreviewDataProcessor(qrScanner);
      }
      CameraManager.getInstance().startCapture();
    } else {
      Log.d(LTAG, "Stopping scanner");
      CameraManager.getInstance().stopCapture();
    }
  }

  /**
   * When a QR code has been found, invoke the Kroll callback
   * Does not invoke the callback if the same code is found multiple times
   */
  private class QrCodeCallbackInvoker implements QrCodeResultHandler {

    private String lastCode = null;

    @Override
    public void codeFound(String code) {
      if (onCodeReceived == null) {
        return;
      }
      // ignore the code if it was just reported
      if (code.equals(lastCode)) {
        return;
      }
      lastCode = code;
      Log.d(LTAG, "Invoking code reception handler");
      Object[] args = {code};
      onCodeReceived.callAsync(Module.this.getKrollObject(), args);
    }
  }
}
