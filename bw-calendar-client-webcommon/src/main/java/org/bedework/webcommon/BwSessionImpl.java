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
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.util.struts.Request;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

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
  private transient Logger log;

  protected boolean debug;

  //private static final String refreshTimeAttr = "bw_refresh_time";
  //private static final long refreshRate = 1 * 60 * 1000;

  /** Not completely valid in the j2ee world but it's only used to count sessions.
   */
  private static class Counts {
    long totalSessions = 0;
  }

  private final ConfigCommon config;

  private final boolean publicAdmin;

  private BwAuthUserPrefs curAuthUserPrefs;

  private static final ConcurrentHashMap<String, Counts> countsMap =
    new ConcurrentHashMap<>();
  private long sessionNum = 0;
  
  private static String publicCollectionsChangeToken;
  private static BwCalendar clonedPublicCollections;  

  /** The current user - null for guest
   */
  private String user;

  private AuthProperties authpars;

  private transient CollectionCollator<BwContact> contactCollator;
  private transient CollectionCollator<BwCategory> categoryCollator;
  private transient CollectionCollator<BwLocation> locationCollator;

  /** Constructor for a Session
   *
   * @param config     our config
   * @param user       String user id
   * @param appName    String identifying particular application
   * @throws Throwable
   */
  public BwSessionImpl(final ConfigCommon config,
                       final String user,
                       final String appName) throws Throwable {
    this.config = config;
    this.user = user;

    debug = getLogger().isDebugEnabled();

    publicAdmin = config.getPublicAdmin();

    setSessionNum(appName);
  }

  @Override
  public void reset(final Request req) {
    req.setSessionAttr(changeTokenAttr, "");
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

  @Override
  public long getSessionNum() {
    return sessionNum;
  }

  @Override
  public void setUser(final String val) {
    user = val;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public boolean isGuest() {
    return user == null;
  }

  @Override
  public void prepareRender(final BwRequest req) {
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();
    final BwModuleState mstate = req.getModule().getState();

    req.setRequestAttr(BwRequest.bwSearchParamsName,
                       cl.getSearchParams());

    req.setRequestAttr(BwRequest.bwSearchResultName,
                       mstate.getSearchResult());

    try {
      mstate.updateViewStartDate(req);

      final String cacheUrl = cl.getSystemProperties().getCacheUrlPrefix();
      if (cacheUrl != null) {
        req.setSessionAttr(BwRequest.bwCachePrefixName, cacheUrl);
      }

      final String featureFlags = cl.getSystemProperties().getFeatureFlags();
      if (featureFlags != null) {
        req.setSessionAttr(BwRequest.bwFeatureFlagsName, featureFlags);
      }
      
      if (cl.getPublicAdmin()) {
        final Collection<BwGroup> adgs;

        final BwPrincipal p = cl.getAuthPrincipal();
        if (p != null) {
          if (!cl.isSuperUser()) {
            // Always restrict to groups of which we are a member
            adgs = cl.getGroups(p);
          } else {
            adgs = cl.getAllGroups(false);
          }

          req.setSessionAttr(BwRequest.bwUserSearchableAdminGroups,
                             adgs);
        }
      }

      form.assignCalendarUserAddress(cl.getCurrentCalendarAddress());

      /* This only till we make module state the request scope form */
      if (form.getEventDates() == null) {
        form.assignEventDates(new EventDates(cl.getCurrentPrincipalHref(),
                                               mstate.getCalInfo(),
                                               form.getHour24(),
                                               form.getEndDateType(),
                                               config.getMinIncrement(),
                                               form.getErr()));
      }

      if (mstate.getEventDates() == null) {
        mstate.assignEventDates(form.getEventDates());
      }

      mstate.getEventDates().setHour24(form.getHour24());

      final String lastChangeToken = (String)req.getSessionAttr(changeTokenAttr);
      final String changeToken = cl.getCurrentChangeToken();

      final boolean needRefresh = (lastChangeToken == null) ||
              !lastChangeToken.equals(changeToken);

      if (needRefresh) {
        cl.flushCached();
      }

      req.setSessionAttr(changeTokenAttr, changeToken);

      //Long lastRefresh = (Long)req.getSessionAttr(refreshTimeAttr);
      //long now = System.currentTimeMillis();

      if (!mstate.getRefresh() || needRefresh) {
//              (lastRefresh == null) || (now - lastRefresh > refreshRate)) {
        // Implant various objects for the pages.

        final String appType = cl.getAppType();
        if (!BedeworkDefs.appTypeFeeder.equals(appType)) {
          embedFilters(req);

          if (!BedeworkDefs.appTypeWebpublic.equals(appType)) {
            if (debug) {
              debug("About to embed collections");
            }

            embedCollections(req);
          }

          if (debug) {
            debug("About to embed public collections");
          }

          embedPublicCollections(req);

          if (debug) {
            debug("About to embed user collections");
          }

          embedUserCollections(req);

          if (debug) {
            debug("About to embed views");
          }

          embedViews(req);
        }

        if (debug) {
          debug("About to embed prefs");
        }

        embedPrefs(req);

        if (debug) {
          debug("After embed prefs");
        }

        authpars = cl.getAuthProperties().cloneIt();
        form.setAuthPars(authpars);

        form.setEventRegAdminToken(
                cl.getSystemProperties().getEventregAdminToken());

        form.setCurrentGroups(cl.getCurrentPrincipal().getGroups());

        //req.setSessionAttr(refreshTimeAttr, now);
      }

      if (mstate.getRefresh() ||
              mstate.getCurTimeView() == null) {
        refreshView(req);
//        mstate.setRefresh(false);
      }
    } catch (final Throwable t) {
      // Not much we can do here
      form.getErr().emit(t);
    }
  }

  @Override
  public void embedFilters(final BwRequest req) throws Throwable {
    req.setSessionAttr(BwRequest.bwFiltersListName,
                       req.getClient().getAllFilters());
  }

  @Override
  public TimeView getCurTimeView(final BwRequest req) {
    final BwModuleState mstate = req.getModule().getState();

    if (mstate.getCurTimeView() == null) {
      refreshView(req);
    }

    return mstate.getCurTimeView();
  }

  @Override
  public AuthProperties getAuthpars() {
    return authpars;
  }

  @Override
  public void embedAddContentCalendarCollections(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwAddContentCollectionListName,
                       request.getClient().getAddContentCollections(!publicAdmin));
  }

  @Override
  public void embedCollections(final BwRequest request) throws Throwable {
    final BwCalendar col;
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();

    try {
      if (form.getSubmitApp()) {
        // Use submission root
        col = cl.getCollection(
                form.getConfig().getSubmissionRoot());
      } else {
        // Current owner
        col = cl.getHome();
      }

      if (col == null) {
        request.getErr().emit("No home collection");
      } else {
        if (debug) {
          debug("About to clone: " + col.getPath());
        }

        embedClonedCollection(request, col,
                              false,
                              BwRequest.bwCollectionListName);

        if (debug) {
          debug("Cloned: " + col.getPath());
        }
      }
    } catch (final Throwable t) {
      request.getErr().emit(t);
    }
  }

  protected void embedPublicCollections(final BwRequest request) throws Throwable {
    final Client cl = request.getClient();
    final String changeToken = cl.getCurrentChangeToken();
    final boolean fromCopy;
    
    fromCopy =  (publicCollectionsChangeToken != null) &&
            publicCollectionsChangeToken.equals(changeToken);

    final BwCalendar root;
    if (!fromCopy) {
      debug("Discarding cached public and rebuilding");
      root = cl.getPublicCalendars();
    } else {
      root = clonedPublicCollections;
    }
    
    publicCollectionsChangeToken = changeToken;
    clonedPublicCollections = embedClonedCollection(request,
                                                    root,
                                                    fromCopy,
                                                    BwRequest.bwPublicCollectionListName);
  }

  @Override
  public void embedUserCollections(final BwRequest request) throws Throwable {
    final BwCalendar col;
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();
    final boolean publicAdmin = form.getConfig().getPublicAdmin();

    try {
      final BwPrincipal p;

      if ((publicAdmin) && (form.getCurrentCalSuite() != null)) {
        // Use calendar suite owner
        p = cl.getPrincipal(
                form.getCurrentCalSuite().getGroup().getOwnerHref());
      } else {
        p = cl.getCurrentPrincipal();
      }

      col = cl.getHome(p, false);

      embedClonedCollection(request, col,
                            false,
                            BwRequest.bwUserCollectionListName);
    } catch (final Throwable t) {
      request.getErr().emit(t);
    }
  }

  /* ====================================================================
   *                   Categories
   * ==================================================================== */

  @Override
  public Collection<BwCategory> embedCategories(final BwRequest request,
                                                final boolean refresh,
                                                final int kind) throws Throwable {
    final String attrName;
    Collection <BwCategory> vals;

    if (kind == ownersEntity) {
      attrName = BwRequest.bwCategoriesListName;

      //noinspection unchecked
      vals = (Collection<BwCategory>)request.getSessionAttr(BwRequest.bwCategoriesListName);
      if (!refresh && vals  != null) {
        return vals;
      }

      vals = getCategoryCollection(request, ownersEntity, true);
    } else if (kind == editableEntity) {
      attrName = BwRequest.bwEditableCategoriesListName;

      //noinspection unchecked
      vals = (Collection<BwCategory>)request.getSessionAttr(BwRequest.bwEditableCategoriesListName);
      if (!refresh && vals  != null) {
        return vals;
      }

      vals = getCategoryCollection(request, editableEntity, false);
    } else if (kind == preferredEntity) {
      attrName = BwRequest.bwPreferredCategoriesListName;

      final Client cl = request.getClient();

      vals = cl.getCategories(curAuthUserPrefs.getCategoryPrefs().getPreferred());
    } else if (kind == defaultEntity) {
      attrName = BwRequest.bwDefaultCategoriesListName;

      //noinspection unchecked
      vals = (Set<BwCategory>)request.getSessionAttr(BwRequest.bwDefaultCategoriesListName);
      if (!refresh && vals  != null) {
        return vals;
      }

      vals = new TreeSet<>();

      final Client cl = request.getClient();

      final Set<String> catuids = cl.getPreferences().getDefaultCategoryUids();

      for (final String uid: catuids) {
        final BwCategory cat = cl.getCategory(uid);

        if (cat != null) {
          vals.add(cat);
        }
      }
    } else {
      throw new Exception("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName, vals);
    return vals;
  }

  @Override
  public Collection<BwCategory> getCategoryCollection(final BwRequest request,
                                                      final int kind,
                                                      final boolean forEventUpdate) throws Throwable {
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();
    Collection<BwCategory> vals = null;

    if (kind == ownersEntity) {
      final String appType = cl.getAppType();
      if (cl.getWebSubmit() ||
              BedeworkDefs.appTypeWebpublic.equals(appType) ||
              BedeworkDefs.appTypeFeeder.equals(appType)) {
        // Use public
        vals = cl.getPublicCategories();
      } else {
        // Current owner
        vals = cl.getCategories();

        final BwEvent ev = form.getEvent();

        if (!publicAdmin && forEventUpdate &&
                (ev != null) &&
                (ev.getCategories() != null)) {
          for (final BwCategory cat: ev.getCategories()) {
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

  /* ====================================================================
   *                   Contacts
   * ==================================================================== */

  @Override
  public void embedContactCollection(final BwRequest request,
                                     final int kind) throws Throwable {
    final Client cl = request.getClient();
    final Collection<BwContact> vals;
    final String attrName;

    if (kind == ownersEntity) {
      attrName = BwRequest.bwContactsListName;

      if (cl.getWebSubmit()) {
        // Use public
        vals = cl.getPublicContacts();
      } else {
        // Current owner
        vals = cl.getContacts();
      }
    } else if (kind == editableEntity) {
      attrName = BwRequest.bwEditableContactsListName;

      vals = cl.getEditableContacts();
    } else if (kind == preferredEntity) {
      attrName = BwRequest.bwPreferredContactsListName;

      vals = curAuthUserPrefs.getContactPrefs().getPreferred();
    } else {
      throw new Exception("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName,
                           getContactCollator().getCollatedCollection(vals));
  }

  /* ====================================================================
   *                   Locations
   * ==================================================================== */

  @Override
  public void embedLocations(final BwRequest request,
                             final int kind) throws Throwable {
    final Collection<BwLocation> vals;
    final String attrName;

    if (kind == ownersEntity) {
      attrName = BwRequest.bwLocationsListName;
      vals = getLocations(request, ownersEntity, true);
    } else if (kind == editableEntity) {
      attrName = BwRequest.bwEditableLocationsListName;

      vals = getLocations(request, editableEntity, false);
    } else if (kind == preferredEntity) {
      attrName = BwRequest.bwPreferredLocationsListName;

      vals = curAuthUserPrefs.getLocationPrefs().getPreferred();
    } else {
      throw new Exception("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName,
                           getLocationCollator().getCollatedCollection(vals));
  }

  @Override
  public void embedViews(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwViewsListName,
                           request.getClient().getAllViews());
  }

  @Override
  public Collection<BwLocation> getLocations(final BwRequest request,
                                             final int kind,
                                             final boolean forEventUpdate) {
    try {
      final BwActionFormBase form = request.getBwForm();
      final Client cl = request.getClient();
      Collection<BwLocation> vals = null;

      if (kind == ownersEntity) {
        if (cl.getWebSubmit()) {
          // Use public
          vals = cl.getPublicLocations();
        } else {
          // Current owner
          vals = cl.getLocations();

          final BwEvent ev = form.getEvent();

          if (!publicAdmin && forEventUpdate && (ev != null)) {
            final BwLocation loc = ev.getLocation();

            if ((loc != null) &&
                    (!loc.getOwnerHref().equals(cl.getCurrentPrincipalHref()))) {
              vals.add(loc);
            }
          }
        }
      } else if (kind == preferredEntity) {
        vals = curAuthUserPrefs.getLocationPrefs().getPreferred();
      } else if (kind == editableEntity) {
        vals = cl.getEditableLocations();
      }

      if (vals == null) {
        // Won't need this with 1.5
        throw new Exception("Software error - bad kind " + kind);
      }

      return getLocationCollator().getCollatedCollection(vals);
    } catch (final Throwable t) {
      t.printStackTrace();
      request.getErr().emit(t);
      return new ArrayList<>();
    }
  }

  /* ====================================================================
   *                   Package methods
   * ==================================================================== */

  /**
   * @param val prefs
   */
  void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
    curAuthUserPrefs = val;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private BwCalendar embedClonedCollection(final BwRequest request,
                                           final BwCalendar col,
                                           final boolean fromCopy,
                                           final String attrName) throws Throwable {
    final ColCloner cc = new ColCloner(request.getClient(),
                                       request.getBwForm().getCalendarsOpenState());
    final BwCalendar cloned;
    
    cloned = cc.deepClone(col, fromCopy);

    request.setSessionAttr(attrName, cloned);
    
    return cloned;
  }

  private void refreshView(final BwRequest req) {
    final BwModuleState mstate = req.getModule().getState();
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();

    try {
      /* First ensure we have the view period set */
      if (mstate.getCurTimeView() == null) {
        /** Figure out the default from the properties
         */
        String vn;

        try {
          vn = cl.getPreferences().getPreferredViewPeriod();
          if (vn == null) {
            vn = "week";
          }
        } catch (final Throwable t) {
          System.out.println("Exception setting current view");
          vn = "week";
        }

        if (mstate.getCurViewPeriod() < 0) {
          for (int i = 1; i < BedeworkDefs.viewPeriodNames.length; i++) {
            final String bwvt = BedeworkDefs.viewPeriodNames[i];
            if (bwvt.startsWith(vn)) {
              mstate.setViewType(bwvt);
              break;
            }
          }

          if (mstate.getViewType() == null) {
            mstate.setViewType(
                    BedeworkDefs.viewPeriodNames[BedeworkDefs.defaultView]);
          }

          mstate.setViewMcDate(new MyCalendarVO(new Date(System.currentTimeMillis())));
        }
      }

      /* Now get a view object */

      if (mstate.getViewMcDate() == null) {
        mstate.setViewMcDate(new MyCalendarVO(new Date(
                System.currentTimeMillis())));
      }

      /* If they said today switch to day */
      if (BedeworkDefs.vtToday.equals(mstate.getViewType())) {
        mstate.setViewType(BedeworkDefs.vtDay);
      }

      final FilterBase filter = getFilter(req, null);
      TimeView tv = null;

      switch (mstate.getViewType()) {
        case BedeworkDefs.vtToday:
        case BedeworkDefs.vtDay:
          tv = new DayView(form.getErr(),
                           mstate.getViewMcDate(),
                           filter);
          break;
        case BedeworkDefs.vtWeek:
          tv = new WeekView(form.getErr(),
                            mstate.getViewMcDate(),
                            filter);
          break;
        case BedeworkDefs.vtMonth:
          tv = new MonthView(form.getErr(),
                             mstate.getViewMcDate(),
                             filter);
          break;
        case BedeworkDefs.vtYear:
          tv = new YearView(form.getErr(),
                            mstate.getViewMcDate(),
                            form.getShowYearData(), filter);
          break;
      }

      mstate.setCurTimeView(tv);

      if (BedeworkDefs.appTypeWebuser.equals(cl.getAppType())) {
        cl.clearSearchEntries();
      }
    } catch (final Throwable t) {
      // Not much we can do here
      req.getErr().emit(t);
    }
  }

  /** If a name is defined fetch it, or use the current filter if it exists
   *
   * @param req the request
   * @param filterName a filter name
   * @return BwFilter or null
   * @throws Throwable
   */
  private FilterBase getFilter(final BwRequest req,
                               final String filterName) throws Throwable {
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();

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
      } catch (final CalFacadeException cfe) {
        req.getErr().emit(cfe);
      }
    }

    return fdef.getFilters();
  }

  private void setSessionNum(final String name) {
    try {
      Counts c = countsMap.get(name);

      if (c == null) {
        c = new Counts();
        c = countsMap.putIfAbsent(name, c);
      }

      sessionNum = c.totalSessions;
      c.totalSessions++;
    } catch (final Throwable ignored) {
    }
  }

  private CollectionCollator<BwCategory> getCategoryCollator() {
    if (categoryCollator == null) {
      categoryCollator = new CollectionCollator<>();
    }

    return categoryCollator;
  }

  private CollectionCollator<BwContact> getContactCollator() {
    if (contactCollator == null) {
      contactCollator = new CollectionCollator<>();
    }

    return contactCollator;
  }

  private CollectionCollator<BwLocation> getLocationCollator() {
    if (locationCollator == null) {
      locationCollator = new CollectionCollator<>();
    }

    return locationCollator;
  }

  protected void embedPrefs(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwPreferencesName,
                           request.getClient().getPreferences());
  }

  /** Get a logger for messages
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(getClass());
    }

    return log;
  }

  protected void debug(final String msg) {
    getLogger().debug(msg);
  }
}
