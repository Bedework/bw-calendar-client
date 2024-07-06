package org.bedework.util.webaction;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/31/22 Time: 14:18
 */
public interface WebActionForm {
  /* ================ Properties methods ============== */

  /** This should not be setConfirmationId as that exposes it to the incoming
   * request.
   *
   * @param val      String confirmation id
   */
  void assignConfirmationId(String val);

  /**
   * @return String
   */
  String getConfirmationId();

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
