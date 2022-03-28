/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import java.io.InputStream;

/**
 * User: mike Date: 3/25/22 Time: 16:56
 */
public class UploadFileInfo {
  private InputStream content;

  private long length;

  public UploadFileInfo(final InputStream content,
                        final long length) {
    this.content = content;
    this.length = length;
  }

  public InputStream getContent() {
    return content;
  }

  public long getLength() {
    return length;
  }
}
