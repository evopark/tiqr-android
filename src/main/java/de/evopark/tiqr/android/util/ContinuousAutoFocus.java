package de.evopark.tiqr.android.util;

import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;

/**
 * An auto-focus callback that simulates continuous auto-focusing by re-focusing
 * the camera every second.
 */
public class ContinuousAutoFocus implements Camera.AutoFocusCallback {

  private final static String LTAG = ContinuousAutoFocus.class.getSimpleName();

  private final static int FOCUS_DELAY = 1000;

  private boolean stopped = true;
  private final Camera camera;
  private Handler autoFocusHandler = new Handler();

  /**
   * Runs auto-focusing and catches exceptions
   */
  private Runnable doAutoFocus = new Runnable() {
    public void run() {
      if (stopped) {
        return;
      }
      try {
        camera.autoFocus(ContinuousAutoFocus.this);
      } catch (RuntimeException e) {
        Log.d(LTAG, "Auto-focus failed, retrying", e);
        ContinuousAutoFocus.this.onAutoFocus(false, camera);
      }
    }
  };

  /**
   * Set up continuous auto-focus for the given camera
   *
   * @param camera the camera which should be auto-focused
   */
  public ContinuousAutoFocus(Camera camera) {
    if (camera == null) {
      throw new IllegalArgumentException("Camera must not be null");
    }
    this.camera = camera;
    start();
  }

  /**
   * Stops auto-focusing the camera
   */
  public void stop() {
    stopped = true;
    autoFocusHandler.removeCallbacks(doAutoFocus);
  }

  /**
   * Starts auto-focusing the camera
   */
  public void start() {
    if (stopped == false) {
      return;
    }
    Log.d(LTAG, "Starting auto-focus");
    stopped = false;
    onAutoFocus(true, camera);
  }

  /**
   * Invoked by the OS when the auto-focus is complete
   * Just schedules another auto-focus cycle
   *
   * @param _success whether auto-focus was successful
   * @param _camera  the camera which completed auto-focus
   */
  @Override
  public void onAutoFocus(boolean _success, Camera _camera) {
    if (!stopped) {
      autoFocusHandler.postDelayed(doAutoFocus, FOCUS_DELAY);
    }
  }

}
