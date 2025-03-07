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
import org.bedework.appcommon.CalendarFormatter;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.WeekView;
import org.bedework.appcommon.YearView;
import org.bedework.appcommon.client.Client;
import org.bedework.base.exc.BedeworkClosed;
import org.bedework.base.exc.BedeworkException;
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
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.responses.CollectionsResponse;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.client.admin.AdminClient;
import org.bedework.client.rw.RWClient;
import org.bedework.util.caching.FlushMap;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.base.response.Response;
import org.bedework.util.webaction.Request;

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
public class BwSessionImpl implements Logged, BwSession {
  //private static final String refreshTimeAttr = "bw_refresh_time";
  //private static final long refreshRate = 1 * 60 * 1000;

  /** Not completely valid in the j2ee world but it's only used to count sessions.
   */
  private static class Counts {
    long totalSessions = 0;
  }

  private final ConfigCommon config;

  private final boolean publicAdmin;

  private boolean newSession = true;

  private BwAuthUserPrefs curAuthUserPrefs;

  private static final ConcurrentHashMap<String, Counts> countsMap =
    new ConcurrentHashMap<>();
  private long sessionNum = 0;
  
  // Both indexed by isSuper
  private static final String[] publicCollectionsChangeToken = {null, null};
  private static final BwCalendar[] clonedPublicCollections = {null, null};
  private static Collection<BwView> publicViews;
  private static long lastViewsRefresh;
  private static final long viewsRefreshInterval = 1000 * 60 * 5;

  private AuthProperties authpars;

  private transient CollectionCollator<BwContact> contactCollator;
  private transient CollectionCollator<BwCategory> categoryCollator;
  private transient CollectionCollator<BwLocation> locationCollator;

  /** Constructor for a Session
   *
   * @param config     our config
   * @param appName    String identifying particular application
   */
  public BwSessionImpl(final ConfigCommon config,
                       final String appName) {
    super();
    
    this.config = config;
    publicAdmin = config.getPublicAdmin();
    setSessionNum(appName);
  }

  @Override
  public void finishInit(final AuthProperties authpars) {
    this.authpars = authpars;
  }

  @Override
  public void reset(final Request req) {
    req.setSessionAttr(changeTokenAttr, "");
  }

  /* ============================================================
   *                     Property methods
   * ============================================================ */

  @Override
  public long getSessionNum() {
    return sessionNum;
  }

  @Override
  public boolean isNewSession() {
    return newSession;
  }

  @Override
  public void resetNewSession() {
    newSession = false;
  }

  @Override
  public void prepareRender(final BwRequest req) {
    final BwActionFormBase form = req.getBwForm();
    final var cl = req.getClient();
    final var mstate = req.getModule().getState();
    final var globals = req.getBwGlobals();

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
        final Collection<BwGroup<?>> adgs;

        final var p = cl.getAuthPrincipal();
        if (p != null) {
          final AdminClient adcl = (AdminClient)cl;
          if (!cl.isSuperUser()) {
            // Always restrict to groups of which we are a member
            adgs = adcl.getGroups(p);
          } else {
            adgs = adcl.getAllGroups(false);
          }

          req.setSessionAttr(BwRequest.bwUserSearchableAdminGroups,
                             adgs);
        }
      }

      form.assignCalendarUserAddress(cl.getCurrentCalendarAddress());

      if (mstate.getEventDates() == null) {
        mstate.assignEventDates(form.getEventDates());
      }

      //mstate.getEventDates().setHour24(req.getConfig().getHour24());

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

        if (Client.ClientType.feeder != cl.getClientType()) {
          embedFilters(req);

          if (cl.getPublicAdmin() || cl.getWebSubmit()) {
            if (debug()) {
              debug("About to embed collections");
            }

            embedCollections(req);
          }

          if (debug()) {
            debug("About to embed public collections");
          }

          embedPublicCollections(req);

          if (debug()) {
            debug("About to embed user collections");
          }

          embedUserCollections(req);

          if (debug()) {
            debug("About to embed views");
          }

          embedViews(req);
        }

        if (debug()) {
          debug("About to embed prefs");
        }

        embedPrefs(req);

        if (debug()) {
          debug("After embed prefs");
        }

        form.assignEventRegAdminToken(
                cl.getSystemProperties().getEventregAdminToken());
        form.assignEventRegWsUrl(
                cl.getSystemProperties().getEventregWSUrl());

        globals.setCurrentGroups(cl.getCurrentPrincipal().getGroups());

