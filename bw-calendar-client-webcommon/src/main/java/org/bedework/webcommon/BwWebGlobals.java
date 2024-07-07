/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.util.misc.Util;
import org.bedework.util.timezones.TimeZoneName;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.webaction.WebGlobals;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

/**
 * User: mike Date: 6/26/24 Time: 22:53
 */
public class BwWebGlobals extends WebGlobals {
  private DateTimeFormatter today;

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

  /**
   * @return DateTimeFormatter today
   */
  public DateTimeFormatter getToday() {
    final var now = new Date(System.currentTimeMillis());
    if (today != null) {
      return today;
    }

    today = new DateTimeFormatter(
            BwDateTimeUtil.getDateTime(now));

    return today;
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

  /** Get a list of system and user timezones
   *
   * @return Collection of timezone names
   */
  public Collection<TimeZoneName> getTimeZoneNames() {
    try {
      return Timezones.getTzNames();
    } catch (final Throwable t) {
      error(t);
      return new TreeSet<TimeZoneName>();
    }
  }
}
