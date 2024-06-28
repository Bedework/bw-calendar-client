package org.bedework.util.webaction;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mike Date: 3/31/22 Time: 14:18
 */
public interface WebActionForm {
  /* ================ Properties methods ============== */

  /**
   * @param val schema + host + port part of uri
   */
  void setSchemeHostPort(String val);

  /**
   * @return schema + host + port part of uri
   */
  String getSchemeHostPort();

  /** Set the part of the URL that identifies the application.
   *
   * @param val       context path in form "/" + name-of-app, e.g. /kiosk
   */
  void setContext(String val);

  /**
   * @return String
   */
  String getContext();

  /** This should not be setCurrentUser as that exposes it to the incoming
   * request.
   *
   * @param val      String user id
   */
  void assignCurrentUser(String val);

  /**
   * @return String
   */
  String getCurrentUser();

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
