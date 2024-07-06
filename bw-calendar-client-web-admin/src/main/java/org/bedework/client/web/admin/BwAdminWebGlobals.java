/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.BwWebGlobals;

/**
 * User: mike Date: 7/4/24 Time: 12:31
 */
public class BwAdminWebGlobals extends BwWebGlobals {
  private boolean superUser;

  public boolean isSuperUser() {
    return superUser;
  }

  public void reset(final Client cl) {
    superUser = cl.isSuperUser();
    super.reset(cl);
  }
}
