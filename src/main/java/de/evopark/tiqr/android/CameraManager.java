package de.evopark.tiqr.android;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import de.evopark.tiqr.android.interfaces.PreviewDataProcessor;
import de.evopark.tiqr.android.util.ContinuousAutoFocus;
import de.evopark.tiqr.android.util.CameraPreviewManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for starting and stopping the Camera as well as managing the callbacks
 * For now it only uses the first back-facing camera: front-facing cameras are usually
 * not very comfortable to scan QR codes...
 * <p>
 * TODO use preview callback with buffers
 */
public class CameraManager {
  private final static String LTAG = "CameraManager";
  private static CameraManager instance = null; // it's a singleton

  private Camera camera = null;
  private Activity activity = null;
  private SurfaceHolder previewSurfaceHolder = null;
  private Set<PreviewDataProcessor> previewDataProcessors = new HashSet<PreviewDataProcessor>();

  private ContinuousAutoFocus autofocus = null;
  private CameraPreviewManager cameraPreviewManager = null;

  /**
   * Private ctor
   * @see #getInstance()
   */
  private CameraManager() {
  }

  /**
   * Provides access to this class' singleton instance
   * @return the singleton instance
   */
  public static synchronized CameraManager getInstance() {
    if (instance == null) {
      instance = new CameraManager();
    }
    return instance;
  }

  /**
   * Add a handler that should be used to analyze preview data
   *
   * @param processor
   * @see PreviewDataProcessor
   */
  public void addPreviewDataProcessor(PreviewDataProcessor processor) {
    previewDataProcessors.add(processor);
    if (previewDataProcessors.size() == 1) {
      registerPreviewCallback();
    }
  }

  /**
   * Stop sending preview data to a given handler
   *
   * @param processor PreviewDataProcessor which was previously added via addPreviewDataProcessor
   */
  public void removePreviewDataProcessor(PreviewDataProcessor processor) {
    previewDataProcessors.remove(processor);
    if (previewDataProcessors.size() == 0 && camera != null) {
      camera.setPreviewCallback(null);
    }
  }

  /**
   * Start feeding data to registered callbacks:
   * If a preview surface holder was registered, it will receive camera pictures
   * If preview data processors have been registered, they will start receiving data
   * If a CameraView has been instantiated, it will receive preview pictures
   */
  public void startCapture() {
    if (camera == null) {
      camera = Camera.open();
      setupAutoFocus();
      linkCameraViewAndSurface();
      registerPreviewCallback();
    }
    camera.startPreview();
  }

  /**
   * Stop sending data to camera previews and data processing callbacks
   * Frees all camera resources
   */
  public void stopCapture() {
    if (camera != null) {
      Log.d(LTAG, "Stopping camera");
      final Camera oldCamera = camera;
      camera = null;
      if (autofocus != null) {
        autofocus.stop();
        autofocus = null;
      }
      linkCameraViewAndSurface();
      oldCamera.stopPreview();
      oldCamera.setPreviewCallback(null);
      oldCamera.release();
    }
  }

  /**
   * Show the camera picture on the given surface
   * @param holder holder of a surface which should show the camera picture
   * @param activity used to determine rotation
   */
  public void showPreviewInSurface(final SurfaceHolder holder, Activity activity) {
    if (holder != previewSurfaceHolder) {
      previewSurfaceHolder = holder;
      this.activity = activity;
      linkCameraViewAndSurface();
    }
  }

  private void setupAutoFocus() {
    if (camera != null) {
      if (autofocus == null) {
        autofocus = new ContinuousAutoFocus(camera);
      } else {
        autofocus.start();
      }
    }
  }

  /**
   * Registers a preview callback with the camera
   * which in turn calls all PreviewDataProcessor instances
   * with the data it received
   */
  private void registerPreviewCallback() {
    if (camera != null) {
      if (previewDataProcessors.size() > 0) {
        Log.d(LTAG, "Registering preview callback");
        camera.setPreviewCallback(new Camera.PreviewCallback() {
          @Override
          public void onPreviewFrame(byte[] bytes, Camera _camera) {
            if (camera == null) {
              return;
            }
            Camera.Parameters params = camera.getParameters();
            if (params == null) {
              return;
            }
            int pictureFormat = params.getPreviewFormat();
            Camera.Size size = params.getPreviewSize();
            for (PreviewDataProcessor processor : previewDataProcessors) {
              processor.processBitmap(size.width, size.height, pictureFormat, bytes);
            }
          }
        });
      } else {
        Log.d(LTAG, "No processors, unregistering camera preview callback");
        camera.setPreviewCallback(null);
      }
    }
  }

  private void linkCameraViewAndSurface() {
    if (camera == null || previewSurfaceHolder == null) {
      if (cameraPreviewManager != null) {
        cameraPreviewManager.uninstall();
        cameraPreviewManager = null;
      }
    } else if (camera != null && previewSurfaceHolder != null) {
      if (cameraPreviewManager == null) {
        cameraPreviewManager = new CameraPreviewManager(camera, previewSurfaceHolder, activity);
      }
    }
  }

}