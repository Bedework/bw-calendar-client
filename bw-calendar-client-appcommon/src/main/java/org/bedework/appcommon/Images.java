package org.bedework.appcommon;

import java.awt.*;
import java.io.Closeable;
import java.io.InputStream;

/**
 * User: mike Date: 3/31/22 Time: 18:46
 */
public interface Images extends Closeable {
  /**
   *
   * @return the full sized image
   */
  Image getImage();

  /**
   *
   * @return a thumbnail or null if none requested
   */
  Image getThumbnail();

  /**
   *
   * @return length of full image
   */
  long getLength();

  /**
   *
   * @return length of thumb image or zero
   */
  long getThumbLength();

  /**
   *
   * @return inputstream for full image
   */
  InputStream getInputStream();

  /**
   *
   * @return inputstream for thumbnail or null
   */
  InputStream getThumbInputStream();
}
