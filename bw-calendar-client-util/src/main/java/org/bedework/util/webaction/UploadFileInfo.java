/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.webaction;

import java.io.File;
import java.io.InputStream;

/**
 * User: mike Date: 3/25/22 Time: 16:56
 */
public interface UploadFileInfo {
  String getFileName();

  String getContentType();

  File getContent();

  InputStream getContentStream();

  long getLength();
}
