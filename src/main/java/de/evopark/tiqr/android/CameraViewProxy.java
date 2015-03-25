package de.evopark.tiqr.android;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * A Kroll proxy for the CameraView class
 */
@Kroll.proxy(creatableInModule = Module.class)
public class CameraViewProxy extends TiViewProxy {
  @Override
  public TiUIView createView(android.app.Activity activity) {
    TiUIView view = new CameraView(this);
    return view;
  }


}
