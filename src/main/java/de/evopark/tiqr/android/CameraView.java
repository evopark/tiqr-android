package de.evopark.tiqr.android;

import android.view.SurfaceView;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * A Titanium view that shows the current camera picture
 * Note that the camera preview needs to be started via the Module
 */
public class CameraView extends TiUIView {

  private SurfaceView surfaceView = null;

  /**
   * Default constructor
   * @see CameraViewProxy
   * @param proxy Kroll view proxy from Titanium
   */
  public CameraView(TiViewProxy proxy) {
    super(proxy);
    getLayoutParams().autoFillsHeight = true;
    getLayoutParams().autoFillsWidth = true;

    surfaceView = new SurfaceView(proxy.getActivity());
    CameraManager.getInstance().setPreviewSurfaceHolder(surfaceView.getHolder());
    setNativeView(surfaceView);
  }

  @Override
  public void release() {
    CameraManager.getInstance().setPreviewSurfaceHolder(null);
    super.release();
  }
}
