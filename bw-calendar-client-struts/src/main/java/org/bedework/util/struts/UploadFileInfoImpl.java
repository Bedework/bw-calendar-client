/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.struts;

import org.bedework.util.webaction.UploadFileInfo;

import org.apache.struts.upload.FormFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: mike Date: 3/25/22 Time: 16:56
 */
public class UploadFileInfoImpl implements UploadFileInfo {
  private final FormFile file;

  public UploadFileInfoImpl(final FormFile file) {
    this.file = file;
  }

  @Override
  public String getFileName() {
    return file.getFileName();
  }

  @Override
  public String getContentType() {
    return file.getContentType();
  }

  @Override
  public InputStream getContentStream() {
    try {
    return file.getInputStream();
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public long getLength() {
    return file.getFileSize();
  }
}
