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

package org.bedework.webcommon;

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.DayView;
import org.bedework.appcommon.MonthView;
import org.bedework.appcommon.MyCalendarVO;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.WeekView;
import org.bedework.appcommon.YearView;
import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.servlet.filters.PresentationState;

import org.apache.struts.util.MessageResources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/** This ought to be made pluggable. We need a session factory which uses
 * CalEnv to figure out which implementation to use.
 *
 * <p>This class represents a session for the Bedework web interface.
 * Some user state will be retained here.
 * We also provide a number of methods which act as the interface between
 * the web world and the calendar world.
 *
 * @author Mike Douglass   douglm     rpi.edu
 */
public class BwSessionImpl implements BwSession {
  private static final String refreshTimeAttr = "bw_refresh_time";
  private static final long refreshRate = 1 * 60 * 1000;

  /** Not completely valid in the j2ee world but it's only used to count sessions.
   */
  private static class Counts {
    long totalSessions = 0;
  }

  private static volatile HashMap<String, Counts> countsMap =
    new HashMap<String, Counts>();
  private long sessionNum = 0;

  /** The current user - null for guest
   */
  private String user;

  /** The application root
   */
  //private String appRoot;

  /** The application name
   */
  private String appName;

  /** The current presentation state of the application
   */
  private PresentationState ps;

  /** Constructor for a Session
   *
   * @param user       String user id
   * @param browserResourceRoot
   * @param appRoot
   * @param appName    String identifying particular application
   * @param ps
   * @param messages
     @param schemeHostPort The prefix for generated urls referring to this server
   * @throws Throwable
   */
  public BwSessionImpl(final String user,
                       final String browserResourceRoot,
                       final String appRoot,
                       final String appName,
                       final PresentationState ps,
                       final MessageResources messages,
                       final String schemeHostPort) throws Throwable {
    this.user = user;

    this.appName = appName;
    this.ps = ps;

    if (ps != null) {
      /*
      if (ps.getAppRoot() == null) {
        ps.setAppRoot(prefixUri(schemeHostPort, appRoot));
      }

      if (ps.getBrowserResourceRoot() == null) {
        ps.setBrowserResourceRoot(prefixUri(schemeHostPort, browserResourceRoot));
      }
      */
      ps.setAppRoot(appRoot);
      ps.setBrowserResourceRoot(browserResourceRoot);
    }

    setSessionNum(appName);
  }

  /* NOTE: This is NOT intended to turn a relative URL into an
  absolute URL. It is a convenience for development which turns a
  not fully specified url into a url referring to the server.

  This will not work if they are treated as relative to the servlet.

  In production mode, the appRoot will normally be fully specified to a
  different web server.
* /
  private String prefixUri(final String schemeHostPort,
                           final String val) {
    if (val.toLowerCase().startsWith("http")) {
      return val;
    }

    StringBuilder sb = new StringBuilder(schemeHostPort);

    if (!val.startsWith("/")) {
      sb.append("/");
    }
    sb.append(val);

    return sb.toString();
  }
  */

  /* ======================================================================
   *                     Property methods
   * ====================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwSession#getSessionNum()
   */
  @Override
  public long getSessionNum() {
    return sessionNum;
  }

  /**
   * @param val
   */
  public void setAppName(final String val) {
    appName = val;
  }

  /**
   * @return app name
   */
  public String getAppName() {
    return appName;
  }

