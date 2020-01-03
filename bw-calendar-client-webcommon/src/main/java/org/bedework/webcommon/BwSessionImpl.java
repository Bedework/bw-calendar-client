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
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.responses.CollectionsResponse;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.util.misc.response.Response;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.util.caching.FlushMap;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.struts.Request;

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

  private BwAuthUserPrefs curAuthUserPrefs;

  private static final ConcurrentHashMap<String, Counts> countsMap =
    new ConcurrentHashMap<>();
  private long sessionNum = 0;
  
  // Both indexed by isSuper
  private static final String[] publicCollectionsChangeToken = {null, null};
  private static final BwCalendar[] clonedPublicCollections = {null, null};

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
   */
  public BwSessionImpl(final ConfigCommon config,
                       final String user,
                       final String appName) {
    super();
    
    this.config = config;
    this.user = user;

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

        if (!BedeworkDefs.appTypeFeeder.equals(cl.getAppType())) {
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
  public void flushPublicCache() {
    clonedPublicCollections[0] = null;
    clonedPublicCollections[1] = null;
  }

  @Override
  public CollectionsResponse getCollections(final BwRequest req) {
    final CollectionsResponse cols = new CollectionsResponse();

    if ("true".equals(req.getStringActionPar("public="))) {
      if (req.getReqPar("cs") != null) {
        cols.setCollections(getUserCollections(req));
      } else {
        cols.setPublicCollections(getPublicCollections(req));
      }
    } else {
      cols.setUserCollections(getUserCollections(req));
    }

    /* TODO what's this
    if (!BedeworkDefs.appTypeWebpublic.equals(cl.getAppType())) {
      cols.setCollections(getAppCollections(req));
    }
    */
    
    cols.setStatus(Response.Status.ok);
    return cols;
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
        request.getErr().emit("Unable to clone home collection");
        return null;
      }

      if (debug()) {
        debug("Cloned: " + col.getPath());
      }
      
      return cloned;
    } catch (final Throwable t) {
      request.getErr().emit(t);
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
        request.getErr().emit("Unable to access public collections");
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
    } catch (final Throwable t) {
      request.getErr().emit(t);
      return null;
    }
  }

  /* Key is colpath of root.
   */
  private final static FlushMap<String, BwCalendar> publicUserCollections =
          new FlushMap<>();

  public BwCalendar getUserCollections(final BwRequest request) {
    final BwCalendar col;
    final BwActionFormBase form = request.getBwForm();
    final Client cl = request.getClient();
    final boolean publicAdmin = cl.getPublicAdmin(); //form.getConfig().getPublicAdmin();

    try {
      final BwPrincipal p;

      if (cl.getWebSubmit() || cl.getPublicAuth()) {
        // Use calsuite in form or default
        String calSuiteName = form.getCalSuiteName();
        if (calSuiteName == null) {
          calSuiteName = form.getConfig().getCalSuite();
          form.setCalSuiteName(calSuiteName);
        }

        if (calSuiteName == null) {
          error("No default calendar suite - nor one requested");
          return null;
        }

        final BwCalSuiteWrapper cs = cl.getCalSuite(calSuiteName);

        if (cs == null) {
          form.getErr().emit("No calendar suite with name ", calSuiteName);
          return null;
        }
        
        form.setCurrentCalSuite(cs);

        p = cl.getPrincipal(cs.getGroup().getOwnerHref());

        if (debug()) {
          debug("Get Calendar home for " + p.getPrincipalRef());
        }

        col = cl.getHome(p, false);
      } else {
        if ((publicAdmin) && (form.getCurrentCalSuite() != null)) {
          // Use calendar suite owner
          p = cl.getPrincipal(
                  form.getCurrentCalSuite().getGroup()
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
        request.getErr().emit("Unable to clone user collection");
        return null;
      }

      if (cl.isGuest()) {
        synchronized (publicUserCollections) {
          publicUserCollections.put(col.getPath(), cloned);
        }
      }

      return cloned;
    } catch (final Throwable t) {
      request.getErr().emit(t);
      return null;
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
      if (cl.getWebSubmit() || cl.getPublicAuth() ||
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
        throw new Exception("Software error - bad kind " + kind);
      }

      return getContactCollator().getCollatedCollection(vals);
    } catch (final Throwable t) {
      t.printStackTrace();
      request.getErr().emit(t);
      return new ArrayList<>();
    }
  }

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
      throw new RuntimeException("Software error - bad kind " + kind);
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

  private BwCalendar getClonedCollection(final BwRequest request,
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
   */
  private FilterBase getFilter(final BwRequest req,
                               @SuppressWarnings("SameParameterValue") final String filterName) {
    final BwActionFormBase form = req.getBwForm();
    final Client cl = req.getClient();

    BwFilterDef fdef = null;

    if (filterName != null) {
      final GetFilterDefResponse gfdr = cl.getFilter(filterName);

      if (gfdr.getStatus() == Response.Status.notFound) {
        form.getErr().emit(ClientError.unknownFilter, filterName);
        return null;
      }

      if (gfdr.getStatus() != Response.Status.ok) {
        form.getErr().emit(ClientError.exc, gfdr.getMessage());
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
        req.getErr().emit(pr.message);
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

  private void embedPrefs(final BwRequest request) throws Throwable {
    request.setSessionAttr(BwRequest.bwPreferencesName,
                           request.getClient().getPreferences());
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
