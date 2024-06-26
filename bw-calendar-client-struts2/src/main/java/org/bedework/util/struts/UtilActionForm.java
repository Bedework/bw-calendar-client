/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.util.struts;

import org.bedework.util.misc.Util;
import org.bedework.util.webaction.UploadFileInfo;
import org.bedework.util.webaction.WebActionForm;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides some convenience methods for use by Action objects.
 *
 * @author Mike Douglass
 */
public class UtilActionForm implements WebActionForm {
  /* URL Components are here for the benefit of jsp to avoid cluttering up
     pages with code.
   */
  /** First part of URL. Allows us to target services on same host.
   */
  protected String schemeHostPort;

  /** The part of the URL that identifies the application -
   * Of the form "/" + name-of-app, e.g. /kiosk
   */
  protected String context;

  /**
   * The current authenticated user. May be null
   */
  protected String currentUser;

  /**
   * Confirmation id - require this on forms
   */
  protected String confirmationId;

  /* ...........................................................
   *                       Uploads and exports
   * ........................................................... */

  private File imageUpload;
  private String imageUploadFileName;
  private String imageUploadContentType;

  private File uploadFile;
  private String uploadFileFileName;
  private String uploadFileContentType;

  /* ==========================================================
   *                          Methods
     ========================================================== */

  /* ================ Properties methods ============== */

  @Override
  public void setSchemeHostPort(final String val) {
    schemeHostPort = val;
  }

  @Override
  public String getSchemeHostPort() {
    return schemeHostPort;
  }

  @Override
  public void setContext(final String val) {
    context = val;
  }

  @Override
  public String getContext() {
    return context;
  }

  @Override
  public void assignCurrentUser(final String val) {
    currentUser = val;
  }

  @Override
  public String getCurrentUser() {
    return currentUser;
  }

  @Override
  public void assignConfirmationId(final String val) {
    confirmationId = val;
  }

  @Override
  public String getConfirmationId() {
    if (confirmationId == null) {
      confirmationId = Util.makeRandomString(16, 35);
    }

    return confirmationId;
  }

  @Override
  public void reset(final HttpServletRequest request) {
    // Default implementation does nothing
  }

  /* ==========================================================
   *                   Uploads and exports
   * ========================================================== */

  /**
   * @param val FormFile
   */
  public void setImageUpload(final File val) {
    imageUpload = val;
  }

  /**
   * @return FormFile
   */
  public File getImageUpload() {
    return imageUpload;
  }

  public void setImageUploadFileName(final String val) {
    imageUploadFileName = val;
  }

  public String getImageUploadFileName() {
    return imageUploadFileName;
  }

  public void setImageUploadContentType(final String val) {
    imageUploadContentType = val;
  }

  public String getImageUploadContentType() {
    return imageUploadContentType;
  }

  @Override
  public UploadFileInfo getImageUploadInfo() {
    if (imageUpload == null) {
      return null;
    }

    return new UploadFileInfoImpl(imageUpload,
                                  imageUploadFileName,
                                  imageUploadContentType);
  }

  public void setUploadFileFileName(final String val) {
    uploadFileFileName = val;
  }

  public String getUploadFileFileName() {
    return uploadFileFileName;
  }

  /**
   * @param val the form file
   */
  public void setUploadFile(final File val) {
    uploadFile = val;
  }

  /**
   * @return FormFile
   */
  public File getUploadFile() {
    return uploadFile;
  }

  public void setUploadFileContentType(final String val) {
    uploadFileContentType = val;
  }

  public String getUploadFileContentType() {
    return uploadFileContentType;
  }

  @Override
  public UploadFileInfo getUploadFileInfo() {
    if (uploadFile == null) {
      return null;
    }

    return new UploadFileInfoImpl(uploadFile,
                                  uploadFileFileName,
                                  uploadFileContentType);
  }
}

