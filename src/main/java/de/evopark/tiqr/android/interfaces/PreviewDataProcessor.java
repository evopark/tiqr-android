package de.evopark.tiqr.android.interfaces;

import de.evopark.tiqr.android.CameraManager;

/**
 * Interface for consumers who want to process data from the camera
 * @see CameraManager
 */
public abstract class PreviewDataProcessor {
  /**
   * Will be invoked when new preview image data is available from the camera
   *
   * @param width  width of the picture in {data} in pixels
   * @param height height of the picture in {data} in pixels
   * @param format format of the data in {data}
   * @param data   image data buffer
   * @see android.graphics.ImageFormat
   */
  public abstract void processBitmap(int width, int height, int format, byte[] data);
}
