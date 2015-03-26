package de.evopark.tiqr.android.ui;

import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.SurfaceView;
import de.evopark.tiqr.android.CameraManager;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

/**
 * A Titanium view that shows the current camera picture
 * Note that the camera preview needs to be started via the Module
 * otherwise the view will not update
 * Also note that setting up a camera view will change the size of the
 * preview to produce the best visual result and avoid distortion
 */
public class CameraView extends TiUIView {

  private final static String LTAG = CameraView.class.getSimpleName();

  private final SurfaceView surfaceView;

  /**
   * Default constructor
   *
   * @param proxy Kroll view proxy from Titanium
   * @see CameraViewProxy
   */
  public CameraView(TiViewProxy proxy) {
    super(proxy);

    Log.d(LTAG, "Instantiating camera surface view");
    surfaceView = new SurfaceView(proxy.getActivity());
    CameraManager.getInstance().showPreviewInSurface(surfaceView.getHolder(), proxy.getActivity());
    setNativeView(surfaceView);
  }
}
