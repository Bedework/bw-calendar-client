/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.struts;

import org.bedework.util.webaction.UploadFileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: mike Date: 3/25/22 Time: 16:56
 */
public class UploadFileInfoImpl implements UploadFileInfo {
  private final File file;
  private final String fileName;
  private final String contentType;

  public UploadFileInfoImpl(final File file,
                            final String fileName,
                            final String contentType) {
    this.file = file;
    this.fileName = fileName;
    this.contentType = contentType;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public InputStream getContentStream() {
    try {
      return new FileInputStream(file);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public long getLength() {
    return file.length();
  }
}
