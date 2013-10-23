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
import org.bedework.appcommon.CollectionCollator;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.DayView;
import org.bedework.appcommon.MonthView;
import org.bedework.appcommon.MyCalendarVO;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.WeekView;
import org.bedework.appcommon.YearView;
import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.util.servlet.filters.PresentationState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

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

  private final boolean isPortlet;

  private final ConfigCommon config;

  private boolean publicAdmin;

  private BwAuthUserPrefs curAuthUserPrefs;

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

  private SystemProperties syspars;

  private transient CollectionCollator<BwCategory> categoryCollator;
  private transient CollectionCollator<BwLocation> locationCollator;

  /** Constructor for a Session
   *
   * @param isPortlet
   * @param config
   * @param user       String user id
   * @param appName    String identifying particular application
   * @param ps
   * @throws Throwable
   */
  public BwSessionImpl(final boolean isPortlet,
                       final ConfigCommon config,
                       final String user,
                       final String appName,
                       final PresentationState ps) throws Throwable {
    this.isPortlet = isPortlet;
    this.config = config;
    this.user = user;
    this.appName = appName;
    this.ps = ps;

    publicAdmin = config.getPublicAdmin();

    if (ps != null) {
      /*
      if (ps.getAppRoot() == null) {
        ps.setAppRoot(prefixUri(schemeHostPort, appRoot));
      }

      if (ps.getBrowserResourceRoot() == null) {
        ps.setBrowserResourceRoot(prefixUri(schemeHostPort, browserResourceRoot));
      }
      */
      ps.setAppRoot(suffixRoot(config.getAppRoot()));
      ps.setBrowserResourceRoot(suffixRoot(config.getBrowserResourceRoot()));
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
    Client cl = req.getClient();

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

        syspars = cl.getSystemProperties();
        form.setEventRegAdminToken(syspars.getEventregAdminToken());

        form.setCurrentGroups(cl.getCurrentPrincipal().getGroups());

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

  public TimeView getCurTimeView(final BwRequest req) {
    BwActionFormBase form = req.getBwForm();

    if (form.getCurTimeView() == null) {
      refreshView(req);
    }

    return form.getCurTimeView();
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

      if ((publicAdmin) && (form.getCurrentCalSuite() != null)) {
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

  /* ====================================================================
   *                   Categories
   * ==================================================================== */

  @Override
  public void embedCategories(final BwRequest request,
                              final boolean refresh) throws Throwable {
    if (!refresh &&
            request.getSessionAttr(BwRequest.bwCategoriesListName) != null) {
      return;
    }

    request.setSessionAttr(BwRequest.bwCategoriesListName,
                           getCategoryCollection(request,
                                                 ownersEntity, true));
  }

  @Override
  public void embedEditableCategories(final BwRequest request,
                                      final boolean refresh) throws Throwable {
    if (!refresh &&
            request.getSessionAttr(BwRequest.bwEditableCategoriesListName) != null) {
      return;
    }

    request.setSessionAttr(BwRequest.bwEditableCategoriesListName,
                           getCategoryCollection(request,
                                                 editableEntity, false));
  }

  @Override
  public Set<BwCategory> embedDefaultCategories(final BwRequest request,
                                                final boolean refresh) throws Throwable {
    Set<BwCategory> cats;

    if (!refresh) {
      cats = (Set<BwCategory>)request.getSessionAttr(BwRequest.bwDefaultCategoriesListName);
      if (cats != null) {
        return cats;
      }
    }

    cats = new TreeSet<>();

    Client cl = request.getClient();

    Set<String> catuids = cl.getPreferences().getDefaultCategoryUids();

    for (String uid: catuids) {
      BwCategory cat = cl.getCategory(uid);

      if (cat != null) {
        cats.add(cat);
      }
    }

    request.setSessionAttr(BwRequest.bwDefaultCategoriesListName,
                           cats);

    return cats;
  }

  /* ====================================================================
   *                   Locations
   * ==================================================================== */

  @Override
  public void embedLocations(final BwRequest request) {
    request.setSessionAttr(BwRequest.bwLocationsListName,
                           getLocations(request, ownersEntity, true));
  }

  @Override
  public void embedEditableLocations(final BwRequest request) {
    request.setSessionAttr(BwRequest.bwEditableLocationsListName,
                           getLocations(request, editableEntity, false));
  }

  @Override
  public void embedPreferredLocations(final BwRequest request) {
    request.setSessionAttr(BwRequest.bwPreferredLocationsListName,
                           getLocationCollator().getCollatedCollection(
                                   curAuthUserPrefs.getLocationPrefs().getPreferred()));
  }

  /* ====================================================================
   *                   Package methods
   * ==================================================================== */

  /**
   * @param val
   */
  void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
    curAuthUserPrefs = val;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private void refreshView(final BwRequest req) {
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
          tv = new DayView(form.getErr(),
                           form.getViewMcDate(),
                           filter);
          break;
        case BedeworkDefs.dayView:
          tv = new DayView(form.getErr(),
                           form.getViewMcDate(),
                           filter);
          break;
        case BedeworkDefs.weekView:
          tv = new WeekView(form.getErr(),
                            form.getViewMcDate(),
                            filter);
          break;
        case BedeworkDefs.monthView:
          tv = new MonthView(form.getErr(),
                             form.getViewMcDate(),
                             filter);
          break;
        case BedeworkDefs.yearView:
          tv = new YearView(form.getErr(),
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

  /* Kind of entity we are referring to */

  private static int ownersEntity = 1;
  private static int editableEntity = 2;

  private Collection<BwCategory> getCategoryCollection(final BwRequest request,
                                                       final int kind,
                                                       final boolean forEventUpdate) throws Throwable {
    BwActionFormBase form = request.getBwForm();
    Client cl = request.getClient();
    Collection<BwCategory> vals = null;

    if (kind == ownersEntity) {

      String appType = cl.getAppType();
      if (BedeworkDefs.appTypeWebsubmit.equals(appType) ||
              BedeworkDefs.appTypeWebpublic.equals(appType) ||
              BedeworkDefs.appTypeFeeder.equals(appType)) {
        // Use public
        vals = cl.getCategories(cl.getPublicUser().getPrincipalRef());
      } else {
        // Current owner
        vals = cl.getCategories();

        BwEvent ev = form.getEvent();

        if (!publicAdmin && forEventUpdate &&
                (ev != null) &&
                (ev.getCategories() != null)) {
          for (BwCategory cat: ev.getCategories()) {
            if (!cat.getOwnerHref().equals(cl.getCurrentPrincipalHref())) {
              vals.add(cat);
            }
          }
        }
      }
    } else if (kind == editableEntity) {
      vals = cl.getEditableCategories();
    }

    if (vals == null) {
      return null;
    }

    return getCategoryCollator().getCollatedCollection(vals);
  }

  private CollectionCollator<BwCategory> getCategoryCollator() {
    if (categoryCollator == null) {
      categoryCollator = new CollectionCollator<BwCategory>();
    }

    return categoryCollator;
  }

  private Collection<BwLocation> getLocations(final BwRequest request,
                                              final int kind,
                                              final boolean forEventUpdate) {
    try {
      BwActionFormBase form = request.getBwForm();
      Client cl = request.getClient();
      Collection<BwLocation> vals = null;

      if (kind == ownersEntity) {
        String appType = cl.getAppType();
        if (BedeworkDefs.appTypeWebsubmit.equals(appType)) {
          // Use public
          vals = cl.getLocations(
                  cl.getPublicUser().getPrincipalRef());
        } else {
          // Current owner
          vals = cl.getLocations();

          BwEvent ev = form.getEvent();

          if (!publicAdmin && forEventUpdate && (ev != null)) {
            BwLocation loc = ev.getLocation();

            if ((loc != null) &&
                    (!loc.getOwnerHref().equals(cl.getCurrentPrincipalHref()))) {
              vals.add(loc);
            }
          }
        }
      } else if (kind == editableEntity) {
        vals = cl.getEditableLocations();
      }

      if (vals == null) {
        // Won't need this with 1.5
        throw new Exception("Software error - bad kind " + kind);
      }

      return getLocationCollator().getCollatedCollection(vals);
    } catch (Throwable t) {
      t.printStackTrace();
      request.getErr().emit(t);
      return new ArrayList<>();
    }
  }

  private CollectionCollator<BwLocation> getLocationCollator() {
    if (locationCollator == null) {
      locationCollator = new CollectionCollator<BwLocation>();
    }

    return locationCollator;
  }

  protected void embedPrefs(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwPreferencesName,
                           request.getClient().getPreferences());
  }

  protected void embedViews(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwViewsListName,
                           request.getClient().getAllViews());
  }

  private String suffixRoot(final String val) throws Throwable {
    StringBuilder sb = new StringBuilder(val);

    /* If we're running as a portlet change the app root to point to a
     * portlet specific directory.
     */
    String portalPlatform = config.getPortalPlatform();

    if (isPortlet && (portalPlatform != null)) {
      sb.append(".");
      sb.append(portalPlatform);
    }

    /* If calendar suite is non-null append that. */
    String calSuite = config.getCalSuite();
    if (calSuite != null) {
      sb.append(".");
      sb.append(calSuite);
    }

    return sb.toString();
  }
}
