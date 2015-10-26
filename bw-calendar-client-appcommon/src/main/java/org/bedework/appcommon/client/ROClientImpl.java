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
package org.bedework.appcommon.client;

import org.bedework.access.Ace;
import org.bedework.access.Acl;
import org.bedework.access.PrivilegeDefs;
import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.CollectionCollator;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.EventKey;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwProperty;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.SubContext;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.base.CategorisedEntity;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.BasicSystemProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.filter.SortTerm;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResult;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.CalSvcFactoryDefault;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.CalSvcIPars;
import org.bedework.calsvci.CalendarsI.SynchStatusResponse;
import org.bedework.calsvci.SchedulingI;
import org.bedework.calsvci.SharingI;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.sysevents.events.HttpEvent;
import org.bedework.sysevents.events.HttpOutEvent;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.util.misc.Util;
import org.bedework.util.struts.Request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import static org.bedework.calsvci.CalSuitesI.ResourceClass;

/**
 * User: douglm Date: 6/27/13 Time: 2:03
 */
public class ROClientImpl implements Client {
  protected boolean debug;

  protected transient Logger log;

  protected String id;

  protected CalSvcIPars pars;

  protected CalSvcI svci;

  protected ObjectMapper mapper = new ObjectMapper(); // create once, reuse

  protected boolean publicView;

  protected boolean superUser;

  protected boolean publicAdmin;

  protected BwPrincipal currentPrincipal;
  private String currentCalendarAddress;

  private Collection<Locale>supportedLocales;

  private final ClientState cstate;

  private transient CollectionCollator<BwCalendar> calendarCollator;
  protected String appType;

  private BwIndexer publicIndexer;
  private BwIndexer userIndexer;
  private SearchResult lastSearch;
  private List<SearchResultEntry> lastSearchEntries;

  protected boolean defaultFilterContextSet;
  protected FilterBase defaultFilterContext;

  /* Set this whenever an update occurs. We may want to delay or flush
   */
  protected long lastUpdate;

  /* Don't delay or flush until after end of request in which we
     updated.
   */
  protected long requestEnd;
  private String viewMode;

  /* The list of cloned admin groups for the use of the user client
   */
  protected static Collection<BwGroup> adminGroupsInfo;

  protected static Collection<BwGroup> calsuiteAdminGroupsInfo;

  /** Hrefs of owners for this calsuite
   */
  protected static List<String> ownerHrefs;

  private static long lastAdminGroupsInfoRefresh;
  static long adminGroupsInfoRefreshInterval = 1000 * 60 * 5;

  private static final Object adminGroupLocker = new Object();

  protected class AccessChecker implements BwIndexer.AccessChecker {
    @Override
    public Acl.CurrentAccess checkAccess(final BwShareableDbentity ent,
                                         final int desiredAccess,
                                         final boolean returnResult)
            throws CalFacadeException {
      return svci.checkAccess(ent, desiredAccess, returnResult);
    }
  }

  protected AccessChecker accessChecker = new AccessChecker();

  /**
   *
   * @param id identify the client - usually module name
   * @param authUser account
   * @param runAsUser account
   * @param calSuiteName the calednar suite
   * @param appType type of application: submit, admin etc
   * @param publicView true for the public RO client
   * @throws CalFacadeException
   */
  public ROClientImpl(final String id,
                      final String authUser,
                      final String runAsUser,
                      final String calSuiteName,
                      final String appType,
                      final boolean publicView)
          throws CalFacadeException {
    this(id);

    reinit(authUser, runAsUser, calSuiteName, appType, publicView);
  }

