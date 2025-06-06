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

import org.bedework.access.PrivilegeDefs;
import org.bedework.appcommon.CollectionCollator;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.EventKey;
import org.bedework.base.exc.BedeworkException;
import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.base.response.Response;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwProperty;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.filter.BwCollectionFilter;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.calfacade.indexing.BwIndexer.DeletedState;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResult;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.locale.BwLocale;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.CalSvcIPars;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calsvci.CalSvcFactoryDefault;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.CollectionsI.SynchStatusResponse;
import org.bedework.convert.IcalTranslator;
import org.bedework.sysevents.events.HttpEvent;
import org.bedework.sysevents.events.HttpOutEvent;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.util.caching.FlushMap;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bedework.calfacade.indexing.BwIndexer.DeletedState.includeDeleted;
import static org.bedework.calfacade.indexing.BwIndexer.DeletedState.noDeleted;
import static org.bedework.calfacade.indexing.BwIndexer.docTypeCollection;
import static org.bedework.calfacade.indexing.BwIndexer.docTypeEvent;
import static org.bedework.calsvci.CalSuitesI.ResourceClass;
import static org.bedework.util.servlet.ActionTypes.actionTypeAction;
import static org.bedework.util.servlet.ConversationTypes.conversationTypeEnd;
import static org.bedework.util.servlet.ConversationTypes.conversationTypeOnly;
import static org.bedework.util.servlet.ConversationTypes.conversationTypeProcessAndOnly;
import static org.bedework.util.servlet.ConversationTypes.conversationTypeUnknown;

/**
 * User: douglm Date: 6/27/13 Time: 2:03
 */
public class ROClientImpl implements Logged, Client {
  protected ConfigCommon conf;

  protected String id;

  protected CalSvcIPars pars;

  protected CalSvcI svci;

  protected boolean publicView;

  protected boolean superUser;

  protected boolean publicAdmin;

  protected BwPrincipal<?> currentPrincipal;
  private String currentCalendarAddress;

  private String primaryPublicPath;

  private static final Object primaryPublicPathLocker = new Object();

  private Collection<Locale>supportedLocales;

  private SearchParams searchParams;

  private transient CollectionCollator<BwCollection> calendarCollator;
  protected ClientType clientType;

  private final Map<String, BwIndexer> publicIndexers = new HashMap<>();
  private final Map<String, BwIndexer> userIndexers = new HashMap<>();
  private SearchResult lastSearch;
  private List<SearchResultEntry> lastSearchEntries;

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
  protected static Collection<BwGroup<?>> adminGroupsInfo;

  protected static Collection<BwGroup<?>> calsuiteAdminGroupsInfo;

  protected BwCalSuiteWrapper calSuite;
  protected BwPrincipal<?> calSuiteOwner;
  protected String calSuiteName;

  protected static long lastAdminGroupsInfoRefresh;
  static long adminGroupsInfoRefreshInterval = 1000 * 60 * 5;

  private static final Object adminGroupLocker = new Object();

  /**
   *
   * @param conf client configuration
   * @param id identify the client - usually module name
   * @param authUser account
   * @param runAsUser account
   * @param calSuiteName the calendar suite
   * @param clientType type of application: submit, admin etc
   * @param publicView true for the public RO client
   */
  public ROClientImpl(final ConfigCommon conf,
                      final String id,
                      final String authUser,
                      final String runAsUser,
                      final String calSuiteName,
                      final ClientType clientType,
                      final boolean publicView) {
    this(conf, id);

    reinit(authUser, runAsUser, calSuiteName, clientType, publicView);
  }

  protected ROClientImpl(final ConfigCommon conf,
                         final String id) {
    this.id = id;
    this.conf = conf;
  }

  public void reinit(final String authUser,
                     final String runAsUser,
                     final String calSuiteName,
                     final ClientType clientType,
                     final boolean publicView) {
    currentPrincipal = null;
    this.clientType = clientType;

    pars = CalSvcIPars.getRoClientPars(id,
                                       authUser,
                                       runAsUser,
                                       calSuiteName,
                                       getPublicAuth());
    svci = new CalSvcFactoryDefault().getSvc(pars);
    this.publicView = publicView;
    resetIndexers();
    this.calSuiteName = calSuiteName;
  }
  
