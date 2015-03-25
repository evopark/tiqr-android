package de.evopark.tiqr.android;

import de.evopark.tiqr.android.interfaces.QrCodeResultHandler;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

/**
 * Entry point for the Titanium module
 *
 * Licensed under the terms of the Apache Public License 2.0
 * Please see the LICENSE included with this distribution for details.
 */

@Kroll.module(name="tiqr", id=ModuleInfo.MODULE_ID)
public class Module extends KrollModule {

  private boolean isScanning = false;
  private KrollFunction onCodeReceived = null;

  public Module() {
    super();
  }

  /**
   * Get the callback function that should be invoked on the JS-side when a code has been detected
   * @return function previously set via {setOnCodeReceived}
   */
  @Kroll.getProperty
  public KrollFunction getOnCodeReceived() {
    return onCodeReceived;
  }

  /**
   * Set a function that should be invoked when a code has been detected
   * The function will receive the detected code as a string as its single argument
   * @param callback a KrollFunction that should be invoked when detecting codes
   */
  @Kroll.setProperty
  public void setOnCodeReceived(KrollFunction callback) {
    onCodeReceived = callback;
  }

  /**
   * Allows reading the "scanning" property from JavaScript
   * @return
   */
  @Kroll.getProperty
  public boolean getIsScanning() {
    return isScanning;
  }

  /**
   * Start or stop scanning by setting the modules "scanning" property to true or false respectively
   * @param scanning whether the module should scan for codes
   */
  @Kroll.setProperty()
  public void setIsScanning(boolean scanning) {
    if (scanning == isScanning) {
      return;
    }
    isScanning = scanning;
    if (isScanning) {
      if (qrScanner == null) {
        qrScanner = new ZxingQrScanner(new QrCodeCallbackInvoker());
        CameraManager.getInstance().addPreviewDataProcessor(qrScanner);
      }
      CameraManager.getInstance().startCapture();
    } else {
      CameraManager.getInstance().stopCapture();
    }
  }

  /**
   * When a QR code has been found, invokes a Kroll callback
   */
  private class QrCodeCallbackInvoker extends QrCodeResultHandler {
    @Override
    public void codeFound(String code) {
      if (onCodeReceived == null) {
        return;
      }
      Object[] args = {code};
      onCodeReceived.callAsync(Module.this.getKrollObject(), args);
    }
  }

  ZxingQrScanner qrScanner = null;
}