  /**
   * @param val
   */
  @Override
  public void setUser(final String val) {
    user = val;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public void setPresentationState(final PresentationState val) {
    ps = val;
  }

  @Override
  public PresentationState getPresentationState() {
    return ps;
  }

  @Override
  public boolean isGuest() {
    return user == null;
  }

  @Override
  public void prepareRender(final BwRequest req) {
    BwActionFormBase form = req.getBwForm();

    try {
      Long lastRefresh = (Long)req.getSessionAttr(refreshTimeAttr);
      long now = System.currentTimeMillis();

      if (!form.isRefreshNeeded() ||
              (lastRefresh == null) || (now - lastRefresh > refreshRate)) {
        // Implant various objects for the pages.
        embedFilters(req);
        embedCollections(req);
        embedPublicCollections(req);
        embedUserCollections(req);
        embedPrefs(req);
        embedViews(req);

        req.setSessionAttr(refreshTimeAttr, now);
      }

      if (!form.isRefreshNeeded()) {
        // Always returned false; if (!form.fetchSvci().refreshNeeded()) {
        return;
        //}
      }
    } catch (Throwable t) {
      // Not much we can do here
      form.getErr().emit(t);
      return;
    }

    if (!req.getClient().getPublicAdmin() ||
            form.getCurTimeView() == null) {
      refreshView(req);
      form.setRefreshNeeded(false);
    }
  }

  @Override
  public void embedFilters(final BwRequest req) throws Throwable {
    req.setSessionAttr(BwRequest.bwFiltersListName,
                       req.getClient().getAllFilters());
  }

  /** Reset the view according to the current setting of curViewPeriod.
   * May be called when we change the view or if we need a refresh
   *
   */
  public void refreshView(final BwRequest req) {
    BwActionFormBase form = req.getBwForm();
    Client cl = req.getClient();

    try {
      /* First ensure we have the view period set */
      if (form.getCurTimeView() == null) {
        /** Figure out the default from the properties
         */
        String vn;

        try {
          vn = cl.getPreferences().getPreferredViewPeriod();
          if (vn == null) {
            vn = "week";
          }
        } catch (Throwable t) {
          System.out.println("Exception setting current view");
          vn = "week";
        }

        if (form.getCurViewPeriod() < 0) {
          for (int i = 1; i < BedeworkDefs.viewPeriodNames.length; i++) {
            if (BedeworkDefs.viewPeriodNames[i].startsWith(vn)) {
              form.setCurViewPeriod(i);
              break;
            }
          }

          if (form.getCurViewPeriod() < 0) {
            form.setCurViewPeriod(BedeworkDefs.weekView);
          }

          form.setViewMcDate(new MyCalendarVO(new Date(System.currentTimeMillis())));
        }
      }

      /* Now get a view object */

      if ((form.getCurViewPeriod() == BedeworkDefs.todayView) ||
              (form.getViewMcDate() == null)) {
        form.setViewMcDate(new MyCalendarVO(new Date(
                System.currentTimeMillis())));
      }

      FilterBase filter = getFilter(req, null);
      TimeView tv = null;

      switch (form.getCurViewPeriod()) {
        case BedeworkDefs.todayView:
          tv = new DayView(cl,
                           req.getErr(),
                           form.getViewMcDate(),
                           filter);
          break;
        case BedeworkDefs.dayView:
          tv = new DayView(cl,
                           req.getErr(),
                           form.getViewMcDate(),
                           filter);
          break;
        case BedeworkDefs.weekView:
          tv = new WeekView(cl,
                            req.getErr(),
                            form.getViewMcDate(),
                            filter);
          break;
        case BedeworkDefs.monthView:
          tv = new MonthView(cl,
                             req.getErr(),
                             form.getViewMcDate(),
                             filter);
          break;
        case BedeworkDefs.yearView:
          tv = new YearView(cl,
                            req.getErr(),
                            form.getViewMcDate(),
                            form.getShowYearData(), filter);
          break;
      }

      form.setCurTimeView(tv);
    } catch (Throwable t) {
      // Not much we can do here
      req.getErr().emit(t);
    }
  }

  @Override
  public void getChildren(final Client cl,
                          final BwCalendar val) throws Throwable {
    cl.resolveAlias(val, true, false);

    val.setChildren(cl.getChildren(val));

    if (val.getChildren() != null) {
      for (BwCalendar c: val.getChildren()) {
        cl.resolveAlias(c, true, false);

        getChildren(cl, c);
      }
    }
  }

  /** If a name is defined fetch it, or use the current filter if it exists
   *
   * @param filterName
   * @return BwFilter or null
   * @throws Throwable
   */
  private FilterBase getFilter(final BwRequest req,
                               final String filterName) throws Throwable {
    BwActionFormBase form = req.getBwForm();
    Client cl = req.getClient();

    BwFilterDef fdef = null;

    if (filterName != null) {
      fdef = cl.getFilter(filterName);

      if (fdef == null) {
        req.getErr().emit(ClientError.unknownFilter, filterName);
      }
    }

    if (fdef == null) {
      fdef = form.getCurrentFilter();
    }

    if (fdef == null) {
      return null;
    }

    if (fdef.getFilters() == null) {
      try {
        cl.parseFilter(fdef);
      } catch (CalFacadeException cfe) {
        req.getErr().emit(cfe);
      }
    }

    return fdef.getFilters();
  }

  /**
    Does the given string represent a rootless URI?  A URI is rootless
    if it is not absolute (that is, does not contain a scheme like 'http')
    and does not start with a '/'
    @param uri String to test
    @return Is the string a rootless URI?  If the string is not a valid
      URI at all (for example, it is null), returns false
   */
  private boolean rootlessUri(final String uri) {
    try {
      return !((uri == null) || uri.startsWith("/") || new URI(uri).isAbsolute());
    } catch (URISyntaxException e) {  // not a URI at all
      return false;
    }
  }

  private void setSessionNum(final String name) {
    try {
      synchronized (countsMap) {
        Counts c = countsMap.get(name);

        if (c == null) {
          c = new Counts();
          countsMap.put(name, c);
        }

        sessionNum = c.totalSessions;
        c.totalSessions++;
      }
    } catch (Throwable t) {
    }
  }

  /** Embed the current users calendars. For admin or guest mode this is the
   * same as calling embedPublicCalendars.
   *
   * <p>For the websubmit application we embed the root of the submission
   * calendars.
   *
   * @param request
   */
  protected void embedCollections(final BwRequest request) throws Throwable {
    BwCalendar calendar = null;
    BwActionFormBase form = request.getBwForm();
    Client cl = request.getClient();

    try {
      if (form.getSubmitApp()) {
        // Use submission root
        calendar = cl.getCollection(
                form.getConfig().getSubmissionRoot());
      } else {
        // Current owner
        calendar = cl.getHome();
      }

      if (calendar != null) {
        Set<String> cos = form.getCalendarsOpenState();

        if (cos != null) {
          calendar.setOpen(cos.contains(calendar.getPath()));
        }
      }

      getChildren(cl, calendar);
    } catch (Throwable t) {
      request.getErr().emit(t);
    }

    request.setSessionAttr(BwRequest.bwCollectionListName,
                           calendar);
  }

  protected void embedPublicCollections(final BwRequest request) throws Throwable {
    Client cl = request.getClient();
    BwCalendar calendar = cl.getPublicCalendars();

    getChildren(cl, calendar);

    request.setSessionAttr(BwRequest.bwPublicCollectionListName,
                           calendar);
  }

  protected void embedUserCollections(final BwRequest request) throws Throwable {
    BwCalendar calendar = null;
    BwActionFormBase form = request.getBwForm();
    Client cl = request.getClient();
    boolean publicAdmin = form.getConfig().getPublicAdmin();

    try {
      BwPrincipal p;

      if (publicAdmin) {
        // Use calendar suite owner
        p = cl.getPrincipal(
                form.getCurrentCalSuite().getGroup().getOwnerHref());
      } else {
        p = cl.getCurrentPrincipal();
      }

      calendar = cl.getHome(p, false);

      if (calendar != null) {
        Set<String> cos = form.getCalendarsOpenState();

        if (cos != null) {
          calendar.setOpen(cos.contains(calendar.getPath()));
        }
      }

      getChildren(cl, calendar);
    } catch (Throwable t) {
      request.getErr().emit(t);
    }

    request.setSessionAttr(BwRequest.bwUserCollectionListName,
                           calendar);
  }

  protected void embedPrefs(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwPreferencesName,
                           request.getClient().getPreferences());
  }

  protected void embedViews(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwViewsListName,
                           request.getClient().getAllViews());
  }
}
