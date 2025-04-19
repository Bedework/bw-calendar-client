/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwString;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import java.io.File;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class RWActionBase extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request) {
    final RWClient cl = (RWClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest()) {
      if (actionIsWebService()) {
        request.sendError(HttpServletResponse.SC_FORBIDDEN, null);

        return forwardNull;
      }

      return forwardNoAccess; // First line of defence
    }

    return doAction(request, cl);
  }

  /**
   * @return true if we should set status and return null for guest.
   */
  protected boolean actionIsWebService() {
    return false;
  }

  /** This is the routine which does the work.
   *
   * @param request   For request pars and BwSession
   * @param form       Admin action form
   * @return int      forward index
   */
  public abstract int doAction(BwRequest request,
                               RWClient cl);

  public BwRWActionForm getRwForm() {
    return (BwRWActionForm)form;
  }

  /**
   * @param val FormFile
   */
  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setImageUpload(final File val) {
    getForm().setImageUpload(val);
  }

  /**
   * @return FormFile
   */
  @SuppressWarnings("UnusedDeclaration")
  public File getImageUpload() {
    return getForm().getImageUpload();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setImageUploadFileName(final String val) {
    getForm().setImageUploadFileName(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getImageUploadFileName() {
    return getForm().getImageUploadFileName();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setImageUploadContentType(final String val) {
    getForm().setImageUploadContentType(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getImageUploadContentType() {
    return getForm().getImageUploadContentType();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setUploadFileFileName(final String val) {
    getForm().setUploadFileFileName(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getUploadFileFileName() {
    return getForm().getUploadFileFileName();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setUploadFile(final File val) {
    getForm().setUploadFile(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public File getUploadFile() {
    return getForm().getUploadFile();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setUploadFileContentType(final String val) {
    getForm().setUploadFileContentType(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getUploadFileContentType() {
    return getForm().getUploadFileContentType();
  }

  // ================== contact form elements ===================

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setContactUid(final String val) {
    getRwForm().setContactUid(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getContactUid() {
    return getRwForm().getContactUid();
  }

  // ================== location form elements ===================

  @SuppressWarnings("UnusedDeclaration")
  public void setLocationAddress(final BwString val) {
    getRwForm().setLocationAddress(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public BwString getLocationAddress() {
    return getRwForm().getLocationAddress();
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setLocationSubaddress(final BwString val) {
    getRwForm().setLocationSubaddress(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public BwString getLocationSubaddress() {
    return getRwForm().getLocationSubaddress();
  }

  @SuppressWarnings("UnusedDeclaration")
  @StrutsParameter
  public void setLocationUid(final String val) {
    getRwForm().setLocationUid(val);
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getLocationUid() {
    return getRwForm().getLocationUid();
  }

  // =================== collection form elements ===================
  public void setCalendar(final BwCollection val) {
    getBwForm().setCalendar(val);
  }

  /** If a calendar object exists, return that otherwise create an empty one.
   *
   * @return BwCollection  populated calendar value object
   */
  public BwCollection getCalendar() {
    return getBwForm().getCalendar();
  }
}
