/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
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

  private boolean hour24;

  /* This should be a cloned copy only */
  private DirectoryInfo dirInfo;

  /** true if this is a guest (unauthenticated) user
   */
  private boolean guest;

  private BwPrincipal<?> adminUser;

  /** The groups of which our user is a member
   */
  private Collection<BwGroup<?>> currentGroups;

  private BwCalSuiteWrapper currentCalSuite;

  private String calSuiteName;

  private String eventRegAdminToken;
  private String eventRegWsUrl;
  private String eventRegAdminUrl;

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
   * @param val true for 24 hour times
   */
  public void setHour24(final boolean val) {
    hour24 = val;
  }

  /**
   * @return bool
   */
  public boolean getHour24() {
    return hour24;
  }

  /**
   * @return DirectoryInfo
   */
  public DirectoryInfo getDirInfo() {
    return dirInfo;
  }

  /**
   * @return true for guest
   */
  public boolean getGuest() {
    return guest;
  }

  /**
   * This holds whatever account we are running as. We may be running
   * as something other than the authenticated account - e.g. public admin
   * of a calendar suite. We need this to hold that cvalue as we may
   * not have a client embedded on entry.
   *
   * @return admin user
   */
  public BwPrincipal<?> getAdminUser() {
    return adminUser;
  }

  /**
   * @return admin userid
   */
  public String getAdminUserId() {
    if (getAdminUser() == null) {
      return null;
    }
    return getAdminUser().getAccount();
  }

  /** The groups of which our user is a member
   *
   * @param val The groups of which our user is a member
   */
  public void setCurrentGroups(final Collection<BwGroup<?>> val) {
    currentGroups = val;
  }

  /**
   * @return user admin groups
   */
  public Collection<BwGroup<?>> getCurrentGroups() {
    return currentGroups;
  }

  /**
   *
   * @param val Current calSuite for the application
   */
  public void setCurrentCalSuite(final BwCalSuiteWrapper val) {
    currentCalSuite = val;
  }

  /**
   * @return BwCalSuiteWrapper
   */
  public BwCalSuiteWrapper getCurrentCalSuite() {
    return currentCalSuite;
  }

  /**
   *
   * @param val Name of current CalSuite.
   */
  public void setCalSuiteName(final String val) {
    calSuiteName = val;
  }

  /** name of current CalSuite.
   *
   * @return String
   */
  public String getCalSuiteName() {
    return calSuiteName;
  }

  /**
   * @param val token for event registration
   */
  public void setEventRegAdminToken(final String val) {
    eventRegAdminToken = val;
  }

  /**
   * @return token for event registration
   */
  public String getEventRegAdminToken() {
    return eventRegAdminToken;
  }

  public void setEventRegWsUrl(final String val) {
    eventRegWsUrl = val;
  }

  public String getEventRegWsUrl() {
    return eventRegWsUrl;
  }

  public void setEventRegAdminUrl(final String val) {
    eventRegAdminUrl = val;
  }

  public String getEventRegAdminUrl() {
    return eventRegAdminUrl;
  }

  public void reset(final Client cl) {
    if (dirInfo == null) {
      dirInfo = cl.getDirectoryInfo();
    }

    guest = cl.isGuest();
    adminUser = cl.getCurrentPrincipal();
  }

  public void changeAdminUser(final BwPrincipal<?> val) {
    if (debug()) {
      debug("New principal: {} adminUser: {}",
            val, getAdminUser());
    }
    adminUser = val;
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
