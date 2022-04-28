/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwString;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.io.File;

import javax.servlet.http.HttpServletResponse;

/**
 * User: mike Date: 3/9/21 Time: 22:37
 */
public abstract class RWActionBase extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final RWClient cl = (RWClient)request.getClient();

    /* Check access
     */
    if (cl.isGuest()) {
      if (actionIsWebService()) {
        request.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);

        return forwardNull;
      }

      return forwardNoAccess; // First line of defence
    }

    return doAction(request,
                    cl,
                    (BwRWActionForm)form);
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
   * @throws Throwable on fatal error
   */
  public abstract int doAction(BwRequest request,
                               RWClient cl,
                               BwRWActionForm form) throws Throwable;

  public BwRWActionForm getRwForm() {
    return (BwRWActionForm)form;
  }

  /**
   * @param val FormFile
   */
  public void setImageUpload(final File val) {
    getForm().setImageUpload(val);
  }

  /**
   * @return FormFile
   */
  public File getImageUpload() {
    return getForm().getImageUpload();
  }

  public void setImageUploadFileName(final String val) {
    getForm().setImageUploadFileName(val);
  }

  public String getImageUploadFileName() {
    return getForm().getImageUploadFileName();
  }

  public void setImageUploadContentType(final String val) {
    getForm().setImageUploadContentType(val);
  }

  public String getImageUploadContentType() {
    return getForm().getImageUploadContentType();
  }

  public void setUploadFileFileName(final String val) {
    getForm().setUploadFileFileName(val);
  }

  public String getUploadFileFileName() {
    return getForm().getUploadFileFileName();
  }

  public void setUploadFile(final File val) {
    getForm().setUploadFile(val);
  }

  public File getUploadFile() {
    return getForm().getUploadFile();
  }

  public void setUploadFileContentType(final String val) {
    getForm().setUploadFileContentType(val);
  }

  public String getUploadFileContentType() {
    return getForm().getUploadFileContentType();
  }

  // ================== contact form elements ===================

  public void setContactUid(final String val) {
    getRwForm().setContactUid(val);
  }

  public String getContactUid() {
    return getRwForm().getContactUid();
  }

  // ================== location form elements ===================

  public void setLocationAddress(final BwString val) {
    getRwForm().setLocationAddress(val);
  }

  public BwString getLocationAddress() {
    return getRwForm().getLocationAddress();
  }

  public void setLocationSubaddress(final BwString val) {
    getRwForm().setLocationSubaddress(val);
  }

  public BwString getLocationSubaddress() {
    return getRwForm().getLocationSubaddress();
  }

  public void setLocationUid(final String val) {
    getRwForm().setLocationUid(val);
  }

  public String getLocationUid() {
    return getRwForm().getLocationUid();
  }

  // =================== collection form elements ===================
  public void setCalendar(final BwCalendar val) {
    getBwForm().setCalendar(val);
  }

  /** If a calendar object exists, return that otherwise create an empty one.
   *
   * @return BwCalendar  populated calendar value object
   */
  public BwCalendar getCalendar() {
    return getBwForm().getCalendar();
  }
}
