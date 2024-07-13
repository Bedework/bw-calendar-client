/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.client.rw.InOutBoxInfo;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwWebGlobals;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * User: mike
 * Date: 7/7/24
 * Time: 16:00
 */
public class BwRWWebGlobals extends BwWebGlobals {
  private BwSynchInfo synchInfo;
  private String submissionRoot;
  private String encodedSubmissionRoot = "";

  private String imageUploadDirectory;

  private NotificationInfo notificationInfo;

  private InOutBoxInfo inBoxInfo;

  /** Last email address used to mail message. By default set to
   * preferences value.
   */
  private String lastEmail;

  private String subject;

  /**
   * @return info or null
   */
  public BwSynchInfo getSynchInfo() {
    return synchInfo;
  }

  /** Return the unencoded root of the workflow collections
   *
   * @return String path.
   */
  public String getSubmissionRoot() {
    return submissionRoot;
  }

  /** Return the encoded root of the submissions calendars
   *
   * @return String path.
   */
  public String getEncodedSubmissionRoot() {
    return encodedSubmissionRoot;
  }

  /**
   * @return path or null
   */
  public String getImageUploadDirectory() {
    return imageUploadDirectory;
  }

  /**
   * @param val NotificationInfo
   */
  public void setNotificationInfo(final NotificationInfo val) {
    notificationInfo = val;
  }

  /**
   * @return NotificationInfo
   */
  public NotificationInfo getNotificationInfo() {
    return notificationInfo;
  }

  /**
   * @param val
   */
  public void setInBoxInfo(final InOutBoxInfo val) {
    inBoxInfo = val;
  }

  public void setLastEmail(final String val) {
    lastEmail = val;
  }

  public String getLastEmail() {
    return lastEmail;
  }

  public void setSubject(final String val) {
    subject = val;
  }

  public String getSubject() {
    return subject;
  }

  /**
   * @return InOutBoxInfo
   */
  public InOutBoxInfo getInBoxInfo() {
    return inBoxInfo;
  }

  public void reset(final Client cl) {
    super.reset(cl);

    synchInfo = ((RWClient)cl).getSynchInfo();

    final var sysprops = cl.getSystemProperties();

    if (cl.getWebSubmit() || cl.getPublicAdmin()) {
      submissionRoot = sysprops.getSubmissionRoot();
      if (getSubmissionRoot() != null) {
        encodedSubmissionRoot = URLEncoder.encode(
                getSubmissionRoot(),
                StandardCharsets.UTF_8);
      }
    }

    final var prefs = cl.getPreferences();

    imageUploadDirectory = prefs.getDefaultImageDirectory();
  }
}