  protected ROClientImpl(final String id) {
    this.id = id;
    cstate = new ClientState(this);

    debug = getLogger().isDebugEnabled();
    if (debug) {
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    final DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

    mapper.setDateFormat(df);

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public void reinit(final String authUser,
                     final String runAsUser,
                     final String calSuiteName,
                     final String appType,
                     final boolean publicView)
          throws CalFacadeException {
    currentPrincipal = null;
    this.appType = appType;

    pars = new CalSvcIPars("roclient-" + id,
                           authUser,
                           runAsUser,
                           calSuiteName,
                           false, // publicAdmin,
                           false, // Allow non-admin super user
                           false, // service
                           getWebSubmit(),
                           false, // adminCanEditAllPublicCategories,
                           false, // adminCanEditAllPublicLocations,
                           false, // adminCanEditAllPublicSponsors,
                           false);    // sessionless
    svci = new CalSvcFactoryDefault().getSvc(pars);
    this.publicView = publicView;
  }

  @Override
  public Client copy(final String id) throws CalFacadeException {
    final ROClientImpl cl = new ROClientImpl(id);

    cl.pars = (CalSvcIPars)pars.clone();
    cl.pars.setLogId(id);

    cl.svci = new CalSvcFactoryDefault().getSvc(cl.pars);
    cl.publicView = publicView;

    return cl;
  }

  @Override
  public void requestIn(final int conversationType)
          throws CalFacadeException {
    postNotification(new HttpEvent(SysEventBase.SysCode.WEB_IN));
    svci.setState("Request in");

    if (conversationType == Request.conversationTypeUnknown) {
      svci.open();
      svci.beginTransaction();
      return;
    }

    if (svci.isRolledback()) {
      svci.close();
    }

    if (conversationType == Request.conversationTypeOnly) {
              /* if a conversation is already started on entry, end it
                  with no processing of changes. */
      if (svci.isOpen()) {
        svci.setState("Request in - close");
        svci.endTransaction();
      }
    }

    if (conversationType == Request.conversationTypeProcessAndOnly) {
      if (svci.isOpen()) {
        svci.setState("Request in - flush");
        svci.flushAll();
        svci.endTransaction();
        svci.close();
      }
    }

    svci.open();
    svci.beginTransaction();
    svci.setState("Request in - started");
  }

  @Override
  public void requestOut(final int conversationType,
                         final int actionType,
                         final long reqTimeMillis)
          throws CalFacadeException {
    requestEnd = System.currentTimeMillis();
    postNotification(
            new HttpOutEvent(SysEventBase.SysCode.WEB_OUT,
                             reqTimeMillis));
    svci.setState("Request out");

    if (!isOpen()) {
      return;
    }

    if (conversationType == Request.conversationTypeUnknown) {
      if (actionType != Request.actionTypeAction) {
        flushAll();
      }
    } else {
      if ((conversationType == Request.conversationTypeEnd) ||
              (conversationType == Request.conversationTypeOnly)) {
        flushAll();
      }
    }

    svci.endTransaction();
    svci.setState("Request out - ended");
  }

  @Override
  public boolean isOpen() {
    return svci.isOpen();
  }

  @Override
  public void close() throws CalFacadeException {
    svci.close();
  }

  @Override
  public void flushAll() throws CalFacadeException {
    svci.flushAll();
  }

  @Override
  public void postNotification(final SysEventBase ev) throws CalFacadeException {
    svci.postNotification(ev);
  }

  @Override
  public void endTransaction() throws CalFacadeException {
    svci.endTransaction();
  }

  @Override
  public String getCurrentChangeToken() throws CalFacadeException {
    return getIndexer().currentChangeToken();
  }

  @Override
  public boolean getPublicAdmin() {
    return publicAdmin;
  }

  @Override
  public boolean getWebSubmit() {
    return BedeworkDefs.appTypeWebsubmit.equals(appType);
  }

  @Override
  public String getAppType() {
    return appType;
  }

  @Override
  public void writeJson(final HttpServletResponse resp,
                           final Object val) throws CalFacadeException {
    try {
      mapper.writeValue(resp.getOutputStream(), val);
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  @Override
  public BwSystem getSyspars() throws CalFacadeException {
    return svci.getSysparsHandler().get();
  }

  @Override
  public void updateSyspars(final BwSystem val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public AuthProperties getAuthProperties()
          throws CalFacadeException {
    return svci.getAuthProperties();
  }

  @Override
  public SystemProperties getSystemProperties()
          throws CalFacadeException {
    return svci.getSystemProperties();
  }

  @Override
  public BasicSystemProperties getBasicSystemProperties()
          throws CalFacadeException {
    return svci.getBasicSystemProperties();
  }

  @Override
  public void rollback() {
    try {
      svci.rollbackTransaction();
    } catch (final Throwable ignored) {}

    try {
      svci.endTransaction();
    } catch (final Throwable ignored) {}
  }

  @Override
  public long getUserMaxEntitySize() throws CalFacadeException {
    return svci.getUserMaxEntitySize();
  }

  @Override
  public boolean isDefaultIndexPublic() {
    return getWebSubmit() || getPublicAdmin() || isGuest();
  }

  @Override
  public boolean isPrincipal(final String val)
          throws CalFacadeException {
    return svci.getDirectories().isPrincipal(val);
  }

  /* ------------------------------------------------------------
   *                     Directories
   * ------------------------------------------------------------ */

  @Override
  public DirectoryInfo getDirectoryInfo() throws CalFacadeException {
    return svci.getDirectories().getDirectoryInfo();
  }

  @Override
  public String getCalendarAddress(final String user)
          throws CalFacadeException {
    final BwPrincipal u = svci.getUsersHandler().getUser(user);
    if (u == null) {
      return null;
    }

    return svci.getDirectories().principalToCaladdr(u);
  }

  @Override
  public String uriToCaladdr(final String val)
          throws CalFacadeException {
    return svci.getDirectories().uriToCaladdr(val);
  }

  @Override
  public boolean getUserMaintOK() throws CalFacadeException {
    return svci.getUserAuth().getUserMaintOK();
  }

  @Override
  public BwPrincipal calAddrToPrincipal(final String cua)
          throws CalFacadeException {
    return svci.getDirectories().caladdrToPrincipal(cua);
  }

  @Override
  public void unindex(final String href) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Principals
   * ------------------------------------------------------------ */

  @Override
  public boolean isSuperUser() {
    return superUser;
  }

  @Override
  public boolean isGuest() {
    return true;
  }

  @Override
  public BwPrincipal getCurrentPrincipal() throws CalFacadeException {
    if (currentPrincipal == null) {
      currentPrincipal = (BwPrincipal)svci.getPrincipal().clone();
    }

    return currentPrincipal;
  }

  @Override
  public BwPrincipal getAuthPrincipal() throws CalFacadeException {
    return svci.getPrincipalInfo().getAuthPrincipal();
  }

  @Override
  public BwPrincipal getOwner() throws CalFacadeException {
    return getCurrentPrincipal();
  }

  @Override
  public String getCurrentPrincipalHref() throws CalFacadeException {
    return getCurrentPrincipal().getPrincipalRef();
  }

  @Override
  public String getCurrentCalendarAddress() throws CalFacadeException {
    if (currentCalendarAddress == null) {
      currentCalendarAddress = svci.getDirectories().principalToCaladdr(getCurrentPrincipal());
    }

    return currentCalendarAddress;
  }

  @Override
  public BwPrincipal getUser(final String val)
          throws CalFacadeException {
    return svci.getUsersHandler().getUser(val);
  }

  @Override
  public BwPrincipal getUserAlways(final String val)
          throws CalFacadeException {
    return svci.getUsersHandler().getAlways(val);
  }

  @Override
  public String makePrincipalUri(final String id,
                                 final int whoType)
          throws CalFacadeException {
    return svci.getDirectories().makePrincipalUri(id, whoType);
  }

  @Override
  public BwPrincipal getPrincipal(final String href)
          throws CalFacadeException {
    return svci.getDirectories().getPrincipal(href);
  }

  @Override
  public boolean validPrincipal(final String href)
          throws CalFacadeException {
    return svci.getDirectories().validPrincipal(href);
  }

  /* ------------------------------------------------------------
   *                     Admin users
   * ------------------------------------------------------------ */

  @Override
  public void addUser(final String account)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void addAuthUser(final BwAuthUser val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwAuthUser getAuthUser(final String userid)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateAuthUser(final BwAuthUser val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<BwAuthUser> getAllAuthUsers()
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  @Override
  public String getAdminGroupsIdPrefix()
          throws CalFacadeException {
    return svci.getAdminDirectories().getAdminGroupsIdPrefix();
  }

  @Override
  public BwGroup getAdminGroup(final String href)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<BwGroup> getAdminGroups()
          throws CalFacadeException {
    refreshAdminGroupInfo();

    return adminGroupsInfo;
  }

  @Override
  public Collection<BwGroup> getCalsuiteAdminGroups()
          throws CalFacadeException {
    refreshAdminGroupInfo();

    return calsuiteAdminGroupsInfo;
  }

  @Override
  public void refreshAdminGroups() {
    lastAdminGroupsInfoRefresh = 0;
  }

  @Override
  public Collection<BwGroup> getAllAdminGroups(final BwPrincipal val)
          throws CalFacadeException {
    return svci.getAdminDirectories().getAllGroups(val);
  }

  @Override
  public void addAdminGroup(final BwAdminGroup group)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void removeAdminGroup(final BwAdminGroup group)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateAdminGroup(final BwAdminGroup group)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean getAdminGroupMaintOK() throws CalFacadeException {
    return svci.getAdminDirectories().getGroupMaintOK();
  }

  @Override
  public BwAdminGroup findAdminGroup(final String name)
          throws CalFacadeException {
    return (BwAdminGroup)svci.getAdminDirectories().findGroup(name);
  }

  @Override
  public void getAdminGroupMembers(final BwAdminGroup group)
          throws CalFacadeException {
    svci.getAdminDirectories().getMembers(group);
  }

  @Override
  public void addAdminGroupMember(final BwAdminGroup group,
                                  final BwPrincipal val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void removeAdminGroupMember(final BwAdminGroup group,
                                     final BwPrincipal val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Groups
   * ------------------------------------------------------------ */

  @Override
  public BwGroup findGroup(final String name)
          throws CalFacadeException {
    return svci.getDirectories().findGroup(name);
  }

  @Override
  public Collection<BwGroup> findGroupParents(final BwGroup group)
          throws CalFacadeException {
    return svci.getDirectories().findGroupParents(group);
  }

  @Override
  public Collection<BwGroup> getGroups(final BwPrincipal val)
          throws CalFacadeException {
    return svci.getDirectories().getGroups(val);
  }

  @Override
  public Collection<BwGroup> getAllGroups(final boolean populate)
          throws CalFacadeException {
    return svci.getDirectories().getAll(populate);
  }

  @Override
  public void getMembers(final BwGroup group)
          throws CalFacadeException {
    svci.getDirectories().getMembers(group);
  }

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  @Override
  public BwPreferences getPreferences() throws CalFacadeException {
    if (publicAdmin | publicView) {
      final BwPreferences prefs = getCalsuitePreferences();
      if (prefs != null) {
        return prefs;
      }
    }

    return svci.getPrefsHandler().get();
  }

  public BwPreferences getCalsuitePreferences() throws CalFacadeException {
    return getCalsuitePreferences(getCalSuite());
  }

  @Override
  public BwPreferences getPreferences(final String user)
          throws CalFacadeException {
    return null;
  }

  @Override
  public void updatePreferences(final BwPreferences val)
          throws CalFacadeException {
    svci.getPrefsHandler().update(val);
  }

  @Override
  public String getPreferredCollectionPath(final String compName)
          throws CalFacadeException {
    return svci.getCalendarsHandler().getPreferred(compName);
  }

  /** Set false to inhibit lastLocale stuff */
  public static boolean tryLastLocale = true;

  @Override
  public Locale getUserLocale(final Collection<Locale> locales,
                              final Locale locale) throws CalFacadeException {
    final Collection<Locale> sysLocales = getSupportedLocales();

    if (locale != null) {
      /* See if it's acceptable */
      final Locale l = BwLocale.matchLocales(sysLocales, locale);
      if (l != null) {
        if (debug) {
          debugMsg("Setting locale to " + l);
        }
        return l;
      }
    }

    /* See if the user expressed a preference */
    final Collection<BwProperty> properties = getPreferences().getProperties();
    String preferredLocaleStr = null;
    String lastLocaleStr = null;

    if (properties != null) {
      for (final BwProperty prop: properties) {
        if (preferredLocaleStr == null) {
          if (prop.getName().equals(BwPreferences.propertyPreferredLocale)) {
            preferredLocaleStr = prop.getValue();
            if (!tryLastLocale) {
              break;
            }
          }
        }

        if (tryLastLocale) {
          if (lastLocaleStr == null) {
            if (prop.getName().equals(BwPreferences.propertyLastLocale)) {
              lastLocaleStr = prop.getValue();
            }
          }
        }

        if ((preferredLocaleStr != null) &&
                (lastLocaleStr != null)) {
          break;
        }
      }
    }

    if (preferredLocaleStr != null) {
      final Locale l = BwLocale.matchLocales(sysLocales,
                                             makeLocale(preferredLocaleStr));
      if (l != null) {
        if (debug) {
          debugMsg("Setting locale to " + l);
        }
        return l;
      }
    }

    if (lastLocaleStr != null) {
      final Locale l = BwLocale.matchLocales(sysLocales,
                                             makeLocale(lastLocaleStr));
      if (l != null) {
        if (debug) {
          debugMsg("Setting locale to " + l);
        }
        return l;
      }
    }

    /* See if the supplied list has a match in the supported locales */

    if (locales != null) {
      // We had an ACCEPT-LANGUAGE header

      for (final Locale loc: locales) {
        final Locale l = BwLocale.matchLocales(sysLocales, loc);
        if (l != null) {
          if (debug) {
            debugMsg("Setting locale to " + l);
          }
          return l;
        }
      }
    }

    /* Use the first from supported locales -
     * there's always at least one in the collection */
    final Locale l = sysLocales.iterator().next();

    if (debug) {
      debugMsg("Setting locale to " + l);
    }
    return l;
  }

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  @Override
  public BwCalendar getHome() throws CalFacadeException {
    return svci.getCalendarsHandler().getHome();
  }

  @Override
  public BwCalendar getCollection(final String path)
          throws CalFacadeException {
    checkUpdate();
    return svci.getCalendarsHandler().get(path);
  }

  @Override
  public boolean collectionExists(final String path)
          throws CalFacadeException {
    checkUpdate();
    return getCollection(path) != null;
  }

  @Override
  public BwCalendar getSpecial(final int calType,
                               final boolean create)
          throws CalFacadeException {
    checkUpdate();

    return svci.getCalendarsHandler().getSpecial(calType, create);
  }

  @Override
  public BwCalendar addCollection(final BwCalendar val,
                                  final String parentPath)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateCollection(final BwCalendar col)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean deleteCollection(final BwCalendar val,
                                  final boolean emptyIt)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwCalendar resolveAlias(final BwCalendar val,
                                 final boolean resolveSubAlias,
                                 final boolean freeBusy)
          throws CalFacadeException {
    checkUpdate();
    return svci.getCalendarsHandler().resolveAlias(val,
                                                   resolveSubAlias,
                                                   freeBusy);
  }

  @Override
  public Collection<BwCalendar> getChildren(final BwCalendar col)
          throws CalFacadeException {
    checkUpdate();

    if (!col.getPublick()) {
      return svci.getCalendarsHandler().getChildren(col);
    }

    // This and its children need to be cached
    BwCalendar ourCopy;

    synchronized (publicCloned) {
      if (!col.unsaved()) {
        ourCopy = publicCloned.get(col.getPath());

        if (ourCopy == null) {
          ourCopy = col.shallowClone();
          publicCloned.put(col.getPath(), ourCopy);
        }
      } else {
        ourCopy = col;
      }

      Collection<BwCalendar> children = ourCopy.getChildren();
      if (children != null) {
        // Assume ok
        return children;
      }

      children = col.getChildren();
      if (children == null) {
        // Have to retrieve
        children = svci.getCalendarsHandler().getChildren(col);
      }

      // Assume we have to clone

      final Collection<BwCalendar> ourChildren = new ArrayList<>();

      for (final BwCalendar ch: children) {
        BwCalendar ourCh = ch;
        if (!ch.unsaved()) {
          ourCh = publicCloned.get(ch.getPath());

          if (ourCh == null) {
            ourCh = ch.shallowClone();
            publicCloned.put(ch.getPath(), ourCh);
          }
        }

        ourChildren.add(ourCh);
      }

      ourCopy.setChildren(ourChildren);

      return ourChildren;
    }
  }

  @Override
  public void moveCollection(final BwCalendar val,
                             final BwCalendar newParent)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public SynchStatusResponse getSynchStatus(final String path) throws CalFacadeException {
    return svci.getCalendarsHandler().getSynchStatus(path);
  }

  @Override
  public Collection<BwCalendar> decomposeVirtualPath(final String vpath)
          throws CalFacadeException {
    return svci.getCalendarsHandler().decomposeVirtualPath(vpath);
  }

  @Override
  public Collection<BwCalendar> getAddContentCollections(final boolean includeAliases)
          throws CalFacadeException {
    checkUpdate();
    return getCalendarCollator().getCollatedCollection(
            svci.getCalendarsHandler().getAddContentCollections(includeAliases));
  }

  @Override
  public BwCalendar getPublicCalendars() throws CalFacadeException {
    checkUpdate();

    final String path = svci.getCalendarsHandler().getPublicCalendarsRootPath();

    BwCalendar res = publicCloned.get(path);

    if (res != null) {
      return res;
    }

    res = svci.getCalendarsHandler().getPublicCalendars().shallowClone();

    synchronized (publicCloned) {
      if (publicCloned.get(path) != null) {
        // Somebody moved in.
        return publicCloned.get(path);
      }

      publicCloned.put(path, res);
    }

    return res;
  }

  @Override
  public BwCalendar getHome(final BwPrincipal principal,
                            final boolean freeBusy)
          throws CalFacadeException {
    return svci.getCalendarsHandler().getHome(principal, freeBusy);
  }

  public void flushCached() {
    synchronized (publicCloned) {
      publicCloned.clear();
    }
  }

  private final static Map<String, BwCalendar> publicCloned =
      new HashMap<>();

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  @Override
  public BwCategory getCategoryByName(final BwString name)
          throws CalFacadeException {
    checkUpdate();
    return svci.getCategoriesHandler().find(name);
  }

  @Override
  public BwCategory getCategory(final String uid)
          throws CalFacadeException {
    checkUpdate();
    return svci.getCategoriesHandler().get(uid);
  }

  @Override
  public BwCategory getPersistentCategory(final String uid)
          throws CalFacadeException {
    return svci.getCategoriesHandler().getPersistent(uid);
  }

  @Override
  public Collection<BwCategory> getCategories()
          throws CalFacadeException {
    checkUpdate();
    return svci.getCategoriesHandler().get();
  }

  @Override
  public Collection<BwCategory> getCategories(final Collection<String> uids)
          throws CalFacadeException {
    return svci.getCategoriesHandler().get(uids);
  }

  @Override
  public Collection<BwCategory> getPublicCategories()
          throws CalFacadeException {
    checkUpdate();
    return svci.getCategoriesHandler().getPublic();
  }

  @Override
  public Collection<BwCategory> getEditableCategories()
          throws CalFacadeException {
    checkUpdate();
    return svci.getCategoriesHandler().getEditable();
  }

  @Override
  public boolean addCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public DeleteReffedEntityResult deleteCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Set<String> getDefaultPublicCategoryUids()
          throws CalFacadeException {
    final Set<String> catUids = new TreeSet<>();

    for (final BwCalSuite suite: svci.getCalSuitesHandler().getAll()) {
      final BwPreferences prefs = getCalsuitePreferences(suite);

      if ((prefs != null) && (prefs.getDefaultCategoryUids() != null)) {
        for (final String uid: prefs.getDefaultCategoryUids()) {
          catUids.add(uid);
        }
      }
    }

    return catUids;
  }

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  @Override
  public BwContact getContact(final String uid)
          throws CalFacadeException {
    checkUpdate();
    return svci.getContactsHandler().get(uid);
  }

  @Override
  public BwContact getPersistentContact(final String uid)
          throws CalFacadeException {
    return svci.getContactsHandler().getPersistent(uid);
  }

  @Override
  public Collection<BwContact> getContacts()
          throws CalFacadeException {
    checkUpdate();
    return svci.getContactsHandler().get();
  }

  @Override
  public Collection<BwContact> getPublicContacts()
          throws CalFacadeException {
    checkUpdate();
    return svci.getContactsHandler().getPublic();
  }

  @Override
  public Collection<BwContact> getEditableContacts()
          throws CalFacadeException {
    checkUpdate();
    return svci.getContactsHandler().getEditable();
  }

  @Override
  public BwContact findContact(final BwString val)
          throws CalFacadeException {
    return svci.getContactsHandler().findPersistent(val);
  }

  @Override
  public void addContact(final BwContact val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateContact(final BwContact val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public DeleteReffedEntityResult deleteContact(final BwContact val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public CheckEntityResult<BwContact> ensureContactExists(final BwContact val,
                                                          final String ownerHref)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  @Override
  public BwLocation getLocation(final String uid)
          throws CalFacadeException {
    checkUpdate();
    return svci.getLocationsHandler().get(uid);
  }

  @Override
  public BwLocation getPersistentLocation(final String uid)
          throws CalFacadeException {
    return svci.getLocationsHandler().getPersistent(uid);
  }

  @Override
  public Collection<BwLocation> getLocations()
          throws CalFacadeException {
    checkUpdate();
    return svci.getLocationsHandler().get();
  }

  @Override
  public Collection<BwLocation> getPublicLocations()
          throws CalFacadeException {
    checkUpdate();
    return svci.getLocationsHandler().getPublic();
  }

  @Override
  public Collection<BwLocation> getEditableLocations()
          throws CalFacadeException {
    checkUpdate();
    return svci.getLocationsHandler().getEditable();
  }

  @Override
  public BwLocation findLocation(final BwString address)
          throws CalFacadeException {
    return svci.getLocationsHandler().findPersistent(address);
  }

  @Override
  public boolean addLocation(final BwLocation val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateLocation(final BwLocation val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public DeleteReffedEntityResult deleteLocation(final BwLocation val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public CheckEntityResult<BwLocation> ensureLocationExists(final BwLocation val,
                                                            final String ownerHref)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  @Override
  public void claimEvent(final BwEvent ev) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<EventInfo> getEventByUid(final String path,
                                             final String guid,
                                             final String rid,
                                             final RecurringRetrievalMode recurRetrieval)
          throws CalFacadeException {
    return svci.getEventsHandler().getByUid(path, guid, rid,
                                            recurRetrieval);
  }

  @Override
  public SearchParams getSearchParams() {
    return cstate.getSearchParams();
  }

  @Override
  public EventInfo getEvent(final String colPath,
                            final String name,
                            final String recurrenceId)
          throws CalFacadeException {
    return svci.getEventsHandler().get(colPath, name, recurrenceId);
  }

  @Override
  public EventInfo getEvent(final String href)
          throws CalFacadeException {
    final EventKey key = new EventKey(href, false);
    return svci.getEventsHandler().get(key.getColPath(),
                                       key.getName(),
                                       key.getRecurrenceId());
  }

  @Override
  public Collection<EventInfo> getEvents(final String filter,
                                         final BwDateTime startDate,
                                         final BwDateTime endDate,
                                         final boolean expand)
          throws CalFacadeException {
    if (filter == null) {
      return null;
    }

    checkUpdate();

    final BwFilterDef fd = new BwFilterDef();
    fd.setDefinition(filter);

    parseFilter(fd);

    final RecurringRetrievalMode rrm;
    if (expand) {
      rrm = RecurringRetrievalMode.expanded;
    } else {
      rrm = RecurringRetrievalMode.overrides;
    }

    return svci.getEventsHandler().getEvents(null,
                                             fd.getFilters(),
                                             startDate,
                                             endDate,
                                             null,
                                             rrm);
  }

  @Override
  public EventInfo.UpdateResult addEvent(final EventInfo ei,
                                         final boolean noInvites,
                                         final boolean scheduling,
                                         final boolean rollbackOnError)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                            final boolean noInvites,
                                            final String fromAttUri)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean deleteEvent(final EventInfo ei,
                             final boolean sendSchedulingMessage)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void markDeleted(final BwEvent event)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Notifications
   * ------------------------------------------------------------ */

  @Override
  public NotificationType findNotification(final String name)
          throws CalFacadeException {
    return svci.getNotificationsHandler().find(name);
  }

  @Override
  public List<NotificationType> allNotifications()
          throws CalFacadeException {
    return svci.getNotificationsHandler().getAll();
  }

  @Override
  public void removeNotification(final String name)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void removeNotification(final NotificationType val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void removeAllNotifications() throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void subscribe(final String principalHref,
                        final List<String> emails)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void unsubscribe(final String principalHref,
                          final List<String> emails)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Resources
   * ------------------------------------------------------------ */

  @Override
  public void saveResource(final String path,
                           final BwResource val) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwResource getResource(final String path) throws CalFacadeException {
    checkUpdate();
    return svci.getResourcesHandler().get(path);
  }

  @Override
  public void getResourceContent(final BwResource val)
          throws CalFacadeException {
    checkUpdate();
    svci.getResourcesHandler().getContent(val);
  }

  @Override
  public List<BwResource> getAllResources(final String path)
          throws CalFacadeException {
    checkUpdate();
    return svci.getResourcesHandler().getAll(path);
  }

  @Override
  public void updateResource(final BwResource val,
                             final boolean updateContent)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwEvent getFreeBusy(final Collection<BwCalendar> fbset,
                             final BwPrincipal who,
                             final BwDateTime start,
                             final BwDateTime end,
                             final BwOrganizer org,
                             final String uid,
                             final String exceptUid)
          throws CalFacadeException {
    return svci.getScheduler().getFreeBusy(fbset, who, start, end,
                                           org, uid, exceptUid);
  }

  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  @Override
  public SchedulingI.FbResponses aggregateFreeBusy(final ScheduleResult sr,
                                                   final BwDateTime start,
                                                   final BwDateTime end,
                                                   final BwDuration granularity)
          throws CalFacadeException {
    return svci.getScheduler().aggregateFreeBusy(sr, start, end, granularity);
  }

  @Override
  public ScheduleResult schedule(final EventInfo ei,
                                 final int method,
                                 final String recipient,
                                 final String fromAttUri,
                                 final boolean iSchedule)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public EventInfo getStoredMeeting(final BwEvent ev)
          throws CalFacadeException {
    checkUpdate();
    return svci.getScheduler().getStoredMeeting(ev);
  }

  @Override
  public ScheduleResult requestRefresh(final EventInfo ei,
                                       final String comment)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Sharing
   * ------------------------------------------------------------ */

  @Override
  public ShareResultType share(final String principalHref,
                               final BwCalendar col,
                               final ShareType share)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void publish(final BwCalendar col)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void unpublish(final BwCalendar col)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public SharingI.ReplyResult sharingReply(final InviteReplyType reply)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public SharingI.SubscribeResult subscribe(final String colPath,
                                            final String subscribedName)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public SharingI.SubscribeResult subscribeExternal(final String extUrl,
                                                    final String subscribedName,
                                                    final int refresh,
                                                    final String remoteId,
                                                    final String remotePw)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Synch
   * ------------------------------------------------------------ */

  @Override
  public BwSynchInfo getSynchInfo() throws CalFacadeException {
    return svci.getSynch().getSynchInfo();
  }

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  @Override
  public void setViewMode(final String val)
          throws CalFacadeException {
    viewMode = val;
  }

  @Override
  public String getViewMode() throws CalFacadeException {
    if (viewMode != null) {
      return viewMode;
    }

    viewMode = getPreferences().getDefaultViewMode();
    if (viewMode == null) {
      if (getPublicAdmin()) {
        viewMode = listViewMode;
      } else {
        viewMode = gridViewMode;
      }
    }

    return viewMode;
  }

  @Override
  public BwView getView(final String val) throws CalFacadeException {
    checkUpdate();
    return svci.getViewsHandler().find(val);
  }

  @Override
  public boolean addView(final BwView val,
                         final boolean makeDefault)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<BwView> getAllViews() throws CalFacadeException {
    return svci.getViewsHandler().getAll();
  }

  @Override
  public boolean addViewCollection(final String name,
                                   final String path)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean removeViewCollection(final String name,
                                      final String path)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean removeView(final BwView val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  @Override
  public void flushState() throws CalFacadeException {
  }

  /* ------------------------------------------------------------
   *                     State of current admin group
   * ------------------------------------------------------------ */

  @Override
  public void setGroupSet(final boolean val) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean getGroupSet() {
    return false;
  }

  @Override
  public void setChoosingGroup(final boolean val) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean getChoosingGroup() {
    return false;
  }

  @Override
  public void setOneGroup(final boolean val) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public boolean getOneGroup() {
    return false;
  }

  @Override
  public void setAdminGroupName(final String val) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public String getAdminGroupName() {
    return null;
  }

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  @Override
  public void moveContents(final BwCalendar cal,
                           final BwCalendar newCal)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public UpdateFromTimeZonesInfo updateFromTimeZones(final int limit,
                                                     final boolean checkOnly,
                                                     final UpdateFromTimeZonesInfo info)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void postMessage(final Message val)
          throws CalFacadeException {
    svci.getMailer().post(val);
  }

  @Override
  public void clearSearch() {
    cstate.setSearchParams(null);
    lastSearch = null;
  }

  @Override
  public void clearSearchEntries() {
    lastSearchEntries = null;
  }

  @Override
  public SearchResult search(final SearchParams params) throws CalFacadeException {
    checkUpdate();
    cstate.setSearchParams(params);

    lastSearchEntries = null;

    String start = null;
    String end = null;

    if (params.getFromDate() != null) {
      start = params.getFromDate().getDate();
    }

    if (params.getToDate() != null) {
      end = params.getToDate().getDate();
    }

    boolean publicIndex = isDefaultIndexPublic();

    if (params.getPublicIndexRequested()) {
      publicIndex = true;
    }

    lastSearch = getIndexer(publicIndex).search(
            params.getQuery(),
            params.getRelevance(),
            params.getFilter(),
            params.getSort(),
            getDefaultFilterContext(),
            start,
            end,
            params.getPageSize(),
            accessChecker,
            params.getRecurMode());

    return lastSearch;
  }

  @Override
  public List<SearchResultEntry> getSearchResult(final Position pos) throws CalFacadeException {
    checkUpdate();
    if (lastSearch == null) {
      return null;
    }

    if ((pos == Position.current) && (lastSearchEntries != null)) {
      return lastSearchEntries;
    }

    lastSearchEntries = formatSearchResult(lastSearch.getIndexer().
            getSearchResult(lastSearch, pos, PrivilegeDefs.privAny));

    if ((lastSearch != null) && (cstate.getSearchParams() != null)) {
      cstate.getSearchParams().setCurOffset(lastSearch.getLastPageStart());
    }

    return lastSearchEntries;
  }

  @Override
  public List<SearchResultEntry> getSearchResult(final int start,
                                                 final int num) throws CalFacadeException {
    if (lastSearch == null) {
      return new ArrayList<>(0);
    }
    checkUpdate();

    return formatSearchResult(lastSearch.getIndexer().
            getSearchResult(lastSearch, start, num, PrivilegeDefs.privAny));
  }

  @Override
  public void changeAccess(final BwShareableDbentity ent,
                           final Collection<Ace> aces,
                           final boolean replaceAll)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */


  @Override
  public void setCalSuite(final BwCalSuite cs)
          throws CalFacadeException {
    ownerHrefs = new ArrayList<>();

    final BwAdminGroup ag = cs.getGroup();
    addOwnerHrefs(ag);

    svci.setCalSuite(cs.getName());
  }

  private void addOwnerHrefs(final BwAdminGroup ag) throws CalFacadeException {
    ownerHrefs.add(ag.getOwnerHref());

    getAdminGroupMembers(ag);
    if (ag.getGroupMembers() == null) {
      return;
    }

    for (final BwPrincipal pr: ag.getGroupMembers()) {
      if (pr instanceof BwAdminGroup) {
        addOwnerHrefs((BwAdminGroup)pr);
      }
    }
  }

  @Override
  public BwCalSuiteWrapper getCalSuite() throws CalFacadeException {
    return svci.getCalSuitesHandler().get();
  }

  @Override
  public BwCalSuiteWrapper getCalSuite(final BwAdminGroup group)
          throws CalFacadeException {
    return svci.getCalSuitesHandler().get(group);
  }

  private static final Map<String, SubContext> suiteToContextMap = new HashMap<>();
  private static Collection<BwCalSuite> suites;

  @Override
  public Collection<BwCalSuite> getContextCalSuites()
          throws CalFacadeException {
    refreshAdminGroupInfo();

    return suites;
  }

  @Override
  public BwCalSuiteWrapper getCalSuite(final String name)
          throws CalFacadeException {
    return svci.getCalSuitesHandler().get(name);
  }

  @Override
  public BwCalSuiteWrapper addCalSuite(final String name,
                                       final String adminGroupName,
                                       final String rootCollectionPath,
                                       final String submissionsPath)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateCalSuite(final BwCalSuiteWrapper cs,
                             final String adminGroupName,
                             final String rootCollectionPath,
                             final String submissionsPath)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void deleteCalSuite(final BwCalSuiteWrapper val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                   Calendar Suite Resources
   * ------------------------------------------------------------ */

  @Override
  public List<BwResource> getCSResources(final BwCalSuite suite,
                                         final String rc)
          throws CalFacadeException {
    return svci.getResourcesHandler().getAll(getCSResourcesPath(suite,
                                                                rc));
  }

  @Override
  public BwResource getCSResource(final BwCalSuite suite,
                                  final String name,
                                  final String rc)
          throws CalFacadeException {
    try {
      final BwResource r = svci.getResourcesHandler().
              get(Util.buildPath(false,
                                 getCSResourcesPath(suite, rc),
                                 "/",
                                 name));
      if (r != null) {
        svci.getResourcesHandler().getContent(r);
      }

      return r;
    } catch (final CalFacadeException cfe) {
      if (CalFacadeException.collectionNotFound.equals(cfe.getMessage())) {
        // Collection does not exist (yet)

        return null;
      }

      throw cfe;
    }
  }

  @Override
  public void addCSResource(final BwCalSuite suite,
                            final BwResource res,
                            final String rc)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void deleteCSResource(final BwCalSuite suite,
                               final String name,
                               final String rc)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                   Filters
   * ------------------------------------------------------------ */

  @Override
  public BwFilterDef getFilter(final String name)
          throws CalFacadeException {
    return svci.getFiltersHandler().get(name);
  }

  @Override
  public void parseFilter(final BwFilterDef val)
          throws CalFacadeException {
    svci.getFiltersHandler().parse(val);
  }

  @Override
  public List<SortTerm> parseSort(final String val)
          throws CalFacadeException {
    return svci.getFiltersHandler().parseSort(val);
  }

  @Override
  public Collection<BwFilterDef> getAllFilters()
          throws CalFacadeException {
    return svci.getFiltersHandler().getAll();
  }

  @Override
  public void validateFilter(final String val)
          throws CalFacadeException {
    svci.getFiltersHandler().validate(val);
  }

  @Override
  public void saveFilter(final BwFilterDef val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void deleteFilter(final String name)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                   protected methods
   * ------------------------------------------------------------ */

  protected BwPreferences getCalsuitePreferences(final BwCalSuite cs) throws CalFacadeException {
    if (cs == null) {
      return null;
    }

    final String csHref = cs.getGroup().getOwnerHref();

    final BwPrincipal p = getUser(csHref);
    if (p == null) {
      return null;
    }

    return svci.getPrefsHandler().get(p);
  }

  protected void refreshAdminGroupInfo()
          throws CalFacadeException {
    if ((adminGroupsInfo != null) &&
            (System.currentTimeMillis() < (lastAdminGroupsInfoRefresh +
                                                   adminGroupsInfoRefreshInterval))) {
      return;
    }

    synchronized (adminGroupLocker) {
      suiteToContextMap.clear();

      for (final SubContext subContext : getSyspars().getContexts()) {
        suiteToContextMap.put(subContext.getCalSuite(), subContext);
      }

      final Set<String> groupHrefs = new TreeSet<>();

      suites = new ArrayList<>();

      for (final BwCalSuite suite: svci.getCalSuitesHandler().getAll()) {
        final BwCalSuite cs = (BwCalSuite)suite.clone();

        groupHrefs.add(cs.getGroup().getPrincipalRef());

        final SubContext subContext = suiteToContextMap.get(cs.getName());

        if (subContext != null) {
          cs.setContext(subContext.getContextName());
          cs.setDefaultContext(subContext.getDefaultContext());
        } else {
          cs.setContext(null);
          cs.setDefaultContext(false);
        }

        suites.add(cs);
      }

      adminGroupsInfo = new ArrayList<>();
      calsuiteAdminGroupsInfo = new ArrayList<>();

      final Collection<BwGroup> ags =
              svci.getAdminDirectories().getAll(true);

      for (final BwGroup g: ags) {
        final BwGroup cg = (BwGroup)g.clone();

        if (groupHrefs.contains(cg.getPrincipalRef())) {
          calsuiteAdminGroupsInfo.add(cg);
        }

        final Collection<BwGroup> mgs = getAllAdminGroups(g);

        for (final BwGroup mg: mgs) {
          final BwGroup cmg = (BwGroup)mg.clone();

          cg.addGroup(cmg);
        }

        adminGroupsInfo.add(cg);
      }

      lastAdminGroupsInfoRefresh = System.currentTimeMillis();
    }
  }

  protected String getCSResourcesPath(final BwCalSuite suite,
                                      final String rc) throws CalFacadeException {
    final ResourceClass csRc = ResourceClass.valueOf(rc);

    return svci.getCalSuitesHandler().getResourcesPath(suite, csRc);

    /**
    if (rc.equals(CalSuiteResource.resourceClassGlobal)) {
      return Util.buildPath(false, getBasicSyspars().getGlobalResourcesPath());
    }

    final BwPrincipal eventsOwner = getPrincipal(suite.getGroup().getOwnerHref());

    final String home = svci.getPrincipalInfo().getCalendarHomePath(eventsOwner);

    final BwPreferences prefs;

    if (superUser) {
      prefs = getPreferences(eventsOwner.getPrincipalRef());
    } else {
      prefs = getPreferences();
    }

    String col = null;

    if (rc.equals(CalSuiteResource.resourceClassAdmin)) {
      col = prefs.getAdminResourcesDirectory();

      if (col == null) {
        col = ".adminResources";
      }
    } else if (rc.equals(CalSuiteResource.resourceClassCalSuite)) {
      col = prefs.getSuiteResourcesDirectory();

      if (col == null) {
        col = ".csResources";
      }
    }

    if (col != null) {
      return Util.buildPath(false, home, "/", col);
    }

    throw new RuntimeException("System error");
     */
  }

  protected BasicSystemProperties getBasicSyspars() throws CalFacadeException {
    return svci.getBasicSystemProperties();
  }

  protected CollectionCollator<BwCalendar> getCalendarCollator() {
    if (calendarCollator == null) {
      calendarCollator = new CollectionCollator<>();
    }

    return calendarCollator;
  }

  protected BwIndexer getIndexer() throws CalFacadeException {
    return getIndexer(isDefaultIndexPublic());
  }

  protected BwIndexer getIndexer(final boolean publick) throws CalFacadeException {
    if (publick) {
      if (publicIndexer == null) {
        publicIndexer = svci.getIndexer(true);
      }

      return publicIndexer;
    }

    if (userIndexer == null) {
      userIndexer = svci.getIndexer(false);
    }

    return userIndexer;
  }

  private final static long indexerDelay = 1010; // Just over a sec for the indexer

  protected <T> T update(final T val) {
    updated();
    return val;
  }

  protected void updated() {
    lastUpdate = System.currentTimeMillis();
  }

  protected void checkUpdate() throws CalFacadeException {
    /*
    if (debug) {
      debugMsg("checkUpdate: \n" +
                       " req=" + requestEnd + "\n" +
                       "last=" + lastUpdate + "\n" +
                       "wait=" + (indexerDelay -
                                          (System.currentTimeMillis() - lastUpdate)));
    }*/

    if (lastUpdate == 0) {
      return;
    }

    if (requestEnd < lastUpdate) {
      // We don't wait if this conversation caused the update
      return;
    }

    final long toWait = indexerDelay -
            (System.currentTimeMillis() - lastUpdate);

    if (toWait > 0) {
      try {
        Thread.sleep(toWait);
      } catch (final InterruptedException ie) {
        throw new CalFacadeException(ie);
      }

      lastUpdate = 0; // Only wait once
    }
  }

  protected Locale makeLocale(final String val) throws CalFacadeException {
    try {
      return Util.makeLocale(val);
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  protected FilterBase getDefaultFilterContext() throws CalFacadeException {
    if (defaultFilterContextSet) {
      return defaultFilterContext;
    }

    final BwView preferred = getView(null);

    if (preferred == null) {
      defaultFilterContextSet = true;
      return defaultFilterContext;
    }

    final String fexpr = "view=\"" + preferred.getName() +"\"";

    final BwFilterDef fd = new BwFilterDef();
    fd.setDefinition(fexpr);

    parseFilter(fd);

    defaultFilterContextSet = true;
    defaultFilterContext = fd.getFilters();

    return defaultFilterContext;
  }

  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void debugMsg(final String msg) {
    getLogger().debug(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  protected void logIt(final String msg) {
    getLogger().info(msg);
  }

  /* ------------------------------------------------------------
   *                   private methods
   * ------------------------------------------------------------ */

  private List<SearchResultEntry> formatSearchResult(
          final List<SearchResultEntry> entries) throws CalFacadeException {
    final IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(this));

    for (final SearchResultEntry sre: entries) {
      final Object o = sre.getEntity();

      if (o instanceof CategorisedEntity) {
        restoreCategories((CategorisedEntity)o);
      }

      if (!(o instanceof EventInfo)) {
        continue;
      }

      final BwEvent ev = ((EventInfo)o).getEvent();
      restoreCategories(ev);

      if (ev.getLocationUid() != null){
        ev.setLocation(getLocation(ev.getLocationUid()));
      }

      final EventFormatter ef = new EventFormatter(this,
                                                   trans,
                                                   (EventInfo)o);
      sre.setEntity(ef);
    }

    return entries;
  }

  private void restoreCategories(final CategorisedEntity ce) throws CalFacadeException {
    final Set<String> uids = ce.getCategoryUids();
    if (Util.isEmpty(uids)) {
      return;
    }

//    Set<String> catUids = new TreeSet<>();

    for (final Object o: uids) {
      final String uid = (String)o;
//      catUids.add(uid);

      ce.addCategory(getCategory(uid));
    }
  }

  private Collection<Locale> getSupportedLocales() throws CalFacadeException {
    if (supportedLocales != null) {
      return supportedLocales;
    }

    supportedLocales = new ArrayList<>();

    final String ll = getSystemProperties().getLocaleList();

    if (ll == null) {
      supportedLocales.add(BwLocale.getLocale());
      return supportedLocales;
    }

    try {
      int pos = 0;

      while (pos < ll.length()) {
        final int nextPos = ll.indexOf(",", pos);
        if (nextPos < 0) {
          supportedLocales.add(makeLocale(ll.substring(pos)));
          break;
        }

        supportedLocales.add(makeLocale(ll.substring(pos, nextPos)));
        pos = nextPos + 1;
      }
    } catch (final CalFacadeException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalFacadeException(CalFacadeException.badSystemLocaleList,
                                   ll);
    }

    if (supportedLocales.isEmpty()) {
      supportedLocales.add(BwLocale.getLocale());
    }

    return supportedLocales;
  }
}
