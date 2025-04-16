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

import org.bedework.appcommon.ConfigCommon;
import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResult;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calsvci.CalendarsI.SynchStatusResponse;
import org.bedework.sysevents.events.SysEventBase;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This interface defines the interactions with the back end system
 * for a client.
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public interface Client extends Serializable {
  enum ClientType {
    // Unauthenticated
    guest,

    // Pretty much same as guest but it's the public client view
    publick,

    // Again much the same but a public feeder
    feeder,

    // public client requiring authentication. Acts like guest mostly
    publicAuth,

    // Submission client
    submission,

    // Personal/group scheduling
    personal,

    // Admin client
    admin
  }

  /**
   * @param id   provide an id for logging and tracing.
   * @return a copy of this client which can be used for an asynchronous
   * action. Client copies should be discarded on completion of the request
   * cycle.
   */
  Client copy(String id);

  /** Call on the way in once we have a client object.
   *
   * @param conversationType one off, initial, last or a continuation
   */
  void requestIn(int conversationType);

  /** Call on the way out.
   *
   * @param conversationType one off, initial, last or a continuation
   * @param actionType action or render
   * @param reqTimeMillis time for request.
   */
  void requestOut(int conversationType,
                  int actionType,
                  long reqTimeMillis);

  /**
   * @return boolean true if open
   */
  boolean isOpen();

  /** Call on the way out after handling a request..
   *
   */
  void close();

  /**
   *
   * @return current configuration
   */
  ConfigCommon getConf();

  /** Flush any backend data we may be hanging on to ready for a new
   * sequence of interactions. This is intended to help with web based
   * applications, especially those which follow the action/render url
   * pattern used in portlets.
   *
   * <p>A flushAll can discard a back end session allowing open to get a
   * fresh one. close() can then be either a no-op or something like a
   * hibernate disconnect.
   *
   * <p>This method should be called before calling open (or after calling
   * close).
   *
   */
  void flushAll();

  /** Send a notification event
   *
   * @param ev - system event
   */
  void postNotification(SysEventBase ev);

  /**
   * @return a change token for the current indexed data
   */
  String getCurrentChangeToken();

  /**
   *
   * @return true if we are doing public admin.
   */
  boolean getPublicAdmin();

  /**
   *
   * @return true if we are the web submit client.
   */
  boolean getWebSubmit();

  /**
   *
   * @return true if we are the authenticated public client.
   */
  boolean getPublicAuth();

  /** apptype
   *
   * @return the client type
   */
  ClientType getClientType();

  /** Return authentication relevant properties.
   *
   * @return AuthProperties object - never null.
   */
  AuthProperties getAuthProperties();

  /** Return properties about the system.
   *
   * @return SystemProperties object - never null.
   */
  SystemProperties getSystemProperties();

  /**
   * If possible roll back the changes.
   */
  void rollback();

  /**
   * @return System limit or user overrride - bytes.
   */
  long getUserMaxEntitySize();

  boolean isDefaultIndexPublic();

  /* ------------------------------------------------------------
   *                     Directories
   * ------------------------------------------------------------ */

  /** Get application visible directory information.
   *
   * @return DirectoryInfo
   */
  DirectoryInfo getDirectoryInfo();

  /**
   *
   * @param cua   calendar user address
   * @return principal corresponding or null for unknown
   */
  BwPrincipal<?> calAddrToPrincipal(String cua);

  /**
   *
   * @param user          String user id
   * @return calendar address or null for unknown user
   */
  String getCalendarAddress(String user);

  /**
   *
   * @return path of primary public calendar
   */
  String getPrimaryPublicPath();

  /** Given a uri return a calendar address.
   * This should handle actions such as turning<br/>
   *   auser
   * <br/>into the associated calendar address of <br/>
   *   mailto:auser@ahost.org
   *
   * <p>It should also deal with user@somewhere.org to
   * mailto:user@somewhere.org
   *
   * <p>Note: this method and userToCalAddr should be doing lookups of the
   * enterprise directory (or carddav) to determine the calendar user address.
   * For the moment we do a transform of the account to getResource a mailto.
   *
   * @param val        uri
   * @return caladdr for this system or null for an invalid uri
   */
  String uriToCaladdr(String val);

  /* ------------------------------------------------------------
   *                     Principals
   * ------------------------------------------------------------ */

  /**
   *
   * @return true for a superuser.
   */
  boolean isSuperUser();

  /**
   *
   * @return true for guest (read-only) mode.
   */
  boolean isGuest();

  /** Does the value appear to represent a valid principal?
   *
   * @param val to test
   * @return true if it's a (possible) principal
   */
  boolean isPrincipal(String val);

  /** This may change as we switch groups.
   *
   * @return the current principal we are acting for
   */
  BwPrincipal<?> getCurrentPrincipal();

  /** This will not change.
   *
   * @return the principal we authenticated as
   */
  BwPrincipal<?> getAuthPrincipal();

  /** Get the current principal to set as owner
   *
   * @return BwPrincipal<?> object
   */
  BwPrincipal<?> getOwner();

  /**
   *
   * @return href for current principal
   */
  String getCurrentPrincipalHref();

  /** Find the user with the given account name.
   *
   * @param val           String user id
   * @return User principal or null if not there
   */
  BwPrincipal<?> getUser(String val);

  /**
   *
   * @return current calendar address
   */
  String getCurrentCalendarAddress();

  /**
   * @param id of account
   * @param whoType - from WhoDefs
   * @return String principal uri
   */
  String makePrincipalUri(String id,
                          int whoType);

  /** Return principal for the given href.
   *
   * @param href of principal
   * @return Principal
   */
  BwPrincipal<?> getPrincipal(String href);

  /** Test for a valid principal in the directory. This may have a number of
   * uses. For example, when organizing meetings we may want to send an
   * invitation to a user who has not yet logged on. This allows us to
   * distinguish between a bad account (spam maybe?) and a real account.
   *
   * <p>Sites may wish to override this method to check their directory to see
   * if the principal exists.
   *
   * @param href of the principal
   * @return true if it's a valid principal
   */
  boolean validPrincipal(String href);

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  /**
   * @return groups
   */
  Collection<BwGroup<?>> getAdminGroups();

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  /** Returns the current user preferences.
   *
   * @return BwPreferences   prefs for the current user
   */
  BwPreferences getPreferences();

  /** Returns the current calsuite preferences.
   *
   * @return BwPreferences   prefs for the current calsuite
   */
  BwPreferences getCalsuitePreferences();

  /** Returns the given user's preferences. Only valid for superuser
   *
   * @param user - account
   * @return BwPreferences   prefs for the user - null for no access
   */
  BwPreferences getPreferences(String user);

  /** Update the user preferences.
   *
   * @param  val     BwPreferences prefs for a user
   */
  void updatePreferences(BwPreferences val);

  /** Get the default calendar path for the current user.
   *
   * @param compName - name of component - "VEVENT" etc
   * @return String path or null for no preference
   */
  String getPreferredCollectionPath(String compName);

  /** Given a (possibly null) list of locales, and/or an explicitly requested locale,
   * figure out what locale to use based on user preferences and system defaults.
   * The order of preference is<ol>
   * <li>Explicitly requested.</li>
   * <li>User preference</li>
   * <li>If supported - last locale set for user</li>
   * <li>Matching entry from supplied list - see below</li>
   * <li>System default</li>
   * </ol>
   *
   * <p>The locale parameter is a possibly null explicitly requested locale.
   *
   * <p>Matching locales is first attempted using Locale equality. If that fails
   * we try to match languages on the assumption that somebody requesting fr_FR
   * is likely to be happier with fr_CA than en_US.
   *
   * @param locales possibly null list of locales
   * @param locale possibly null explicitly requested locale
   * @return Collection of locales.
   */
  Locale getUserLocale(Collection<Locale> locales,
                       Locale locale);

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  /**
   * @return home for current principal
   */
  BwCalendar getHome();

  /**
   * @param path for collection
   * @return collection object or null.
   */
  BwCalendar getCollection(String path);

  /**
   * @param path for collection
   * @return true if it exists and user has access.
   */
  boolean collectionExists(String path);

  /** Get a special calendar (e.g. Trash) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   */
  BwCalendar getSpecial(int calType,
                        boolean create);

  /** Attempt to getResource collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val collection
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy true for a freebusy request
   * @return BwCalendar
   */
  BwCalendar resolveAlias(BwCalendar val,
                          boolean resolveSubAlias,
                          boolean freeBusy);

  /** Returns children of the given collection to which the current user has
   * some access. Both the col value and the returned children may be
   * cloned non-persistent objects
   *
   * @param  col          parent collection
   * @return Collection   of BwCalendar
   */
  Collection<BwCalendar> getChildren(BwCalendar col)
         ;

  /**
   *
   * @param val collection
   * @return never null - requestStatus set for not an external subscription.
   */
  SynchStatusResponse getSynchStatus(BwCalendar val);

  /** A virtual path might be for example "/user/adgrp_Eng/Lectures/Lectures"
   * which has two two components<ul>
   * <li>"/user/adgrp_Eng/Lectures" and</li>
   * <li>"Lectures"</li></ul>
   *
   * <p>
   * "/user/adgrp_Eng/Lectures" is a real path which is an alias to
   * "/public/aliases/Lectures" which is a folder containing the alias
   * "/public/aliases/Lectures/Lectures" which is aliased to the single calendar.
   *
   * @param vpath A virtual path
   * @return collection of collection objects - null for bad vpath
   */
  Collection<BwCalendar> decomposeVirtualPath(String vpath);

  /** Returns the href of the root of the public calendars tree.
   *
   * @return String   root path
   */
  String getPublicCalendarsRootPath();

  /** Returns the cloned root of the tree of public calendars.
   *
   * @return BwCalendar   root
   */
  BwCalendar getPublicCalendars();

  /** Returns root of calendars owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *<p>
   * Note: the returned object is NOT a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCalendar   user home.
   */
  BwCalendar getHome(BwPrincipal<?> principal,
                     boolean freeBusy);

  /**
   * Flush any cached (public) copies of collections.
   */
  void flushCached();

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  /**
   * @param name unique (for current user) value for the category -
   *             possible language set.
   * @return category entity or null.
   */
  GetEntityResponse<BwCategory> getCategoryByName(BwString name);

  /**
   * @param uid of the category
   * @return category entity or null.
   */
  BwCategory getCategoryByUid(String uid);

  /**
   * @param href of the category
   * @return category entity or null.
   */
  BwCategory getCategory(String href);

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwCategory> getCategories();

  /** Return all public entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwCategory> getPublicCategories();

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwCategory> getEditableCategories();

  /** Return all default category uids for public suites.
   *
   * <p>Returns an empty collection for none.
   *
   * @return Collection     of uids
   */
  Set<String> getDefaultPublicCategoryUids();

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  /** Get the contact with the given uid.
   *
   * @param uid of contact
   * @return contact object
   */
  GetEntityResponse<BwContact> getContactByUid(String uid);

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwContact> getContacts();

  /** Return all current user entities that match the filter.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param fexpr filter expression
   * @return Collection     of objects
   */
  GetEntitiesResponse<BwContact> getContacts(String fexpr,
                                             int from,
                                             int size)
         ;

  /** Return all public entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwContact> getPublicContacts();

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwContact> getEditableContacts();

  /** Find the contact.
   *
   * @param val to match
   * @return contact object
   */
  GetEntityResponse<BwContact> findContact(BwString val);

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  /** Get the location with the given uid.
   *
   * @param uid of location
   * @return Location object
   */
  GetEntityResponse<BwLocation> getLocationByUid(String uid);

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwLocation> getLocations();

  /** Return all current user entities that match the filter.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param fexpr filter expression
   * @return Collection     of objects
   */
  GetEntitiesResponse<BwLocation> getLocations(String fexpr,
                                               int from,
                                               int size);

  /** Return all public entiities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwLocation> getPublicLocations();

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   */
  Collection<BwLocation> getEditableLocations();

  /** Find the location given the address.
   *
   * @param address to be found
   * @return Location object
   */
  GetEntityResponse<BwLocation> findLocation(BwString address);

  /** Find the location given the combined address values.
   *
   * @param val - address, room, city, state, zip
   * @param persisted - true if we want the db copy
   * @return Location object
   */
  GetEntityResponse<BwLocation> fetchLocationByCombined(String val,
                                                        boolean persisted);

  /** Find a location owned by the current user which has a named
   * key field which matches the value.
   *
   * @param name - of key field
   * @param val - expected full value
   * @return null or location object
   */
  GetEntityResponse<BwLocation> fetchLocationByKey(String name,
                                                   String val);

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  /** Return a Collection of EventInfo objects. More than one for a recurring
   * event with overrides.
   *
   * @param path       collection to search
   * @param guid uid we want
   * @param rid recurrence id
   * @param recurRetrieval How recurring event is returned.
   * @return response with status and Collection of EventInfo
   */
  GetEntitiesResponse<EventInfo> getEventByUid(String path,
                                               String guid,
                                               String rid,
                                               RecurringRetrievalMode recurRetrieval);

  /** Get events given the calendar and String name. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   *
   * @param  colPath   String collection path
   * @param name       String possible name
   * @param recurrenceId null for entire event + overrides.
   * @return EventInfo or null
   */
  EventInfo getEvent(String colPath,
                     String name,
                     String recurrenceId);

  /** Get events given the href. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   *
   * @param href   String collection path / name [ # recurrenceid ]
   * @return EventInfo or null
   */
  EventInfo getEvent(String href);

  /** Return the events for the current user within the given date and time
   * range. If retrieveList is supplied only those fields (and a few required
   * fields) will be returned.
   *
   * @param filter       String filter restricting search or null.
   * @param startDate    BwDateTime start - may be null
   * @param endDate      BwDateTime end - may be null.
   * @param expanded     How recurring event is returned - expanded means
   *                     expand all instances to full form. Otherwise
   *                     just return overrides.
   * @return Collection  populated event value objects
   */
  Collection<EventInfo> getEvents(String filter,
                                  BwDateTime startDate,
                                  BwDateTime endDate,
                                  boolean expanded)
         ;

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  String listViewMode = "list";
  String gridViewMode = "grid";

  /** Set the view mode
   *
   * @param val "list" or "grid" or null to revert to default
   */
  void setViewMode(String val);

  /**
   *
   * @return  "list" or "grid"
   */
  String getViewMode();

  /** Find the named view.
   *
   * @param  val     String view name - null means default
   * @return BwView  null view not found.
   */
  BwView getView(String val);

  /** Return the collection of views - named collections of collections
   *
   * @return collection of views
   */
  Collection<BwView> getAllViews();

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  /** Called if anything is changed which affects the state of the client, e.g
   * switching display flags, deleting collections.
   */
  void flushState();

  /* ------------------------------------------------------------
   *                     Search
   * ------------------------------------------------------------ */

  /** Called to reset any search. Signals to the application it needs
   * to rebuild the search
   *
   */
  void clearSearch();

  /** Called to force a refetch
   *
   */
  void clearSearchEntries();

  /** Called to search an index. If params.publick is false use the
   * current principal to identify the index.
   *
   * @param params  defining search
   * @return  SearchResult   never null
   */
  SearchResult search(SearchParams params);

  /**
   * @return SearchParams
   */
  SearchParams getSearchParams();

  /** Called to retrieve results after a search of the index. Updates
   * the current search result.
   *
   * @param pos - specify movement in result set
   * @return  list of SearchResultEntry
   */
  List<SearchResultEntry> getSearchResult(Position pos);

  /** Called to retrieve results after a search of the index. Updates
   * the current search result.
   *
   * @param start in result set
   * @param num of entries
   * @return  list of SearchResultEntry
   */
  List<SearchResultEntry> getSearchResult(int start,
                                          int num);

  /* ------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------ */

  /** Get the current calendar suite.
   *
   * @return BwCalSuiteWrapper null for unknown calendar suite
   */
  BwCalSuiteWrapper getCalSuite();

  /** Return the collection of cal suites filled in with sub-context information
   *
   * @return Calendar suites
   */
  Collection<BwCalSuite> getContextCalSuites();

  /** Get a calendar suite given the name
   *
   * @param  name     String name of calendar suite
   * @return BwCalSuiteWrapper null for unknown calendar suite
   */
  BwCalSuiteWrapper getCalSuite(String name);

  /* ------------------------------------------------------------
   *                     Filters
   * Possible these are used nowhere
   * ------------------------------------------------------------ */

  /** Get a filter given the name
   *
   * @param  name     String internal name of filter
   * @return GetFilterDefResponse withstatus set
   */
  GetFilterDefResponse getFilter(String name);

  /** Parse the definition in the given filter object
   *
   * @param  val       BwFilterDef
   */
  ParseResult parseFilter(BwFilterDef val);

  /** Parse the sort expression
   *
   * @param  val  String sort expression
   */
  ParseResult parseSort(String val);

  /** Get filter definitions to which this user has access
   *
   * @return Collection     of BwCalSuiteWrapper
   */
  Collection<BwFilterDef> getAllFilters();
}
