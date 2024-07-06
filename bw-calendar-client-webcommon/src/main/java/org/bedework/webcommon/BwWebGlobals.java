/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.util.misc.Util;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.WebGlobals;

import java.util.Collection;
import java.util.Locale;

/**
 * User: mike Date: 6/26/24 Time: 22:53
 */
public class BwWebGlobals extends WebGlobals {
  private Locale requestedLocale;

  private BwPrincipal<?> adminUserId;

  /**
   * The current administrative user.
   */
  protected String currentAdminUser;

  public void reset(final BwRequest req) {
    super.reset(req);
    setLocale(req);
  }

  private void setLocale(final BwRequest request) {
    final BwModuleState mstate = request.getModule().getState();
    final Collection<Locale> reqLocales = request.getLocales();
    final String reqLoc = request.getReqPar("locale");

    if (reqLoc != null) {
      if ("default".equals(reqLoc)) {
        requestedLocale = null;
      } else {
        try {
          requestedLocale = Util.makeLocale(reqLoc); // Make it stick
        } catch (final Throwable t) {
          // Ignore bad parameter?
        }
      }
    }

    final Locale loc =
            request.getClient().getUserLocale(reqLocales,
                                              getRequestedLocale());

    if (loc != null) {
      BwLocale.setLocale(loc);
      final Locale cloc = getCurrentLocale();
      if ((cloc == null) || (!cloc.equals(loc))) {
        mstate.setRefresh(true);
      }
      currentLocale = loc;
    }
  }

  /**
   * @return locale or null
   */
  public Locale getRequestedLocale() {
    return requestedLocale;
  }

  /**
   * This holds whatever account we are running as. We may be running
   * as something other than the authenticated account - e.g. public admin
   * of a calendar suite. We need this to hold that cvalue as we may
   * not have a client embedded on entry.
   *
   * @return admin user id
   */
  public String getCurrentAdminUser() {
    return currentAdminUser;
  }

  /**
   * @return admin userid
   */
  public String getAdminUserId() {
    return adminUserId.getAccount();
  }

  public void reset(final Client cl) {
    currentAdminUser = cl.getCurrentPrincipal().getAccount();
  }

  public void changeAdminUserId(final BwPrincipal<?> val) {
    adminUserId = val;
  }

  /**
   * @return default timezoneid
   */
  public String getDefaultTzid() {
    return Timezones.getThreadDefaultTzid();
  }
}
