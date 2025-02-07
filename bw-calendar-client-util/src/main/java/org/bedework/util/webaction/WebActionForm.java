package org.bedework.util.webaction;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/31/22 Time: 14:18
 */
public interface WebActionForm {
  /* ================ Properties methods ============== */

  void reset(HttpServletRequest request);

  /**
   *
   * @return null if no request parameter named imageUpload otherwise
   *               an UploadFileInfo object
   */
  UploadFileInfo getImageUploadInfo();

  /**
   *
   * @return null if no request parameter named uploadFile otherwise
   *               an UploadFileInfo object
   */
  UploadFileInfo getUploadFileInfo();
}
