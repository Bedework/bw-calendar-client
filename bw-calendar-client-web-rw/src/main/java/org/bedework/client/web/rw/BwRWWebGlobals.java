/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.BwWebGlobals;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * User: mike
 * Date: 7/7/24
 * Time: 16:00
 */
public class BwRWWebGlobals extends BwWebGlobals {
  private String submissionRoot;
  private String encodedSubmissionRoot = "";

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

  public void reset(final Client cl) {
    super.reset(cl);

    final var sysprops = cl.getSystemProperties();

    if (cl.getWebSubmit() || cl.getPublicAdmin()) {
      submissionRoot = sysprops.getSubmissionRoot();
      if (getSubmissionRoot() != null) {
        encodedSubmissionRoot = URLEncoder.encode(
                getSubmissionRoot(),
                StandardCharsets.UTF_8);
      }
    }
  }
}