        //req.setSessionAttr(refreshTimeAttr, now);
      }

      if (mstate.getRefresh() ||
              mstate.getCurTimeView() == null) {
        refreshView(req);
//        mstate.setRefresh(false);
      }
    } catch (final BedeworkClosed bc) {
      // Pass it up
      throw bc;
    } catch (final Throwable t) {
      // Not much we can do here
      req.error(t);
    }
  }

  @Override
  public void flushPublicCache() {
    clonedPublicCollections[0] = null;
    clonedPublicCollections[1] = null;
  }

  @Override
  public CollectionsResponse getCollections(final BwRequest req) {
    final CollectionsResponse cols = new CollectionsResponse();

    if ("true".equals(req.getStringActionPar("public"))) {
      if (req.getReqPar("cs") != null) {
        cols.setCollections(getUserCollections(req));
      } else {
        cols.setPublicCollections(getPublicCollections(req));
      }
    } else {
      cols.setUserCollections(getUserCollections(req));
    }

    /* TODO what's this
    if (ClientType.publick != cl.getClientType())) {
      cols.setCollections(getAppCollections(req));
    }
    */
    
    cols.setStatus(Response.Status.ok);
    return cols;
  }

  @Override
  public void embedFilters(final BwRequest req) {
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
  public void embedAddContentCalendarCollections(final BwRequest request) {
    final RWClient cl = (RWClient)request.getClient();
    request.setSessionAttr(BwRequest.bwAddContentCollectionListName,
                       cl.getAddContentCollections());
  }

  @Override
  public void embedCollections(final BwRequest request) {
    final BwCalendar cloned = getAppCollections(request);

    if (cloned != null) {
      request.setSessionAttr(BwRequest.bwCollectionListName, cloned);
    } // TODO - if null should it be deleted?
  }

  @SuppressWarnings("WeakerAccess")
  protected void embedPublicCollections(final BwRequest request) {
    final BwCalendar cloned = getPublicCollections(request);

    if (cloned != null) {
      request.setSessionAttr(BwRequest.bwPublicCollectionListName,
                             cloned);
    } // TODO - if null should it be deleted?
  }

  @Override
  public void embedUserCollections(final BwRequest request) {
    final BwCalendar cloned = getUserCollections(request);

    if (cloned != null) {
      request.setSessionAttr(BwRequest.bwUserCollectionListName, cloned);
    } // TODO - if null should it be deleted?
  }

  public BwCalendar getAppCollections(final BwRequest request) {
    final BwCalendar col;
    final Client cl = request.getClient();

    try {
      if (cl.getWebSubmit()) {
        col = cl.getCollection(
                cl.getSystemProperties().getSubmissionRoot());
      } else {
        col = cl.getHome();
      }

      if (col == null) {
        request.error("No home collection");
        return null;
      } 

      if (col.getPath().equals(cl.getPublicCalendarsRootPath())) {
        if (debug()) {
          debug("Return public root");
        }

        return getPublicCollections(request);
      }
      
      if (debug()) {
        debug("About to clone: " + col.getPath());
      }

      final BwCalendar cloned = 
              getClonedCollection(request, col,
                                    false);

      if (cloned == null) {
        warn("Unable to clone " + col);
        request.error("Unable to clone home collection");
        return null;
      }

      if (debug()) {
        debug("Cloned: " + col.getPath());
      }
      
      return cloned;
    } catch (final BedeworkException be) {
      request.error(be);
      return null;
    }
  }
  
  protected BwCalendar getPublicCollections(final BwRequest request) {
    try {
      final Client cl = request.getClient();
      final String changeToken = cl.getCurrentChangeToken();
      final boolean fromCopy;
      // TODo fix all this as admins might have different access rights
      final int accessIndex;
      if (cl.isSuperUser()) {
        accessIndex = 0;
      } else {
        accessIndex = 1;
      }

      fromCopy = (clonedPublicCollections[accessIndex] != null) &&
              (publicCollectionsChangeToken[accessIndex] != null) &&
              publicCollectionsChangeToken[accessIndex].equals(changeToken);

      final BwCalendar root;
      if (!fromCopy) {
        debug("Discarding cached public and rebuilding");
        root = cl.getPublicCalendars();
      } else {
        root = clonedPublicCollections[accessIndex];
      }

      if (root == null) {
        request.error("Unable to access public collections");
        warn("Unable to access public collections");
        return null;
      }

      if (fromCopy && cl.isGuest()) {
        // Use the cloned calendars. No need to copy
        debug("Using cloned copy");
        return root;
      }

      clonedPublicCollections[accessIndex] =
              getClonedCollection(request,
                                  root,
                                  fromCopy);
      if (clonedPublicCollections[accessIndex] == null) {
        return null;
      }

      publicCollectionsChangeToken[accessIndex] = changeToken;

      return clonedPublicCollections[accessIndex];
    } catch (final BedeworkException be) {
      request.error(be);
      return null;
    }
  }

  /* Key is colpath of root.
   */
  private final static FlushMap<String, BwCalendar> publicUserCollections =
          new FlushMap<>();

  public BwCalendar getUserCollections(final BwRequest request) {
    final BwCalendar col;
    final var globals = request.getBwGlobals();
    final var cl = request.getClient();
    final boolean publicAdmin = cl.getPublicAdmin(); //form.getConfig().getPublicAdmin();

    final BwPrincipal<?> p;

    if (cl.getWebSubmit() || cl.getPublicAuth()) {
      // Use calsuite in form or default
      String calSuiteName = globals.getCalSuiteName();
      if (calSuiteName == null) {
        calSuiteName = request.getConfig().getCalSuite();
        globals.setCalSuiteName(calSuiteName);
      }

      if (calSuiteName == null) {
        error("No default calendar suite - nor one requested");
        return null;
      }

      final BwCalSuiteWrapper cs = cl.getCalSuite(calSuiteName);

      if (cs == null) {
        request.error("No calendar suite with name ", calSuiteName);
        return null;
      }
        
      globals.setCurrentCalSuite(cs);

      p = cl.getPrincipal(cs.getGroup().getOwnerHref());

      if (debug()) {
        debug("Get Calendar home for " + p.getPrincipalRef());
      }

      col = cl.getHome(p, false);
    } else {
      if ((publicAdmin) && (globals.getCurrentCalSuite() != null)) {
        // Use calendar suite owner
        p = cl.getPrincipal(
                globals.getCurrentCalSuite().getGroup()
                    .getOwnerHref());
      } else {
        p = cl.getCurrentPrincipal();
      }

      if (debug()) {
        debug("Get Calendar home for " + p.getPrincipalRef());
      }

      col = cl.getHome(p, false);
    }
      
    if (col == null) {
      warn("No home collection for " + p.getPrincipalRef());
      return null;
    }

    if (cl.isGuest() || cl.getPublicAuth()) {
      final BwCalendar cloned = publicUserCollections.get(
              col.getPath());

      if (cloned != null) {
        if (debug()) {
          debug("Use cloned from map for " + col.getPath());
        }

        return cloned;
      }
    }

    final BwCalendar cloned = getClonedCollection(request, col,
                                                  false);

    if (cloned == null) {
      warn("Unable to clone " + col);
      request.error("Unable to clone user collection");
      return null;
    }

    if (cl.isGuest()) {
      synchronized (publicUserCollections) {
        publicUserCollections.put(col.getPath(), cloned);
      }
    }

    return cloned;
  }

  /* ==============================================================
   *                   Categories
   * ============================================================== */

  @Override
  public Collection<BwCategory> embedCategories(
          final BwRequest request,
          final boolean refresh,
          final int kind) {
    final String attrName;
    Collection <BwCategory> vals;

    if (kind == ownersEntity) {
      attrName = BwRequest.bwCategoriesListName;

      //noinspection unchecked
      vals = (Collection<BwCategory>)request.getSessionAttr(attrName);
      if (!refresh && vals != null) {
        return vals;
      }

      vals = getCategoryCollection(request, ownersEntity, true);
    } else if (kind == editableEntity) {
      attrName = BwRequest.bwEditableCategoriesListName;

      //noinspection unchecked
      vals = (Collection<BwCategory>)request.getSessionAttr(attrName);
      if (!refresh && vals != null) {
        return vals;
      }

      vals = getCategoryCollection(request, editableEntity, false);
    } else if (kind == preferredEntity) {
      attrName = BwRequest.bwPreferredCategoriesListName;

      vals = curAuthUserPrefs.getCategoryPrefs().getPreferred();
    } else if (kind == defaultEntity) {
      attrName = BwRequest.bwDefaultCategoriesListName;

      //noinspection unchecked
      vals = (Set<BwCategory>)request.getSessionAttr(attrName);
      if (!refresh && vals != null) {
        return vals;
      }

      vals = new TreeSet<>();

      final Client cl = request.getClient();

      final Set<String> catuids = cl.getPreferences().getDefaultCategoryUids();

      for (final String uid: catuids) {
        final BwCategory cat = cl.getCategoryByUid(uid);

        if (cat != null) {
          vals.add(cat);
        }
      }
    } else {
      throw new RuntimeException("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName, vals);
    return vals;
  }

  @Override
  public Collection<BwCategory> getCategoryCollection(
          final BwRequest request,
          final int kind,
          final boolean forEventUpdate) {
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();
    Collection<BwCategory> vals = null;

    if (kind == ownersEntity) {
      final var clientType = cl.getClientType();
      if (cl.getWebSubmit() || cl.getPublicAuth() ||
              (Client.ClientType.publick == clientType) ||
              (Client.ClientType.feeder == clientType)) {
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

  /* ==============================================================
   *                   Contacts
   * ============================================================== */

  @Override
  public Collection<BwContact> getContacts(final BwRequest request,
                                           final int kind,
                                           final boolean forEventUpdate) {
    try {
      final BwActionFormBase form = request.getBwForm();
      final Client cl = request.getClient();
      Collection<BwContact> vals = null;

      if (kind == ownersEntity) {
        if (cl.getWebSubmit()) {
          // Use public
          vals = cl.getPublicContacts();
        } else {
          // Current owner
          vals = cl.getContacts();

          final BwEvent ev = form.getEvent();

          if (!publicAdmin && forEventUpdate && (ev != null)) {
            final BwContact ent = ev.getContact();

            if ((ent != null) &&
                    (!ent.getOwnerHref().equals(cl.getCurrentPrincipalHref()))) {
              vals.add(ent);
            }
          }
        }
      } else if (kind == preferredEntity) {
        vals = curAuthUserPrefs.getContactPrefs().getPreferred();
      } else if (kind == editableEntity) {
        vals = cl.getEditableContacts();
      }

      if (vals == null) {
        // Won't need this with 1.5
        throw new RuntimeException("Software error - bad kind " + kind);
      }

      return getContactCollator().getCollatedCollection(vals);
    } catch (final BedeworkException be) {
      request.error(be);
      return new ArrayList<>();
    }
  }

  @Override
  public void embedContactCollection(final BwRequest request,
                                     final int kind) {
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
      throw new RuntimeException("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName,
                           getContactCollator().getCollatedCollection(vals));
  }

  /* ==============================================================
   *                   Locations
   * ============================================================== */

  @Override
  public void embedLocations(final BwRequest request,
                             final int kind) {
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
      throw new BedeworkException("Software error - bad kind " + kind);
    }

    request.setSessionAttr(attrName,
                           getLocationCollator().getCollatedCollection(vals));
  }

  @Override
  public void embedViews(final BwRequest request) {
    final var cl = request.getClient();
    if (cl.isGuest() && (publicViews != null) &&
            (System.currentTimeMillis() <
                     (lastViewsRefresh + viewsRefreshInterval))) {
      request.setSessionAttr(BwRequest.bwViewsListName,
                             publicViews);
      return;
    }

    final var views = cl.getAllViews();

    if (cl.isGuest() && (views != null)) {
      final var publicRoot = getPublicCollections(request);
      if (publicRoot != null) {
        for (final var view: views) {
          final var collectionPaths = view.getCollectionPaths();
          final var collections = new ArrayList<BwCalendar>();

          if (collectionPaths != null) {
            for (final var path: collectionPaths) {
              final var col = findCollection(publicRoot, path);
              if (col != null) {
                collections.add(col);
              }
            }
          }

          view.setCollections(collections);
        }
      }

      lastViewsRefresh = System.currentTimeMillis();
      publicViews = views;
    }

    request.setSessionAttr(BwRequest.bwViewsListName,
                           views);
  }

  private BwCalendar findCollection(final BwCalendar root,
                                    final String path) {
    if (path.equals(root.getPath())) {
      return root;
    }

    if (path.startsWith(root.getPath() + "/")) {
      final var children = root.getChildren();
      if (children == null) {
        return null;
      }

      for (final var child: children) {
        final var childCol = findCollection(child, path);
        if (childCol != null) {
          return childCol;
        }
      }
    }

    return null;
  }

  @Override
  public Collection<BwLocation> getLocations(
          final BwRequest request,
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
      request.error(t);
      return new ArrayList<>();
    }
  }

  /* ====================================================================
   *                   Package methods
   * ==================================================================== */

  /**
   * @param val prefs
   */
  public void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
    curAuthUserPrefs = val;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private BwCalendar getClonedCollection(
          final BwRequest request,
          final BwCalendar col,
          final boolean fromCopy)  {
    final ColCloner cc = new ColCloner(request.getClient(),
                                       request.getBwForm().getCalendarsOpenState());
    final ColCloner.CloneResult clres = cc.deepClone(col, fromCopy);
    
    if (!clres.isOk()) {
      warn("getClonedCollection failed: " + clres.getStatus() +
                   " " + clres.getMessage());
    }

    return clres.getCol();
  }

  /*
  private void resetOpenState(final BwRequest request,
                              final BwCalendar col) {
    resetOpenStates(col, request.getBwForm().getCalendarsOpenState()); 
  }

  private void resetOpenStates(final BwCalendar col,
                               final Set<String> openStates) {
    if (openStates == null) {
      return;
    }
    col.setOpen(openStates.contains(col.getPath()));
    
    if (col.getCollectionInfo().childrenAllowed &&
            !Util.isEmpty(col.getChildren())) {
      for (final BwCalendar chCol: col.getChildren()) {
        resetOpenStates(chCol, openStates);
      }
    }
    
  }*/
  
  private void refreshView(final BwRequest req) {
    final BwModuleState mstate = req.getModule().getState();
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();

    try {
      /* First ensure we have the view period set */
      if (mstate.getCurTimeView() == null) {
        /* Figure out the default from the properties
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

          mstate.setViewMcDate(new CalendarFormatter(new Date(System.currentTimeMillis())));
        }
      }

      /* Now get a view object */

      if (mstate.getViewMcDate() == null) {
        mstate.setViewMcDate(new CalendarFormatter(new Date(
                System.currentTimeMillis())));
      }

      /* If they said today switch to day */
      if (BedeworkDefs.vtToday.equals(mstate.getViewType())) {
        mstate.setViewType(BedeworkDefs.vtDay);
      }

      final FilterBase filter = getFilter(req, null);
      final TimeView tv = switch (mstate.getViewType()) {
        case BedeworkDefs.vtToday, BedeworkDefs.vtDay ->
                new DayView(req.getErr(),
                            mstate.getViewMcDate(),
                            filter);
        case BedeworkDefs.vtWeek ->
                new WeekView(req.getErr(),
                             mstate.getViewMcDate(),
                             filter);
        case BedeworkDefs.vtMonth ->
                new MonthView(req.getErr(),
                              mstate.getViewMcDate(),
                              filter);
        case BedeworkDefs.vtYear ->
                new YearView(req.getErr(),
                             mstate.getViewMcDate(),
                             req.getConfig().getShowYearData(),
                             filter);
        default -> null;
      };

      mstate.setCurTimeView(tv);

      if (Client.ClientType.personal == cl.getClientType()) {
        cl.clearSearchEntries();
      }
    } catch (final Throwable t) {
      // Not much we can do here
      req.error(t);
    }
  }

  /** If a name is defined fetch it, or use the current filter if it exists
   *
   * @param req the request
   * @param filterName a filter name
   * @return BwFilter or null
   */
  private FilterBase getFilter(final BwRequest req,
                               @SuppressWarnings("SameParameterValue") final String filterName) {
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();

    BwFilterDef fdef = null;

    if (filterName != null) {
      final GetFilterDefResponse gfdr = cl.getFilter(filterName);

      if (gfdr.getStatus() == Response.Status.notFound) {
        req.error(ClientError.unknownFilter, filterName);
        return null;
      }

      if (gfdr.getStatus() != Response.Status.ok) {
        req.error(ClientError.exc, gfdr.getMessage());
        return null;
      }
      
      fdef = gfdr.getFilterDef();
    }

    if (fdef == null) {
      fdef = form.getCurrentFilter();
    }

    if (fdef == null) {
      return null;
    }

    if (fdef.getFilters() == null) {
      final ParseResult pr = cl.parseFilter(fdef);
      if (!pr.ok) {
        req.error(pr.message);
      }
    }

    return fdef.getFilters();
  }

  private void setSessionNum(final String name) {
    final Counts c = countsMap.computeIfAbsent(name, k -> new Counts());

    sessionNum = c.totalSessions;
    c.totalSessions++;
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

  private void embedPrefs(final BwRequest request) {
    request.setSessionAttr(BwRequest.bwPreferencesName,
                           request.getClient().getPreferences());
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