  protected void resetIndexers() {
    publicIndexers.clear();
    userIndexers.clear();
  }

  protected BwPrincipal<?> getCurrentCalSuiteOwner() {
    if (calSuiteOwner != null) {
      return calSuiteOwner;
    }

    if (calSuiteName == null) {
      return null;
    }

    calSuite = getCalSuite(calSuiteName);
    if (calSuite != null) {
      final String owner = calSuite.getGroup().getOwnerHref();

      calSuiteOwner = getPrincipal(owner);
    }

    return calSuiteOwner;
  }

  @Override
  public Client copy(final String id) {
    final ROClientImpl cl = new ROClientImpl(conf, id);

    copyCommon(id, cl);

    cl.publicView = publicView;

    return cl;
  }

  protected void copyCommon(final String id,
                            final ROClientImpl cl) {
    cl.pars = (CalSvcIPars)pars.clone();
    cl.pars.setLogId(id);

    cl.svci = new CalSvcFactoryDefault().getSvc(cl.pars);
    cl.clientType = clientType;
    cl.calSuiteName = calSuiteName;
  }

  @Override
  public void requestIn(final int conversationType) {
    postNotification(new HttpEvent(SysEventBase.SysCode.WEB_IN));
    svci.setState("Request in");

    if (conversationType == conversationTypeUnknown) {
      svci.open();
      svci.beginTransaction();
      return;
    }

    if (svci.isRolledback()) {
      svci.close();
    }

    if (conversationType == conversationTypeOnly) {
              /* if a conversation is already started on entry, end it
                  with no processing of changes. */
      if (svci.isOpen()) {
        svci.setState("Request in - close");
        svci.endTransaction();
      }
    }

    if (conversationType == conversationTypeProcessAndOnly) {
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
          {
    requestEnd = System.currentTimeMillis();
    postNotification(
            new HttpOutEvent(SysEventBase.SysCode.WEB_OUT,
                             reqTimeMillis));
    svci.setState("Request out");
    publicIndexers.clear();
    userIndexers.clear();

    if (!isOpen()) {
      return;
    }

    if (conversationType == conversationTypeUnknown) {
      if (actionType != actionTypeAction) {
        flushAll();
      }
    } else {
      if ((conversationType == conversationTypeEnd) ||
              (conversationType == conversationTypeOnly)) {
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
  public void close() {
    svci.close();
  }

  @Override
  public ConfigCommon getConf() {
    return conf;
  }

  @Override
  public void flushAll() {
    svci.flushAll();
  }

  @Override
  public void postNotification(final SysEventBase ev) {
    svci.postNotification(ev);
  }

  @Override
  public String getCurrentChangeToken() {
    var evChg = getIndexer(isDefaultIndexPublic(),
                      docTypeEvent).currentChangeToken();
    if (evChg == null) {
      evChg = "";
    }

    return evChg + getIndexer(isDefaultIndexPublic(),
                              docTypeCollection).currentChangeToken();
  }

  @Override
  public boolean getPublicAdmin() {
    return publicAdmin;
  }

  @Override
  public boolean getWebSubmit() {
    return ClientType.submission == clientType;
  }

  @Override
  public boolean getPublicAuth() {
    return ClientType.publicAuth == clientType;
  }

  @Override
  public ClientType getClientType() {
    return clientType;
  }

  @Override
  public AuthProperties getAuthProperties() {
    return svci.getAuthProperties();
  }

  @Override
  public SystemProperties getSystemProperties() {
    return svci.getSystemProperties();
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
  public long getUserMaxEntitySize() {
    return svci.getUserMaxEntitySize();
  }

  @Override
  public boolean isDefaultIndexPublic() {
    return getWebSubmit() || getPublicAdmin() || isGuest();
  }

  @Override
  public boolean isPrincipal(final String val) {
    return svci.getDirectories().isPrincipal(val);
  }

  /* ------------------------------------------------------------
   *                     Directories
   * ------------------------------------------------------------ */

  @Override
  public DirectoryInfo getDirectoryInfo() {
    return svci.getDirectories().getDirectoryInfo();
  }

  @Override
  public String getCalendarAddress(final String user) {
    return svci.getDirectories().userToCaladdr(user);
  }

  @Override
  public String getPrimaryPublicPath() {
    synchronized (primaryPublicPathLocker) {
      if (primaryPublicPath == null) {
        final var primaryCal = svci.getCollectionsHandler()
                                   .getPrimaryPublicPath();
        if (primaryCal == null) {
          throw new BedeworkException("No primary calendar set");
        }

        primaryPublicPath = primaryCal.getPath();
      }

      return primaryPublicPath;
    }
  }

  @Override
  public String uriToCaladdr(final String val) {
    return svci.getDirectories().uriToCaladdr(val);
  }

  @Override
  public BwPrincipal<?> calAddrToPrincipal(final String cua) {
    return svci.getDirectories().caladdrToPrincipal(cua);
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
  public BwPrincipal<?> getCurrentPrincipal() {
    if (currentPrincipal == null) {
      currentPrincipal = (BwPrincipal<?>)svci.getPrincipal().clone();
    }

    return currentPrincipal;
  }

  @Override
  public BwPrincipal<?> getAuthPrincipal() {
    return svci.getPrincipalInfo().getAuthPrincipal();
  }

  @Override
  public BwPrincipal<?> getOwner() {
    return getCurrentPrincipal();
  }

  @Override
  public String getCurrentPrincipalHref() {
    return getCurrentPrincipal().getPrincipalRef();
  }

  @Override
  public String getCurrentCalendarAddress() {
    if (currentCalendarAddress == null) {
      currentCalendarAddress = svci.getDirectories().principalToCaladdr(getCurrentPrincipal());
    }

    return currentCalendarAddress;
  }

  @Override
  public BwPrincipal<?> getUser(final String val) {
    return svci.getUsersHandler().getUser(val);
  }

  @Override
  public String makePrincipalUri(final String id,
                                 final int whoType) {
    return svci.getDirectories().makePrincipalUri(id, whoType);
  }

  @Override
  public BwPrincipal<?> getPrincipal(final String href)
          {
    return svci.getDirectories().getPrincipal(href);
  }

  @Override
  public boolean validPrincipal(final String href) {
    return svci.getDirectories().validPrincipal(href);
  }

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  @Override
  public Collection<BwGroup<?>> getAdminGroups()
          {
    return refreshAdminGroupInfo();
  }

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  @Override
  public BwPreferences getPreferences() {
    if (publicView) {
      final BwPreferences prefs = getCalsuitePreferences();
      if (prefs != null) {
        return prefs;
      }
    }

    return svci.getPrefsHandler().get();
  }

  public BwPreferences getCalsuitePreferences() {
    return getCalsuitePreferences(getCalSuite());
  }

  @Override
  public BwPreferences getPreferences(final String user) {
    return null;
  }

  @Override
  public void updatePreferences(final BwPreferences val)
          {
    throw new BedeworkException("org.bedework.read.only.client");
  }

  @Override
  public String getPreferredCollectionPath(final String compName)
          {
    return svci.getCollectionsHandler().getPreferred(compName);
  }

  /** Set false to inhibit lastLocale stuff */
  public static boolean tryLastLocale = true;

  @Override
  public Locale getUserLocale(final Collection<Locale> locales,
                              final Locale locale) {
    final Collection<Locale> sysLocales = getSupportedLocales();

    if (locale != null) {
      /* See if it's acceptable */
      final Locale l = BwLocale.matchLocales(sysLocales, locale);
      if (l != null) {
        if (debug()) {
          debug("Setting locale to " + l);
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
        if (debug()) {
          debug("Setting locale to " + l);
        }
        return l;
      }
    }

    if (lastLocaleStr != null) {
      final Locale l = BwLocale.matchLocales(sysLocales,
                                             makeLocale(lastLocaleStr));
      if (l != null) {
        if (debug()) {
          debug("Setting locale to " + l);
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
          if (debug()) {
            debug("Setting locale to " + l);
          }
          return l;
        }
      }
    }

    /* Use the first from supported locales -
     * there's always at least one in the collection */
    final Locale l = sysLocales.iterator().next();

    if (debug()) {
      debug("Setting locale to " + l);
    }
    return l;
  }

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  @Override
  public BwCollection getHome() {
    return svci.getCollectionsHandler().getHome();
  }

  @Override
  public BwCollection getCollection(final String path) {
    checkUpdate();
    return svci.getCollectionsHandler().get(path);
  }

  @Override
  public boolean collectionExists(final String path)
          {
    checkUpdate();
    return getCollection(path) != null;
  }

  @Override
  public BwCollection getSpecial(final int calType,
                                 final boolean create)
          {
    checkUpdate();

    return svci.getCollectionsHandler().getSpecial(calType, create);
  }

  @Override
  public BwCollection resolveAlias(final BwCollection val,
                                   final boolean resolveSubAlias,
                                   final boolean freeBusy)
          {
    checkUpdate();
    return svci.getCollectionsHandler().resolveAliasIdx(val,
                                                        resolveSubAlias,
                                                        freeBusy);
  }

  @Override
  public Collection<BwCollection> getChildren(final BwCollection col)
          {
    checkUpdate();

    if (!col.getPublick()) {
      return svci.getCollectionsHandler().getChildrenIdx(col);
    }

    // This and its children need to be cached
    BwCollection ourCopy;

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

      Collection<BwCollection> children = ourCopy.getChildren();
      if (children != null) {
        // Assume ok
        return children;
      }

      children = col.getChildren();
      if (children == null) {
        // Have to retrieve
        children = svci.getCollectionsHandler().getChildrenIdx(col);
      }

      // Assume we have to clone

      final Collection<BwCollection> ourChildren = new ArrayList<>();

      for (final BwCollection ch: children) {
        BwCollection ourCh = ch;
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
  public SynchStatusResponse getSynchStatus(final BwCollection val) {
    return svci.getCollectionsHandler().getSynchStatus(val);
  }

  @Override
  public Collection<BwCollection> decomposeVirtualPath(final String vpath)
          {
    return svci.getCollectionsHandler().decomposeVirtualPath(vpath);
  }

  @Override
  public String getPublicCalendarsRootPath() {
    return svci.getCollectionsHandler().getPublicCollectionsRootPath();
  }

  @Override
  public BwCollection getPublicCalendars() {
    checkUpdate();

    final String path = svci.getCollectionsHandler().getPublicCollectionsRootPath();

    BwCollection res = publicCloned.get(path);

    if (res != null) {
      return res;
    }

    res = svci.getCollectionsHandler().getPublicCollections();
    
    if (res == null) {
      warn("*****************************************************" +
           "Unable to retrieve public calendar root " + path +
           "*****************************************************");
      return null;
    }

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
  public BwCollection getHome(final BwPrincipal<?> principal,
                              final boolean freeBusy)
          {
    return svci.getCollectionsHandler().getHome(principal, freeBusy);
  }

  public void flushCached() {
    synchronized (publicCloned) {
      publicCloned.clear();
    }
  }

  private final static Map<String, BwCollection> publicCloned =
      new HashMap<>();

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  @Override
  public GetEntityResponse<BwCategory> getCategoryByName(final BwString name) {
    checkUpdate();
    return svci.getCategoriesHandler().findPersistent(name);
  }

  @Override
  public BwCategory getCategoryByUid(final String uid) {
    checkUpdate();
    final var resp = svci.getCategoriesHandler().getByUid(uid);
    checkResponse(resp);
    return resp.getEntity();
  }

  @Override
  public BwCategory getCategory(final String href)
          {
    checkUpdate();
    return svci.getCategoriesHandler().get(href);
  }

  @Override
  public Collection<BwCategory> getCategories()
          {
    checkUpdate();
    return svci.getCategoriesHandler().get();
  }

  @Override
  public Collection<BwCategory> getPublicCategories()
          {
    checkUpdate();
    return svci.getCategoriesHandler().getPublic();
  }

  @Override
  public Collection<BwCategory> getEditableCategories()
          {
    checkUpdate();
    return svci.getCategoriesHandler().getEditable();
  }

  @Override
  public Set<String> getDefaultPublicCategoryUids()
          {
    final Set<String> catUids = new TreeSet<>();

    for (final BwCalSuite suite: svci.getCalSuitesHandler().getAll()) {
      final BwPreferences prefs = getCalsuitePreferences(suite);

      if ((prefs != null) && (prefs.getDefaultCategoryUids() != null)) {
        catUids.addAll(prefs.getDefaultCategoryUids());
      }
    }

    return catUids;
  }

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  @Override
  public GetEntityResponse<BwContact> getContactByUid(final String uid) {
    checkUpdate();
    final var resp = svci.getContactsHandler().getByUid(uid);
    checkResponse(resp);
    return resp;
  }

  @Override
  public Collection<BwContact> getContacts()
          {
    checkUpdate();
    return svci.getContactsHandler().get();
  }

  @Override
  public GetEntitiesResponse<BwContact> getContacts(final String fexpr,
                                                    final int from,
                                                    final int size) {
    return svci.getContactsHandler().find(fexpr, from, size);
  }

  @Override
  public Collection<BwContact> getPublicContacts()
          {
    checkUpdate();
    return svci.getContactsHandler().getPublic();
  }

  @Override
  public Collection<BwContact> getEditableContacts()
          {
    checkUpdate();
    return svci.getContactsHandler().getEditable();
  }

  @Override
  public GetEntityResponse<BwContact> findContact(final BwString val) {
    final var resp = svci.getContactsHandler().findPersistent(val);
    checkResponse(resp);
    return resp;
  }

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  @Override
  public GetEntityResponse<BwLocation> getLocationByUid(final String uid) {
    checkUpdate();
    final var resp = svci.getLocationsHandler().getByUid(uid);
    checkResponse(resp);
    return resp;
  }

  @Override
  public Collection<BwLocation> getLocations()
          {
    checkUpdate();
    return svci.getLocationsHandler().get();
  }

  @Override
  public GetEntitiesResponse<BwLocation> getLocations(final String fexpr,
                                                      final int from,
                                                      final int size) {
    return svci.getLocationsHandler().find(fexpr, from, size);
  }

  @Override
  public Collection<BwLocation> getPublicLocations()
          {
    checkUpdate();
    return svci.getLocationsHandler().getPublic();
  }

  @Override
  public Collection<BwLocation> getEditableLocations()
          {
    checkUpdate();
    return svci.getLocationsHandler().getEditable();
  }

  @Override
  public GetEntityResponse<BwLocation> findLocation(final BwString address) {
    final var resp = svci.getLocationsHandler().findPersistent(address);
    checkResponse(resp);
    return resp;
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByCombined(
          final String val,
          final boolean persisted) {
    return svci.getLocationsHandler().fetchLocationByCombined(val,
                                                              persisted);
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByKey(
          final String name,
          final String val) {
    return svci.getLocationsHandler().fetchLocationByKey(name, val);
  }

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  @Override
  public GetEntitiesResponse<EventInfo> getEventByUid(final String path,
                                             final String guid,
                                             final String rid,
                                             final RecurringRetrievalMode recurRetrieval) {
    final GetEntitiesResponse<EventInfo> resp = new GetEntitiesResponse<>();

    try {
      final var ents =
              svci.getEventsHandler()
                  .getByUid(path, guid,
                            rid,
                            recurRetrieval);
      if (Util.isEmpty(ents)) {
        resp.setStatus(Response.Status.notFound);
      } else {
        resp.setEntities(ents);
      }

      return resp;
    } catch (final Throwable t) {
      checkResponse(resp); // Will force an error
      return resp.error(t);
    }
  }

  @Override
  public SearchParams getSearchParams() {
    return searchParams;
  }

  @Override
  public EventInfo getEvent(final String colPath,
                            final String name,
                            final String recurrenceId) {
    return svci.getEventsHandler().get(colPath, name, recurrenceId);
  }

  @Override
  public EventInfo getEvent(final String href)
          {
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
          {
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
                                             noDeleted,
                                             rrm);
  }

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  @Override
  public void setViewMode(final String val) {
    viewMode = val;
  }

  @Override
  public String getViewMode() {
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
  public BwView getView(final String val) {
    checkUpdate();
    return svci.getViewsHandler().find(val);
  }

  @Override
  public Collection<BwView> getAllViews() {
    if (getPublicAuth() && (getCurrentCalSuiteOwner() != null)) {
      return svci.getViewsHandler().getAll(getCurrentCalSuiteOwner());
    }
    return svci.getViewsHandler().getAll();
  }

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  @Override
  public void flushState() {
  }

  /* ------------------------------------------------------------
   *                     Search
   * ------------------------------------------------------------ */

  @Override
  public void clearSearch() {
    searchParams = null;
    lastSearch = null;
  }

  @Override
  public void clearSearchEntries() {
    lastSearchEntries = null;
  }

  @Override
  public SearchResult search(final SearchParams params) {
    checkUpdate();
    searchParams = params;

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

    final DeletedState delState;

    if (getPublicAdmin() && isSuperUser()) {
      delState = includeDeleted;
    } else {
      delState = noDeleted;
    }

    lastSearch = getIndexer(publicIndex, docTypeEvent).search(
            params.getQuery(),
            params.getRelevance(),
            params.getFilter(),
            params.getSort(),
            getDefaultFilterContext(),
            start,
            end,
            params.getPageSize(),
            delState,
            params.getRecurMode());

    return lastSearch;
  }

  @Override
  public List<SearchResultEntry> getSearchResult(final Position pos) {
    checkUpdate();
    if (lastSearch == null) {
      return null;
    }

    if ((pos == Position.current) && (lastSearchEntries != null)) {
      return lastSearchEntries;
    }

    lastSearchEntries = formatSearchResult(lastSearch.getIndexer().
            getSearchResult(lastSearch, pos, PrivilegeDefs.privAny));

    if ((lastSearch != null) && (searchParams != null)) {
      searchParams.setCurOffset(lastSearch.getLastPageStart());
    }

    return lastSearchEntries;
  }

  @Override
  public List<SearchResultEntry> getSearchResult(final int start,
                                                 final int num) {
    if (lastSearch == null) {
      return new ArrayList<>(0);
    }
    checkUpdate();

    return formatSearchResult(
            lastSearch.getIndexer()
                      .getSearchResult(lastSearch,
                                       start, num,
                                       PrivilegeDefs.privRead));
  }

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */

  @Override
  public BwCalSuiteWrapper getCalSuite() {
    return svci.getCalSuitesHandler().get();
  }

  private static Collection<BwCalSuite> suites;

  @Override
  public Collection<BwCalSuite> getContextCalSuites()
          {
    refreshAdminGroupInfo();

    return suites;
  }

  @Override
  public BwCalSuiteWrapper getCalSuite(final String name)
          {
    return svci.getCalSuitesHandler().get(name);
  }

  /* ------------------------------------------------------------
   *                   Filters
   * ------------------------------------------------------------ */

  @Override
  public GetFilterDefResponse getFilter(final String name) {
    return svci.getFiltersHandler().get(name);
  }

  @Override
  public ParseResult parseFilter(final BwFilterDef val) {
    return svci.getFiltersHandler().parse(val);
  }

  @Override
  public ParseResult parseSort(final String val) {
    return svci.getFiltersHandler().parseSort(val);
  }

  @Override
  public Collection<BwFilterDef> getAllFilters()
          {
    return svci.getFiltersHandler().getAll();
  }

  /* ------------------------------------------------------------
   *                   protected methods
   * ------------------------------------------------------------ */

  protected BwPreferences getCalsuitePreferences(final BwCalSuite cs) {
    if (cs == null) {
      return null;
    }

    final String csHref = cs.getGroup().getOwnerHref();

    final BwPrincipal<?> p = getUser(csHref);
    if (p == null) {
      return null;
    }

    return svci.getPrefsHandler().get(p);
  }

  protected Collection<BwGroup<?>> refreshAdminGroupInfo() {
    final var res = adminGroupsInfo; // Save in case adminGroupsInfo set to null
    if ((res != null) &&
            (System.currentTimeMillis() < (lastAdminGroupsInfoRefresh +
                                                   adminGroupsInfoRefreshInterval))) {
      return res;
    }

    synchronized (adminGroupLocker) {
      final Set<String> groupHrefs = new TreeSet<>();

      suites = new ArrayList<>();

      for (final BwCalSuite suite: svci.getCalSuitesHandler().getAll()) {
        final BwCalSuite cs = (BwCalSuite)suite.clone();

        // For the moment we skip suites if the group description starts with "INACTIVE"
        final String desc = cs.getGroup().getDescription();

        if ((desc != null) && desc.startsWith("INACTIVE")) {
          continue;
        }

        groupHrefs.add(cs.getGroup().getPrincipalRef());

        cs.setContext(null);
        cs.setDefaultContext(false);

        suites.add(cs);
      }

      adminGroupsInfo = new ArrayList<>();
      calsuiteAdminGroupsInfo = new ArrayList<>();
      
      final Map<String, BwPrincipal<?>> cloned = new HashMap<>();

      final var ags =
              svci.getAdminDirectories().getAll(true);

      for (final var g: ags) {
        final var cg = cloneGroup(g, cloned);

        if (groupHrefs.contains(cg.getPrincipalRef())) {
          calsuiteAdminGroupsInfo.add(cg);
        }

        // Set the memberships for this group.
        final var mgs = getAllAdminGroups(g);

        for (final var mg: mgs) {
          final var cmg = cloneGroup(mg, cloned);

          cg.addGroup(cmg);
        }

        adminGroupsInfo.add(cg);
      }

      lastAdminGroupsInfoRefresh = System.currentTimeMillis();

      return adminGroupsInfo;
    }
  }

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>This does check the groups for membership of other groups so the
   * returned collection gives the groups of which the principal is
   * directly or indirectly a member.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   */
  private Collection<BwGroup<?>> getAllAdminGroups(final BwPrincipal<?> val)
          {
    return svci.getAdminDirectories().getAllGroups(val);
  }

  private BwGroup<?> cloneGroup(final BwGroup<?> g,
                                final Map<String, BwPrincipal<?>> cloned) {
    var cg = (BwGroup<?>)cloned.get(g.getPrincipalRef());

    if (cg != null) {
      return cg;
    }
    
    cg = g.shallowClone();
    cloned.put(g.getPrincipalRef(), cg);

    final var ms = g.getGroupMembers();
    if (ms == null) {
      return cg;
    }

    for (final var mbr: ms) {
      BwPrincipal<?> cmbr = cloned.get(mbr.getPrincipalRef());

      if (cmbr == null) {
        if (mbr instanceof BwGroup) {
          cmbr = cloneGroup((BwGroup<?>)mbr, cloned);
        } else {
          cmbr = (BwPrincipal<?>)mbr.clone();
        }
        cloned.put(mbr.getPrincipalRef(), cmbr);
      }
      cg.addGroupMember(cmbr);
    }
    
    return cg;
  }

  protected String getCSResourcesPath(final BwCalSuite suite,
                                      final String rc) {
    final ResourceClass csRc = ResourceClass.valueOf(rc);

    return svci.getCalSuitesHandler().getResourcesPath(suite, csRc);

    /*
    if (rc.equals(CalSuiteResource.resourceClassGlobal)) {
      return Util.buildPath(false, getBasicSyspars().getGlobalResourcesPath());
    }

    final BwPrincipal<?> eventsOwner = getPrincipal(suite.getGroup().getOwnerHref());

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

  /*
  protected BasicSystemProperties getBasicSyspars() {
    return svci.getBasicSystemProperties();
  }
  */

  protected CollectionCollator<BwCollection> getCalendarCollator() {
    if (calendarCollator == null) {
      calendarCollator = new CollectionCollator<>();
    }

    return calendarCollator;
  }

  protected BwIndexer getIndexer(final boolean publick,
                                 final String docType) {
    if (publick) {
      BwIndexer idx = publicIndexers.get(docType);
      if (idx == null) {
        idx = svci.getIndexer(true, docType);
        publicIndexers.put(docType, idx);
      }

      return idx;
    }

    BwIndexer idx = userIndexers.get(docType);
    if (idx == null) {
      idx = svci.getIndexer(false, docType);
      userIndexers.put(docType, idx);
    }

    return idx;
  }

  private final static long indexerDelay = 1010; // Just over a sec for the indexer

  protected <T> T update(final T val) {
    updated();
    return val;
  }

  protected void updated() {
    lastUpdate = System.currentTimeMillis();
    synchronized (adminGroupLocker) {
      adminGroupsInfo = null;
    }
    synchronized (primaryPublicPathLocker) {
      primaryPublicPath = null;
    }
  }

  protected void checkUpdate() {
    /*
    if (debug()) {
      debug("checkUpdate: \n" +
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
      } catch (final InterruptedException ignored) {
        //throw new BedeworkException(ie);
        // Assume we're shutting down.
      }

      lastUpdate = 0; // Only wait once
    }
  }

  protected Locale makeLocale(final String val) {
      return Util.makeLocale(val);
  }

  private final static FlushMap<String, FilterBase> defaultFilters =
          new FlushMap<>();

  protected FilterBase getDefaultFilterContext() {
    final var pr = getCurrentPrincipal();
    final String phref;
    if (pr == null) {
      phref = null;
    } else {
      phref = pr.getPrincipalRef();
    }

    FilterBase tblVal = defaultFilters.get(phref);
    if (tblVal != null) {
      return tblVal;
    }

    final BwView preferred = getView(null);

    if (preferred == null) {
      tblVal = makeDefaultView();
    } else {
      final String fexpr = "view=\"" + preferred.getName() + "\"";

      final BwFilterDef fd = new BwFilterDef();
      fd.setDefinition(fexpr);

      parseFilter(fd);

      tblVal = fd.getFilters();

      if (tblVal == null) {
        warn("Null filter for " + phref);
      }
    }

    synchronized (defaultFilters) {
      defaultFilters.put(phref, tblVal);
    }

    return tblVal;
  }

  /* ------------------------------------------------------------
   *                   private methods
   * ------------------------------------------------------------ */

  private FilterBase makeDefaultView() {
    final Collection<BwCollection> cols = new ArrayList<>();

    findCollections(getHome(), cols);

    FilterBase flt = null;

    for (final BwCollection col: cols) {
      flt = FilterBase.addOrChild(flt,
                                  new BwCollectionFilter(col.getName(),
                                                         col));
    }

    return flt;
  }

  private void findCollections(final BwCollection root,
                               final Collection<BwCollection> cols)
          {
    if (root.getCalendarCollection()) {
      cols.add(root);
      return;
    }

    for (final var ch: svci.getCollectionsHandler().getChildren(root)) {
      findCollections(ch, cols);
    }
  }

  private List<SearchResultEntry> formatSearchResult(
          final List<SearchResultEntry> entries) {
    final IcalTranslator trans = new IcalTranslator(
            new IcalCallbackcb(this));

    for (final SearchResultEntry sre: entries) {
      final Object o = sre.getEntity();

      if (!(o instanceof EventInfo)) {
        continue;
      }

      final EventFormatter ef = new EventFormatter(this,
                                                   trans,
                                                   (EventInfo)o);
      sre.setEntity(ef);
    }

    return entries;
  }

  private Collection<Locale> getSupportedLocales() {
    if (supportedLocales != null) {
      return supportedLocales;
    }

    supportedLocales = new ArrayList<>();

    final String ll = getSystemProperties().getLocaleList();

    if (ll == null) {
      supportedLocales.add(BwLocale.getLocale());
      return supportedLocales;
    }

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

    if (supportedLocales.isEmpty()) {
      supportedLocales.add(BwLocale.getLocale());
    }

    return supportedLocales;
  }

  protected void checkResponse(final Response<?> resp) {
    if (!resp.isError()) {
      return;
    }

    final var exc = resp.getException();
    if (exc instanceof RuntimeException) {
      throw (RuntimeException)exc;
    }

    if (exc != null) {
      throw new RuntimeException(exc);
    }

    throw new RuntimeException(resp.toString());
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
