package de.evopark.tiqr.android.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Changes the preview size of a camera based on the size of a surface view that is attached to it
 * It is somewhat unfortunate that the size of the display influences the size of the
 * data that's delivered to image processing callbacks, so we might want to look for a different way to scale the view
 */
public class CameraPreviewManager implements SurfaceHolder.Callback {

  private final static String LTAG = CameraPreviewManager.class.getSimpleName();

  // these are tried as fallbacks if no real preview size can be determined
  private final static int PREVIEW_FALLBACK_WIDTH = 640;
  private final static int PREVIEW_FALLBACK_HEIGHT = 480;

  private final Camera camera;
  private final Activity activity;
  private final SurfaceHolder holder;
  private boolean running = false;

  /**
   * Starts showing the camera preview in the surface
   *  @param camera a Camera whose preview size should be adjusted
   * @param holder a SurfaceHolder whose dimensions to use
   * @param activity
   */
  public CameraPreviewManager(Camera camera, SurfaceHolder holder, Activity activity) {
    this.camera = camera;
    this.holder = holder;
    this.activity = activity;
    install();
  }

  public void install() {
    if (running) {
      return;
    }
    Log.d(LTAG, "Linking camera to surface view");
    running = true;
    holder.addCallback(this);
    // if the holder isn't creating anymore, initial callbacks won't be fired
    if (!holder.isCreating()) {
      Log.d(LTAG, "Surface already created, manually firing callbacks");
      surfaceCreated(holder);
      final Rect surfaceSize = holder.getSurfaceFrame();
      surfaceChanged(null, 0, surfaceSize.width(), surfaceSize.height());
    }
  }

  /**
   * Stops adoption of the camera's preview size
   */
  public void uninstall() {
    if (!running) {
      return;
    }
    running = false;
    Log.d(LTAG, "Unlinking camera from surface view");
    holder.removeCallback(this);
  }

  /**
   * Not sure whether that's a good idea, but try some manual cleanup if the object gets cleaned up by GC
   * @see Object#finalize()
   */
  @Override
  public void finalize() {
    uninstall();
  }

  /**
   * @see android.view.SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
   */
  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    if (!running) {
      return;
    }
    try {
      Log.d(LTAG, "Surface created, setting preview display");
      camera.setPreviewDisplay(surfaceHolder);
    } catch (IOException e) {
      Log.e(LTAG, "Could not set camera preview display");
    }
  }

  /**
   * @see android.view.SurfaceHolder.Callback#surfaceChanged(SurfaceHolder, int, int, int)
   */
  @Override
  public void surfaceChanged(SurfaceHolder _holder, int _format, int width, int height) {
    Log.d(LTAG, String.format("Surface changed: %dx%dpx", width, height));
    final Camera.Parameters parameters = camera.getParameters();
    if (parameters != null) {
      Log.d(LTAG, "Stopping preview to adapt to preview surface size");
      camera.stopPreview();
      final List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
      final Camera.Size previewSize = chooseBestPreviewSize(supportedSizes, width, height);
      if (previewSize != null) {
        Log.d(LTAG, String.format("Using preview size %dx%dpx", previewSize.width, previewSize.height));
        adaptToRotation(parameters, previewSize.width, previewSize.height);
      } else {
        Log.d(LTAG, String.format("Could not determine optimum preview size. Falling back to %dx%d",
            PREVIEW_FALLBACK_WIDTH, PREVIEW_FALLBACK_HEIGHT));
        adaptToRotation(parameters, PREVIEW_FALLBACK_WIDTH, PREVIEW_FALLBACK_HEIGHT);
      }
      camera.setParameters(parameters);
      Log.d(LTAG, "Restarting preview with new parameters");
      camera.startPreview();
    }
  }

  /**
   * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(SurfaceHolder)
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder _holder) {
    Log.d(LTAG, "Surface destroyed");
    // TODO: what to do here? Remove camera preview display? Reset preview size?
  }

  /**
   * Chooses the most appropriate preview size from a list of supported sizes
   *
   * @param supported list to select the best size from
   * @param width     width of the target area
   * @param height    height of the target area
   * @return Camera.Size which fits the target area best
   */
  private Camera.Size chooseBestPreviewSize(List<Camera.Size> supported, int width, int height) {
    Camera.Size bestPreviewSize = null;

    float bestRatio = Float.MAX_VALUE;
    final float ratio = (float)width/(float)height;

    for (Camera.Size size : supported) {
      final float currentRatio = (float)size.width/(float)size.height;
      if (Math.abs(currentRatio - ratio) < bestRatio && size.width <= width && size.height <= height) {
        bestRatio = Math.abs(currentRatio - ratio);
        bestPreviewSize = size;
      }
    }
    // if no optimal preview size was found, try reversing width and height
    return bestPreviewSize != null ? bestPreviewSize : chooseBestPreviewSize(supported, height, width);
  }

  /**
   * Adapt the orientation and size of the camera parameters to device rotation
   * That way it's always the correct way down...
   * @param parameters camera paramaters to modify
   * @param previewWidth desired width of the preview
   * @param previewHeight desired height of the preview
   */
  private void adaptToRotation(Camera.Parameters parameters, int previewWidth, int previewHeight) {
    final Display display = activity.getWindowManager().getDefaultDisplay();
    final int rotation = activity.getResources().getConfiguration().orientation;
    if (display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) {
      parameters.setPreviewSize(previewHeight, previewWidth);
    } else {
      parameters.setPreviewSize(previewWidth, previewHeight);
    }
    if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
      camera.setDisplayOrientation(0);
    } else {
      camera.setDisplayOrientation(90);
    }
  }
}
