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
import org.bedework.appcommon.ConfigCommon;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
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
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.filter.SimpleFilterParser.ParseResult;
import org.bedework.calfacade.indexing.BwIndexer.Position;
import org.bedework.calfacade.indexing.SearchResult;
import org.bedework.calfacade.indexing.SearchResultEntry;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.responses.GetFilterDefResponse;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.CalendarsI.SynchStatusResponse;
import org.bedework.calsvci.EventProperties.EnsureEntityExistsResult;
import org.bedework.calsvci.EventsI.RealiasResult;
import org.bedework.calsvci.SchedulingI;
import org.bedework.calsvci.SharingI;
import org.bedework.sysevents.events.SysEventBase;
import org.bedework.util.misc.response.GetEntitiesResponse;
import org.bedework.util.misc.response.GetEntityResponse;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * This interface defines the interactions with the back end system
 * for a client.
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public interface Client extends Serializable {
  /**
   * @param id   provide an id for logging and tracing.
   * @return a copy of this client which can be used for an asynchronous
   * action. Client copies should be discarded on completion of the request
   * cycle.
   */
  Client copy(String id);

  /** Call on the way in once we have a client object.
   *
   * @param conversationType
   * @throws CalFacadeException on error
   */
  void requestIn(int conversationType) throws CalFacadeException;

  /** Call on the way out.
   *
   * @param conversationType
   * @param actionType
   * @param reqTimeMillis time for request.
   * @throws CalFacadeException on error
   */
  void requestOut(int conversationType,
                  int actionType,
                  long reqTimeMillis) throws CalFacadeException;

  /**
   * @return boolean true if open
   */
  boolean isOpen();

  /** Call on the way out after handling a request..
   *
   * @throws CalFacadeException on error
   */
  void close() throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  void flushAll() throws CalFacadeException;

  /** Send a notification event
   *
   * @param ev - system event
   */
  void postNotification(SysEventBase ev);

  /** End a (possibly long-running) transaction. In the web environment
   * this should in some way check version numbers to detect concurrent updates
   * and fail with an exception.
   *
   * @throws CalFacadeException on fatal error
   */
  void endTransaction() throws CalFacadeException;

  /**
   * @return a change token for the current indexed data
   * @throws CalFacadeException on fatal error
   */
  String getCurrentChangeToken() throws CalFacadeException;

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
   * @return boolean
   */
  String getAppType();

  /** Write the value as json to the response stream.
   *
   * @param resp to write to
   * @param val to output
   * @throws CalFacadeException on fatal error
   */
  void writeJson(HttpServletResponse resp,
                 Object val) throws CalFacadeException;

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

  /**
   * @param href to be unindexed
   * @throws CalFacadeException on fatal error
   */
  void unindex(String href) throws CalFacadeException;

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
  BwPrincipal calAddrToPrincipal(String cua);

  /**
   *
   * @param user          String user id
   * @return calendar address or null for unknown user
   * @throws CalFacadeException on fatal error
   */
  String getCalendarAddress(String user) throws CalFacadeException;

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

  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  boolean getAdminGroupMaintOK();

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * <p>This may need supplementing with changes to the jsp. For example,
   * it's hard to deal programmatically with the case of directory/roles
   * based authorisation and db based user information.
   *
   * @return boolean    true if user maintenance is implemented.
   * @throws CalFacadeException  for errors
   */
  boolean getUserMaintOK() throws CalFacadeException;

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
  BwPrincipal getCurrentPrincipal();

  /** This will not change.
   *
   * @return the principal we authenticated as
   */
  BwPrincipal getAuthPrincipal();

  /** Get the current principal to set as owner
   *
   * @return BwPrincipal object
   */
  BwPrincipal getOwner();

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
  BwPrincipal getUser(String val);

  /** Find the user with the given account name. Create if not there.
   *
   * @param val           String user id
   * @return BwUser       representing the user
   */
  BwPrincipal getUserAlways(String val);

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
   * @throws CalFacadeException on fatal error
   */
  BwPrincipal getPrincipal(String href) throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  boolean validPrincipal(String href) throws CalFacadeException;

  /** Add an entry for the user.
   *
   * @param account for user
   * @throws CalFacadeException on fatal error
   */
  void addUser(String account) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Admin users
   * ------------------------------------------------------------ */

  /** Add the user entry
   *
   * @param  val      AuthUser users entry
   * @throws CalFacadeException on fatal error
   */
  void addAuthUser(BwAuthUser val) throws CalFacadeException;

  /** Return the given authorised user. Will always return an entry (except for
   * exceptional conditions.) An unauthorised user will have a usertype of
   * noPrivileges.
   *
   * @param  pr the principal
   * @return BwAuthUser    users entry
   * @throws CalFacadeException on fatal error
   */
  BwAuthUser getAuthUser(BwPrincipal pr) throws CalFacadeException;

  /** Update the user entry
   *
   * @param  val      AuthUser users entry
   * @throws CalFacadeException on fatal error
   */
  void updateAuthUser(BwAuthUser val) throws CalFacadeException;

  /** Return a collection of all authorised users
   *
   * @return Collection      of BwAuthUser for users with any special authorisation.
   * @throws CalFacadeException on fatal error
   */
  Collection<BwAuthUser> getAllAuthUsers() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  /**
   * @return String used to prefix administrative group names to distinguish
   *         them from user group names.
   */
  String getAdminGroupsIdPrefix();

  /**
   * @param href  a principal href
   * @return group
   * @throws CalFacadeException  for errors
   */
  BwGroup getAdminGroup(String href) throws CalFacadeException;

  /**
   * @return groups
   * @throws CalFacadeException  for errors
   */
  Collection<BwGroup> getAdminGroups() throws CalFacadeException;

  /**
   * @return groups that are calsuite owners
   * @throws CalFacadeException  for errors
   */
  Collection<BwGroup> getCalsuiteAdminGroups() throws CalFacadeException;

  /**
   * Force a refetch of the groups when getAdminGroups is called
   */
  void refreshAdminGroups();

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>This does check the groups for membership of other groups so the
   * returned collection gives the groups of which the principal is
   * directly or indirectly a member.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> getAllAdminGroups(BwPrincipal val) throws CalFacadeException;

  /** Add a group
   *
   * @param  group           BwGroup group object to add
   * @exception CalFacadeException If there's a problem
   */
  void addAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** Delete a group
   *
   * @param  group           BwGroup group object to delete
   * @exception CalFacadeException If there's a problem
   */
  void removeAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** update a group. This may have no meaning in some directories.
   *
   * @param  group           BwGroup group object to update
   * @exception CalFacadeException If there's a problem
   */
  void updateAdminGroup(BwAdminGroup group) throws CalFacadeException;

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwAdminGroup        group object
   */
  BwAdminGroup findAdminGroup(String name);

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException on fatal error
   */
  void getAdminGroupMembers(BwAdminGroup group) throws CalFacadeException;

  /** Add a principal to a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   * @exception CalFacadeException   For invalid usertype values.
   */
  void addAdminGroupMember(BwAdminGroup group,
                           BwPrincipal val) throws CalFacadeException;

  /** Remove a member from a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   * @exception CalFacadeException   For invalid usertype values.
   */
  void removeAdminGroupMember(BwAdminGroup group,
                              BwPrincipal val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Groups
   * ------------------------------------------------------------ */

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwGroup        group object
   */
  BwGroup findGroup(String name);

  /**
   * @param group the group
   * @return Collection of groups of which this is a member
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> findGroupParents(BwGroup group) throws CalFacadeException;

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>Does not check the returned groups for membership of other groups.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> getGroups(BwPrincipal val) throws CalFacadeException;

  /** Return all groups to which this user has some access. Never returns null.
   *
   * @param  populate      boolean populate with members
   * @return Collection    of BwGroup
   * @throws CalFacadeException on fatal error
   */
  Collection<BwGroup> getAllGroups(boolean populate) throws CalFacadeException;

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException on fatal error
   */
  void getMembers(BwGroup group) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  /** Returns the current user preferences.
   *
   * @return BwPreferences   prefs for the current user
   * @throws CalFacadeException on fatal error
   */
  BwPreferences getPreferences() throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  void updatePreferences(BwPreferences val) throws CalFacadeException;

  /** Get the default calendar path for the current user.
   *
   * @param compName - name of component - "VEVENT" etc
   * @return String path or null for no preference
   * @throws CalFacadeException on fatal error
   */
  String getPreferredCollectionPath(String compName) throws CalFacadeException;

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
   * @param locales
   * @param locale possibly null explicitly requested locale
   * @return Collection of locales.
   * @throws CalFacadeException on fatal error
   */
  Locale getUserLocale(Collection<Locale> locales,
                       Locale locale) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  /**
   * @return home for current principal
   * @throws CalFacadeException on fatal error
   */
  BwCalendar getHome() throws CalFacadeException;

  /**
   * @param path for collection
   * @return collection object or null.
   * @throws CalFacadeException on fatal error
   */
  BwCalendar getCollection(String path) throws CalFacadeException;

  /**
   * @param path for collection
   * @return true if it exists and user has access.
   * @throws CalFacadeException on fatal error
   */
  boolean collectionExists(String path) throws CalFacadeException;

  /** Get a special calendar (e.g. Trash) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   * @throws CalFacadeException on fatal error
   */
  BwCalendar getSpecial(int calType,
                        boolean create) throws CalFacadeException;

  /** Add a calendar collection
   *
   * <p>The new collection object will be added to the db. If the indicated parent
   * is null it will be added as a root level collection.
   *
   * <p>Certain restrictions apply, mostly because of interoperability issues.
   * A collection cannot be added to another collection which already contains
   * entities, e.g. events etc.
   *
   * <p>Names cannot contain certain characters - (complete this)
   *
   * <p>Name must be unique at this level, i.e. all paths must be unique
   *
   * @param  val     BwCalendar new object
   * @param  parentPath  String path to parent.
   * @return BwCalendar object as added. Parameter val MUST be discarded
   * @throws CalFacadeException on fatal error
   */
  BwCalendar addCollection(BwCalendar val,
                           String parentPath) throws CalFacadeException;

  /** Update the given collection
   *
   * @param col the collection
   * @throws CalFacadeException on fatal error
   */
  void updateCollection(BwCalendar col) throws CalFacadeException;

  /** Delete a collection. Also remove it from the current user preferences (if any).
   *
   * @param val      BwCalendar collection
   * @param emptyIt  true to delete contents
   * @return boolean  true if it was deleted.
   *                  false if it didn't exist
   * @throws CalFacadeException for in use or marked as default calendar
   */
  boolean deleteCollection(BwCalendar val,
                           boolean emptyIt) throws CalFacadeException;

  /** Attempt to getResource collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val collection
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy true for a freebusy request
   * @return BwCalendar
   * @throws CalFacadeException on fatal error
   */
  BwCalendar resolveAlias(BwCalendar val,
                          boolean resolveSubAlias,
                          boolean freeBusy) throws CalFacadeException;

  /** Returns children of the given collection to which the current user has
   * some access. Both the col value and the returned children may be
   * cloned non-persistent objects
   *
   * @param  col          parent collection
   * @return Collection   of BwCalendar
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCalendar> getChildren(BwCalendar col)
          throws CalFacadeException;

  /** Move a calendar object from one parent to another
   *
   * @param  val         BwCalendar object
   * @param  newParent   BwCalendar potential parent
   * @throws CalFacadeException on fatal error
   */
  void moveCollection(BwCalendar val,
                      BwCalendar newParent) throws CalFacadeException;

  /**
   *
   * @param path to collection
   * @return never null - requestStatus set for not an external subscription.
   * @throws CalFacadeException on fatal error
   */
  SynchStatusResponse getSynchStatus(String path) throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCalendar> decomposeVirtualPath(String vpath) throws CalFacadeException;

  /** Return a list of calendars in which calendar objects can be
   * placed by the current user.
   *
   * <p>Caldav does not allow collections inside collections so that
   * calendar collections are the leaf nodes only.
   *
   *
   * @param includeAliases - true to include aliases - for public admin we don't
   *                    want aliases
   * @return Set   of BwCalendar
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCalendar> getAddContentCollections(boolean includeAliases)
          throws CalFacadeException;

  /** Returns the href of the root of the public calendars tree.
   *
   * @return String   root path
   */
  String getPublicCalendarsRootPath();

  /** Returns the cloned root of the tree of public calendars.
   *
   * @return BwCalendar   root
   * @throws CalFacadeException on fatal error
   */
  BwCalendar getPublicCalendars() throws CalFacadeException;

  /** Returns root of calendars owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   * 
   * Note: the returned object is NOT a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCalendar   user home.
   * @throws CalFacadeException on fatal error
   */
  BwCalendar getHome(BwPrincipal principal,
                     boolean freeBusy) throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  BwCategory getCategoryByUid(String uid) throws CalFacadeException;

  /**
   * @param href of the category
   * @return category entity or null.
   * @throws CalFacadeException on fatal error
   */
  BwCategory getCategory(String href) throws CalFacadeException;

  /**
   * @param uid of the category
   * @return category entity or null.
   * @throws CalFacadeException on fatal error
   */
  BwCategory getPersistentCategory(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCategory> getCategories() throws CalFacadeException;

  /** Return all public entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCategory> getPublicCategories() throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwCategory> getEditableCategories() throws CalFacadeException;

  /** Update a category.
   *
   * @param val   category object to be updated
   * @throws CalFacadeException on fatal error
   */
  void updateCategory(BwCategory val) throws CalFacadeException;

  /** Add the given category.
   *
   * @param val category
   * @return false if not added because already exists
   */
  boolean addCategory(BwCategory val);

  interface DeleteReffedEntityResult {
    boolean getDeleted();

    Collection<EventPropertiesReference> getReferences();
  }

  /** Delete a category
   *
   * @param val      BwEventProperty object to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException on fatal error
   */
  DeleteReffedEntityResult deleteCategory(BwCategory val) throws CalFacadeException;

  /** Return all default category uids for public suites.
   *
   * <p>Returns an empty collection for none.
   *
   * @return Collection     of uids
   * @throws CalFacadeException on fatal error
   */
  Set<String> getDefaultPublicCategoryUids() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  /** Get the contact with the given uid.
   *
   * @param uid of contact
   * @return contact object
   */
  GetEntityResponse<BwContact> getContactByUid(String uid);

  /** Get the contact with the given uid.
   *
   * @param uid of contact
   * @return contact object
   * @throws CalFacadeException on fatal error
   */
  BwContact getPersistentContact(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwContact> getContacts() throws CalFacadeException;

  /** Return all current user entities that match the filter.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param fexpr filter expression
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  GetEntitiesResponse<BwContact> getContacts(String fexpr,
                                             int from,
                                             int size)
          throws CalFacadeException;

  /** Return all public entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwContact> getPublicContacts() throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwContact> getEditableContacts() throws CalFacadeException;

  /** Find the contact.
   *
   * @param val to match
   * @return contact object
   */
  GetEntityResponse<BwContact> findContact(BwString val);

  /** Add the contact
   * @param val contact
   */
  void addContact(BwContact val);

  /** Update the contact
   * @param val contact
   * @throws CalFacadeException on fatal error
   */
  void updateContact(BwContact val) throws CalFacadeException;

  /** Delete a contact
   *
   * @param val      contact to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException on fatal error
   */
  DeleteReffedEntityResult deleteContact(BwContact val) throws CalFacadeException;

  /** Ensure a contact exists. If it already does returns the object.
   * If not creates the entity.
   *
   * @param val     contact object.
   * @param ownerHref   String principal href, null for current user
   * @return response object.
   */
  EnsureEntityExistsResult<BwContact> ensureContactExists(BwContact val,
                                                          String ownerHref);

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  /** Get the location with the given uid.
   *
   * @param uid of location
   * @return Location object
   */
  GetEntityResponse<BwLocation> getLocationByUid(String uid);

  /** Get the location with the given uid.
   *
   * @param uid of location
   * @return Location object
   * @throws CalFacadeException on fatal error
   */
  BwLocation getPersistentLocation(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwLocation> getLocations() throws CalFacadeException;

  /** Return all current user entities that match the filter.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param fexpr filter expression
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  GetEntitiesResponse<BwLocation> getLocations(String fexpr,
                                               int from,
                                               int size) throws CalFacadeException;

  /** Return all public entiities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwLocation> getPublicLocations() throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException on fatal error
   */
  Collection<BwLocation> getEditableLocations() throws CalFacadeException;

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

  /** Add the location
   * @param val a location
   * @return true if added
   */
  boolean addLocation(BwLocation val);

  /** Update the location
   * @param val a location
   * @throws CalFacadeException on error
   */
  void updateLocation(BwLocation val) throws CalFacadeException;

  /** Delete a location
   *
   * @param val      location to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException on error
   */
  DeleteReffedEntityResult deleteLocation(BwLocation val) throws CalFacadeException;

  /** Ensure a location exists. If it already does returns the object.
   * If not creates the entity.
   *
   * @param val     location object.
   * @param ownerHref   String principal href, null for current user
   * @return response object.
   */
  EnsureEntityExistsResult<BwLocation> ensureLocationExists(BwLocation val,
                                                            String ownerHref);

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  /** Claim ownership of this event
   *
   * @param ev  event
   */
  void claimEvent(BwEvent ev);

  /** Realias the event - set categories according to the set of aliases
   *
   * @param ev  event
   * @return set of categories referenced by the aliases           
   */
  RealiasResult reAlias(BwEvent ev);

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
   * @throws CalFacadeException on fatal error
   */
  EventInfo getEvent(String colPath,
                     String name,
                     String recurrenceId)
          throws CalFacadeException;

  /** Get events given the href. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   *
   * @param href   String collection path / name [ # recurrenceid ]
   * @return EventInfo or null
   * @throws CalFacadeException on fatal error
   */
  EventInfo getEvent(String href) throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  Collection<EventInfo> getEvents(String filter,
                                  BwDateTime startDate,
                                  BwDateTime endDate,
                                  boolean expanded)
          throws CalFacadeException;

  /** Add an event and ensure its location and contact exist. The calendar path
   * must be set in the event.
   *
   * <p>For public events some calendar implementors choose to allow the
   * dynamic creation of locations and contacts. For each of those, if we have
   * an id, then the object represents a preexisting database item.
   *
   * <p>Otherwise the client has provided information which will be used to
   * locate an already existing location or contact. Failing that we use the
   * information to create a new entry.
   *
   * <p>For user clients, we generally assume no contact and the location is
   * optional. However, both conditions are enforced at the application level.
   *
   * <p>On return the event object will have been updated. In addition the
   * location and contact may have been updated.
   *
   * <p>If this is a scheduling event and noInvites is set to false then
   * invitations wil be sent out to the attendees.
   *
   * <p>The event to be added may be a reference to another event. In this case
   * a number of fields should have been copied from that event. Other fields
   * will come from the target.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param rollbackOnError rollback transaction if true
   * @return UpdateResult Counts of changes.
   */
  UpdateResult addEvent(EventInfo ei,
                        boolean noInvites,
                        boolean rollbackOnError);

  /** Update an event in response to an attendee. Exactly as normal update if
   * fromAtt is null. Otherwise no status update is sent to the given attendee
   *
   * <p>  Any changeset should be embedded in the event info object.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param fromAttUri   attendee responding
   * @return UpdateResult Counts of changes.
   */
  UpdateResult updateEvent(EventInfo ei,
                           boolean noInvites,
                           String fromAttUri);

  /** Update an event in response to an attendee. Exactly as normal update if
   * fromAtt is null. Otherwise no status update is sent to the given attendee
   *
   * <p>  Any changeset should be embedded in the event info object.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param fromAttUri   attendee responding
   * @param alwaysWrite  write and reindex whatever changetable says
   * @return UpdateResult Counts of changes.
   */
  UpdateResult updateEvent(EventInfo ei,
                           boolean noInvites,
                           String fromAttUri,
                           boolean alwaysWrite);

  /** Delete an event.
   *
   * @param ei                 BwEvent object to be deleted
   * @param sendSchedulingMessage   Send a declined or cancel scheduling message
   * @return true if event deleted
   * @throws CalFacadeException on fatal error
   */
  boolean deleteEvent(EventInfo ei,
                      boolean sendSchedulingMessage) throws CalFacadeException;

  /** For an event to which we have write access we simply mark it deleted.
   *
   * <p>Otherwise we add an annotation maarking the event as deleted.
   *
   * @param event to delete
   * @throws CalFacadeException on fatal error
   */
  void markDeleted(BwEvent event) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Notifications
   * ------------------------------------------------------------ */

  /** Find a notification in the notification collection for the
   * current principal with the given name.
   *
   * @param name - of the notification
   * @return null for no notification or the notification with that name
   * @throws CalFacadeException on fatal error
   */
  NotificationType findNotification(String name) throws CalFacadeException;

  /** Return all notifications for this user
   *
   * @return all notifications for this user
   * @throws CalFacadeException on fatal error
   */
  List<NotificationType> allNotifications() throws CalFacadeException;

  /** Remove the given notification from the notification collection for the
   * current calendar user.
   *
   * @param name of notification to remove
   */
  void removeNotification(String name);

  /** Remove the given notification from the notification collection for the
   * current calendar user.
   *
   * @param val notification to remove
   * @throws CalFacadeException on fatal error
   */
  void removeNotification(NotificationType val) throws CalFacadeException;

  /** Remove all notifications for given principal
   *
   * @param principalHref principal to clean
   * @throws CalFacadeException on fatal error
   */
  void removeAllNotifications(String principalHref) throws CalFacadeException;

  void subscribe(String principalHref,
                 List<String> emails) throws CalFacadeException;

  void unsubscribe(String principalHref,
                   List<String> emails) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Resources
   * ------------------------------------------------------------ */
  /** Save a resource at the given collection path. The collection MUST exist.
   *
   * @param  val       resource with attached content
   * @throws CalFacadeException for errors including duplicate name
   */
  void saveResource(BwResource val) throws CalFacadeException;

  /** Get a resource given the path - does not getResource content
   *
   * @param  path     String path to resource
   * @return BwResource null for unknown resource
   * @throws CalFacadeException on error
   */
  BwResource getResource(String path) throws CalFacadeException;


  /** Retrieve resource content given the resource. It will be set in the resource
   * object
   *
   * @param  val BwResource
   * @throws CalFacadeException on error
   * @return false if content missing
   */
  boolean getResourceContent(BwResource val) throws CalFacadeException;

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @return List     of BwResource
   * @throws CalFacadeException on error
   */
  List<BwResource> getAllResources(String path) throws CalFacadeException;

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @param count this many
   * @return List     of BwResource
   * @throws CalFacadeException on fatal error
   */
  List<BwResource> getResources(String path,
                                int count) throws CalFacadeException;

  /** Update a resource.
   *
   * @param  val          resource
   * @param updateContent if true we also update the content
   * @throws CalFacadeException for errors including duplicate name
   */
  void updateResource(BwResource val,
                      boolean updateContent) throws CalFacadeException;

  /**
   * 
   * @param val the resource object
   * @param content to embed
   * @throws CalFacadeException for errors
   */
  void setResourceValue(BwResource val,
                        String content) throws CalFacadeException;

  /**
   *
   * @param val the resource object
   * @param content to embed
   * @throws CalFacadeException for errors
   */
  void setResourceValue(BwResource val,
                        byte[] content) throws CalFacadeException;
  
  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  /** Get the free busy for the given principal as a list of busy periods.
   *
   * @param fbset  Collections for which to provide free-busy. Null for the
   *               for default collection (as specified by user).
   *               Used for local access to a given calendar via e.g. caldav
   * @param who    If cal is null getResource the info for this user, otherwise
   *               this is used as the free/busy result owner
   * @param start  of freebusy period
   * @param end of freebusy period
   * @param org - needed to make the result compliant
   * @param uid - uid of requesting component or null for no request
   * @param exceptUid if non-null omit this uid from the freebusy calculation
   * @return BwEvent
   * @throws CalFacadeException on fatal error
   */
  BwEvent getFreeBusy(Collection<BwCalendar> fbset,
                      BwPrincipal who,
                      BwDateTime start,
                      BwDateTime end,
                      BwOrganizer org,
                      String uid,
                      String exceptUid)
          throws CalFacadeException;

  /** Get aggregated free busy for a ScheduleResult.
   *
   * @param sr ScheduleResult
   * @param start of period
   * @param end of period
   * @param granularity of result - eg 1 hour
   * @return FbResponses
   * @throws CalFacadeException on fatal error
   */
  SchedulingI.FbResponses aggregateFreeBusy(ScheduleResult sr,
                                            BwDateTime start,
                                            BwDateTime end,
                                            BwDuration granularity) throws CalFacadeException;

  /** Schedule a meeting or publish an event. The event object must have the organizer
   * and attendees and possibly recipients set according to itip + caldav.
   *
   * <p>The event will be added to the users outbox which will trigger the send
   * of requests to other users inboxes. For users within this system the
   * request will be immediately addded to the recipients inbox. For external
   * users they are sent via ischedule or mail.
   *
   * @param ei         EventInfo object containing with method=REQUEST, CANCEL,
   *                              ADD, DECLINECOUNTER or PUBLISH
   * @param recipient - non null to send to this recipient only (for REFRESH)
   * @param fromAttUri
   * @param iSchedule  true if it's an iSchedule request.
   * @return ScheduleResult
   * @throws CalFacadeException on fatal error
   */
  ScheduleResult schedule(EventInfo ei,
                          String recipient,
                          String fromAttUri,
                          boolean iSchedule) throws CalFacadeException;

  /** Return the users copy of the active meeting with the
   * same uid as that given.
   *
   * @param ev the event to search for
   * @return possibly null meeting
   * @throws CalFacadeException on fatal error
   */
  EventInfo getStoredMeeting(BwEvent ev) throws CalFacadeException;

  /** Attendee wants a refresh
   *
   * @param ei event which is probably in a calendar.
   * @param comment - optional comment
   * @return   ScheduleResult
   * @throws CalFacadeException on fatal error
   */
  ScheduleResult requestRefresh(EventInfo ei,
                                String comment) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Sharing
   * ------------------------------------------------------------ */
  /**
   * @param principalHref share as this user.
   * @param col MUST be a sharable collection
   * @param share the request
   * @return list of ok and !ok sharees
   * @throws CalFacadeException on fatal error
   */
  ShareResultType share(String principalHref,
                        BwCalendar col,
                        ShareType share) throws CalFacadeException;


  /** Publish the collection - that is make it available for subscriptions.
   *
   * @param col collection object
   * @throws CalFacadeException on fatal error
   */
  void publish(BwCalendar col) throws CalFacadeException;

  /** Unpublish the collection - that is make it unavailable for subscriptions
   * and remove any existing subscriptions.
   *
   * @param col collection object
   * @throws CalFacadeException on fatal error
   */
  void unpublish(BwCalendar col) throws CalFacadeException;

  /**
   * @param reply the request
   * @return a ReplyResult object.
   * @throws CalFacadeException on fatal error
   */
  SharingI.ReplyResult sharingReply(InviteReplyType reply) throws CalFacadeException;

  /** Subscribe to the collection - must be a published collection.
   *
   * @param colPath  collection path
   * @param subscribedName name for new alias
   * @return path of new alias and flag
   * @throws CalFacadeException on fatal error
   */
  SharingI.SubscribeResult subscribe(String colPath,
                                     String subscribedName) throws CalFacadeException;

  /** Subscribe to an external url.
   *
   * @param extUrl the url
   * @param subscribedName name for new alias
   * @param refresh - refresh rate in minutes <= 0 for default
   * @param remoteId - may be null
   * @param remotePw  - may be null
   * @return path of new alias and flag
   * @throws CalFacadeException on fatal error
   */
  SharingI.SubscribeResult subscribeExternal(String extUrl,
                                             String subscribedName,
                                             int refresh,
                                             String remoteId,
                                             String remotePw) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Synch
   * ------------------------------------------------------------ */

  /** Returns the synch service information.
   *
   * @return full synch info
   * @throws CalFacadeException on fatal error
   */
  BwSynchInfo getSynchInfo() throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  BwView getView(String val) throws CalFacadeException;

  /** Add a view.
   *
   * @param  val           BwView to add
   * @param  makeDefault   boolean true for make this the default.
   * @return boolean false view not added, true - added.
   * @throws CalFacadeException on fatal error
   */
  boolean addView(BwView val,
                  boolean makeDefault) throws CalFacadeException;

  /** Return the collection of views - named collections of collections
   *
   * @return collection of views
   * @throws CalFacadeException on fatal error
   */
  Collection<BwView> getAllViews() throws CalFacadeException;

  /** Add a collection path to the named view.
   *
   * @param  name    String view name - null means default
   * @param  path     collection path to add
   * @return boolean false view not found, true - collection path added.
   * @throws CalFacadeException on fatal error
   */
  boolean addViewCollection(String name,
                            String path) throws CalFacadeException;

  /** Remove a collection path from the named view.
   *
   * @param  name    String view name - null means default
   * @param  path    collection path to remove
   * @return boolean false view not found, true - collection path removed.
   * @throws CalFacadeException on fatal error
   */
  boolean removeViewCollection(String name,
                               String path) throws CalFacadeException;

  /** Remove the view for the owner of the object.
   *
   * @param  val     BwView
   * @return boolean false - view not found.
   * @throws CalFacadeException on fatal error
   */
  boolean removeView(BwView val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  /** Called if anything is changed which affects the state of the client, e.g
   * switching display flags, deleting collections.
   */
  void flushState();

  /* ------------------------------------------------------------
   *                     State of current admin group
   * ------------------------------------------------------------ */

  /**
   * @param val true if group is set
   * @throws CalFacadeException on fatal error
   */
  void setGroupSet(boolean val) throws CalFacadeException;

  /**
   * @return true for group set
   */
  boolean getGroupSet();

  /**
   * @param val true if choosing group
   * @throws CalFacadeException on fatal error
   */
  void setChoosingGroup(boolean val) throws CalFacadeException;

  /**
   * @return true for choosing group
   */
  boolean getChoosingGroup();

  /**
   * @param val true if only 1 group available
   * @throws CalFacadeException on fatal error
   */
  void setOneGroup(boolean val) throws CalFacadeException;

  /**
   * @return true if there is only one group
   */
  boolean getOneGroup();

  /** Current admin group name, or null for none
   *
   * @param val      BwAdminGroup representing users group or null
   * @throws CalFacadeException on fatal error
   */
  void setAdminGroupName(String val) throws CalFacadeException;

  /**
   * @return String admin group name
   */
  String getAdminGroupName();

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  /** Move the contents of one collection to another
   *
   * @param cal where from
   * @param newCal where to
   * @throws CalFacadeException on fatal error
   */
  void moveContents(BwCalendar cal,
                    BwCalendar newCal) throws CalFacadeException;

  /** Update the system after changes to timezones. This is a lengthy process
   * so the method allows the caller to specify how many updates are to take place
   * before returning.
   *
   * <p>To restart the update, call the method again, giving it the result from
   * the last call as a parameter.
   *
   * <p>If called again after all events have been checked the process will be
   * redone using timestamps to limit the check to events added or updated since
   * the first check. Keep calling until the number of updated events is zero.
   *
   * @param limit   -1 for no limit
   * @param checkOnly  don't update if true.
   * @param info    null on first call, returned object from previous calls.
   * @return UpdateFromTimeZonesInfo staus of the update
   * @throws CalFacadeException on fatal error
   */
  UpdateFromTimeZonesInfo updateFromTimeZones(String colHref,
                                              int limit,
                                              boolean checkOnly,
                                              UpdateFromTimeZonesInfo info)
          throws CalFacadeException;

  /**
   * @param val mail message
   * @throws CalFacadeException on fatal error
   */
  void postMessage(Message val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     SearchNextAction
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
   * @throws CalFacadeException on fatal error
   */
  SearchResult search(SearchParams params) throws CalFacadeException;

  /**
   * @return SearchParams
   */
  SearchParams getSearchParams();

  /** Called to retrieve results after a search of the index. Updates
   * the current search result.
   *
   * @param pos - specify movement in result set
   * @return  list of SearchResultEntry
   * @throws CalFacadeException on fatal error
   */
  List<SearchResultEntry> getSearchResult(Position pos) throws CalFacadeException;

  /** Called to retrieve results after a search of the index. Updates
   * the current search result.
   *
   * @param start in result set
   * @param num of entries
   * @return  list of SearchResultEntry
   * @throws CalFacadeException on fatal error
   */
  List<SearchResultEntry> getSearchResult(int start,
                                          int num) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Access
   * ------------------------------------------------------------ */

  /** Change the access to the given calendar entity.
   *
   * @param ent      BwShareableDbentity
   * @param aces     Collection of ace
   * @param replaceAll true to replace the entire access list.
   * @throws CalFacadeException on fatal error
   */
  void changeAccess(BwShareableDbentity<?> ent,
                    Collection<Ace> aces,
                    boolean replaceAll) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */

  /** Set the calendar suite we are running as. Must be running as an
   * unauthenticated user.
   *
   * @throws CalFacadeException on fatal error
   */
  void setCalSuite(BwCalSuite cs) throws CalFacadeException;

  /** Get the current calendar suite.
   *
   * @return BwCalSuiteWrapper null for unknown calendar suite
   */
  BwCalSuiteWrapper getCalSuite();

  /** Get a calendar suite given the 'owning' group
   *
   * @param  group     BwAdminGroup
   * @return BwCalSuiteWrapper null for unknown calendar suite
   * @throws CalFacadeException on fatal error
   */
  BwCalSuiteWrapper getCalSuite(BwAdminGroup group)
          throws CalFacadeException;

  /** Return the collection of cal suites filled in with sub-context information
   *
   * @return Calendar suites
   */
  Collection<BwCalSuite> getContextCalSuites() throws CalFacadeException;

  /** Get a calendar suite given the name
   *
   * @param  name     String name of calendar suite
   * @return BwCalSuiteWrapper null for unknown calendar suite
   * @throws CalFacadeException on fatal error
   */
  BwCalSuiteWrapper getCalSuite(String name) throws CalFacadeException;

  /** Create a new calendar suite
   *
   * @param name ofsuite
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param submissionsPath where submitted events go
   * @return BwCalSuiteWrapper for new object
   * @throws CalFacadeException on fatal error
   */
  BwCalSuiteWrapper addCalSuite(String name,
                                String adminGroupName,
                                String rootCollectionPath,
                                String submissionsPath) throws CalFacadeException;

  /** Update a calendar suite. Any of the parameters to be changed may be null
   * or the current value to indicate no change.
   *
   * @param cs     BwCalSuiteWrapper object
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath - root for suite
   * @param submissionsPath where submitted events go
   * @throws CalFacadeException on fatal error
   */
  void updateCalSuite(BwCalSuiteWrapper cs,
                      String adminGroupName,
                      String rootCollectionPath,
                      String submissionsPath) throws CalFacadeException;

  /** Delete a calendar suite object
   *
   * @param  val     BwCalSuiteWrapper object
   * @throws CalFacadeException on fatal error
   */
  void deleteCalSuite(BwCalSuiteWrapper val) throws CalFacadeException;

  /** Is the current calendar suite the 'owner'.
   *
   * @param ent the entity to test
   * @return true if so
   */
  boolean isCalSuiteEntity(BwShareableDbentity<?> ent);

  /* ------------------------------------------------------------
   *                   Calendar Suite Resources
   * ------------------------------------------------------------ */

  /** Get a list of resources. The content is not fetched.
   *
   * @param suite - calendar suite
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return list
   * @throws CalFacadeException on fatal error
   */
  List<BwResource> getCSResources(BwCalSuite suite,
                                  String rc) throws CalFacadeException;

  /** Get named resource. The content is fetched.
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return resource or null
   * @throws CalFacadeException on fatal error
   */
  BwResource getCSResource(BwCalSuite suite,
                           String name,
                           String rc) throws CalFacadeException;

  /** Add a resource. The supplied object has all fields set except for the
   * path. This will be determined by the cl parameter and set in the object.
   *
   * <p>The parent collection will be created if necessary.
   *
   * @param suite - calendar suite
   * @param res resource to add
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException on fatal error
   */
  void addCSResource(BwCalSuite suite,
                     BwResource res,
                     String rc) throws CalFacadeException;

  /** Delete named resource
   *
   * @param suite - calendar suite
   * @param name of resource
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException on fatal error
   */
  void deleteCSResource(BwCalSuite suite,
                        String name,
                        String rc) throws CalFacadeException;

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
   * @throws CalFacadeException on fatal error
   */
  Collection<BwFilterDef> getAllFilters() throws CalFacadeException;

  /** Validate a filter definition.
   *
   * @param  val       String xml filter definition
   * @throws CalFacadeException on fatal error
   */
  void validateFilter(String val) throws CalFacadeException;

  /** Validate and persist a new filter definition
   *
   * @param  val       filter definition
   * @throws CalFacadeException for errrors including duplicate name
   */
  void saveFilter(BwFilterDef val) throws CalFacadeException;

  /** Delete a filter given the name
   *
   * @param  name     String name of filter
   * @throws CalFacadeException on fatal error
   */
  void deleteFilter(String name) throws CalFacadeException;
}
