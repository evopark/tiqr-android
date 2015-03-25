package de.evopark.tiqr.android;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import de.evopark.tiqr.android.interfaces.PreviewDataProcessor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for starting and stopping the Camera as well as managing the callbacks
 * For now it only uses the first back-facing camera: front-facing cameras are usually
 * not very comfortable to scan QR codes...
 *
 * TODO use preview callback with buffers
 */
public class CameraManager {
  private final static String LTAG = "CameraManager";

  private Camera camera = null;
  private SurfaceHolder previewSurfaceHolder = null;
  private Set<PreviewDataProcessor> previewDataProcessors = new HashSet<PreviewDataProcessor>();

  // singleton code start
  private static CameraManager instance = null;

  public static synchronized CameraManager getInstance() {
    if (instance == null) {
      instance = new CameraManager();
    }
    return instance;
  }

  private CameraManager() {
  }
  // singleton code end

  /**
   * Add a processor that should be used to analyze preview data
   * @param processor
   */
  public void addPreviewDataProcessor(PreviewDataProcessor processor) {
    previewDataProcessors.add(processor);
    if (previewDataProcessors.size() == 1) {
      doRegisterCallback();
    }
  }

  /**
   * Stop sending preview data to a given processor
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
   * If preview data processors will be registered, they will start receiving data
   */
  public void startCapture() {
    if (camera == null) {
      camera = Camera.open();
      doSetPreviewDisplay();
      doRegisterCallback();
    }
    camera.startPreview();
  }

  /**
   * Sets the surface that should show a preview of the current camera's picture
   */
  public void setPreviewSurfaceHolder(SurfaceHolder newSurfaceHolder) {
    if (newSurfaceHolder == previewSurfaceHolder) {
      return;
    }
    if (previewSurfaceHolder != null) {
      if (newSurfaceHolder != null) {
        Log.i(LTAG, "Preview surface holder changed. Old holder remains stale.");
      } else {
        Log.i(LTAG, "Disabling old surface holder");
      }
    }
    previewSurfaceHolder = newSurfaceHolder;
    doSetPreviewDisplay();
  }

  private void doSetPreviewDisplay() {
    if (camera != null) {
      try {
        camera.setPreviewDisplay(previewSurfaceHolder);
      } catch (IOException e) {
        Log.e(LTAG, "Failed to set preview surface", e);
      }
    }
  }

  private void doRegisterCallback() {
    if (camera != null) {
      if (previewDataProcessors.size() > 0) {
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
        camera.setPreviewCallback(null);
      }
    }
  }

  /**
   * Stop sending data to camera previews and data processing callbacks
   */
  public void stopCapture() {
    if (camera != null) {
      Camera oldCamera = camera;
      camera = null;
      oldCamera.stopPreview();
      oldCamera.release();
    }
  }
}
