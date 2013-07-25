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

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.BwAuthUser;
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
import org.bedework.calfacade.BwPreferences;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.SchedulingI;
import org.bedework.calsvci.SharingI;
import org.bedework.webcommon.search.SearchResultEntry;

import edu.rpi.cct.misc.indexing.SearchLimits;
import edu.rpi.cmt.access.Ace;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * This interface defines the interactions with the back end system
 * for a client.
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public interface Client extends Serializable {
  /** Call on the way in once we have a client object.
   *
   * @param conversationType
   * @throws CalFacadeException
   */
  void requestIn(int conversationType) throws CalFacadeException;

  /** Call on the way out.
   *
   * @param conversationType
   * @param actionType
   * @param reqTimeMillis time for request.
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  void close() throws CalFacadeException;

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
   * @throws CalFacadeException
   */
  void flushAll() throws CalFacadeException;

  /** End a (possibly long-running) transaction. In the web environment
   * this should in some way check version numbers to detect concurrent updates
   * and fail with an exception.
   *
   * @throws CalFacadeException
   */
  void endTransaction() throws CalFacadeException;

  /**
   *
   * @return true if we are doing public admin.
   */
  boolean getPublicAdmin();

  /** Get the (possibly cached) system pars using name supplied at init
   *
   * @return BwSystem object
   * @throws CalFacadeException if not admin
   */
  BwSystem getSyspars() throws CalFacadeException;

  /** Update the system pars
   *
   * @param val BwSystem object
   * @throws CalFacadeException if not admin
   */
  void updateSyspars(BwSystem val) throws CalFacadeException;

  /** Return properties about the system.
   *
   * @return SystemProperties object - never null.
   * @throws CalFacadeException
   */
  SystemProperties getSystemProperties() throws CalFacadeException;

  /**
   * If possible roll back the changes.
   */
  void rollback();

  /**
   * @return System limit or user overrride - bytes.
   * @throws CalFacadeException
   */
  long getUserMaxEntitySize() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Directories
   * ------------------------------------------------------------ */

  /** Get application visible directory information.
   *
   * @return DirectoryInfo
   * @throws CalFacadeException
   */
  DirectoryInfo getDirectoryInfo() throws CalFacadeException;

  /**
   *
   * @param cua   calendar user address
   * @return principal corresponding or null for unknown
   * @throws CalFacadeException
   */
  BwPrincipal calAddrToPrincipal(String cua) throws CalFacadeException;

  /**
   *
   * @param user          String user id
   * @return calendar address or null for unknown user
   * @throws CalFacadeException
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
   * @throws CalFacadeException  for errors
   */
  String uriToCaladdr(String val) throws CalFacadeException;

  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   * @throws CalFacadeException  for errors
   */
  boolean getAdminGroupMaintOK() throws CalFacadeException;

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
   * @return public entity owner
   * @throws CalFacadeException
   */
  BwPrincipal getPublicUser() throws CalFacadeException;

  /** Does the value appear to represent a valid principal?
   *
   * @param val
   * @return true if it's a (possible) principal
   * @throws CalFacadeException
   */
  boolean isPrincipal(String val) throws CalFacadeException;

  /**
   *
   * @return the current principal we are acting for
   * @throws CalFacadeException
   */
  BwPrincipal getCurrentPrincipal() throws CalFacadeException;

  /** Get the current principal to set as owner
   *
   * @return BwPrincipal object
   * @throws CalFacadeException
   */
  BwPrincipal getOwner() throws CalFacadeException;

  /**
   *
   * @return href for current principal
   * @throws CalFacadeException
   */
  String getCurrentPrincipalHref() throws CalFacadeException;

  /**
   *
   * @return current principal account
   * @throws CalFacadeException
   */
  String getCurrentAccount() throws CalFacadeException;

  /** Find the user with the given account name.
   *
   * @param val           String user id
   * @return User principal or null if not there
   * @throws CalFacadeException
   */
  BwPrincipal getUser(final String val) throws CalFacadeException;

  /** Find the user with the given account name. Create if not there.
   *
   * @param val           String user id
   * @return BwUser       representing the user
   * @throws CalFacadeException
   */
  BwPrincipal getUserAlways(String val) throws CalFacadeException;

  /**
   *
   * @return current calendar address
   * @throws CalFacadeException
   */
  String getCurrentCalendarAddress() throws CalFacadeException;

  /**
   * @param id
   * @param whoType - from WhoDefs
   * @return String principal uri
   * @throws CalFacadeException
   */
  String makePrincipalUri(String id,
                          int whoType) throws CalFacadeException;

  /** Return principal for the given href.
   *
   * @param href
   * @return Principal
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  boolean validPrincipal(String href) throws CalFacadeException;

  /** Add an entry for the user.
   *
   * @param account
   * @throws CalFacadeException
   */
  void addUser(String account) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Admin users
   * ------------------------------------------------------------ */

  /** Return the given authorised user. Will always return an entry (except for
   * exceptional conditions.) An unauthorised user will have a usertype of
   * noPrivileges.
   *
   * @param  userid        String user id
   * @return BwAuthUser    users entry
   * @throws CalFacadeException
   */
  BwAuthUser getAuthUser(String userid) throws CalFacadeException;

  /** Update the user entry
   *
   * @param  val      AuthUserVO users entry
   * @throws CalFacadeException
   */
  void updateAuthUser(BwAuthUser val) throws CalFacadeException;

  /** Return a collection of all authorised users
   *
   * @return Collection      of BwAuthUser for users with any special authorisation.
   * @throws CalFacadeException
   */
  Collection<BwAuthUser> getAllAuthUsers() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Admin Groups
   * ------------------------------------------------------------ */

  /**
   * @return String used to prefix administrative group names to distinguish
   *         them from user group names.
   * @throws CalFacadeException  for errors
   */
  String getAdminGroupsIdPrefix() throws CalFacadeException;

  /**
   * @param getMembers
   * @return groups
   * @throws CalFacadeException  for errors
   */
  Collection<BwGroup> getAdminGroups(boolean getMembers) throws CalFacadeException;

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>This does check the groups for membership of other groups so the
   * returned collection gives the groups of which the principal is
   * directly or indirectly a member.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   * @throws CalFacadeException
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
   * @exception CalFacadeException If there's a problem
   */
  BwAdminGroup findAdminGroup(String name) throws CalFacadeException;

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException
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
   * @exception CalFacadeException If there's a problem
   */
  BwGroup findGroup(String name) throws CalFacadeException;

  /**
   * @param group the group
   * @return Collection of groups of which this is a member
   * @throws CalFacadeException
   */
  Collection<BwGroup> findGroupParents(BwGroup group) throws CalFacadeException;

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>Does not check the returned groups for membership of other groups.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   * @throws CalFacadeException
   */
  Collection<BwGroup> getGroups(BwPrincipal val) throws CalFacadeException;

  /** Return all groups to which this user has some access. Never returns null.
   *
   * @param  populate      boolean populate with members
   * @return Collection    of BwGroup
   * @throws CalFacadeException
   */
  Collection<BwGroup> getAllGroups(boolean populate) throws CalFacadeException;

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   * @throws CalFacadeException
   */
  void getMembers(BwGroup group) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  /** Returns the current user preferences.
   *
   * @return BwPreferences   prefs for the current user
   * @throws CalFacadeException
   */
  BwPreferences getPreferences() throws CalFacadeException;

  /** Returns the given user's preferences. Only valid for superuser
   *
   * @param user - account
   * @return BwPreferences   prefs for the user - null for no access
   * @throws CalFacadeException
   */
  BwPreferences getPreferences(String user) throws CalFacadeException;

  /** Update the user preferences.
   *
   * @param  val     BwPreferences prefs for a user
   * @throws CalFacadeException
   */
  void updatePreferences(BwPreferences val) throws CalFacadeException;

  /** Get the default calendar path for the current user.
   *
   * @return String path or null for no preference
   * @throws CalFacadeException
   */
  public String getPreferredCollectionPath() throws CalFacadeException;

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
   * <p>The locale parameter is a possibly null explicily requested locale.
   *
   * <p>Matching locales is first attempted using Locale equality. If that fails
   * we try to match languages on the assumption that somebody requesting fr_FR
   * is likely to be happier with fr_CA than en_US.
   *
   * @param locales
   * @param locale
   * @return Collection of locales.
   * @throws CalFacadeException
   */
  public Locale getUserLocale(Collection<Locale> locales,
                              Locale locale) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  /**
   * @return home for current principal
   * @throws CalFacadeException
   */
  BwCalendar getHome() throws CalFacadeException;

  /**
   * @param path for collection
   * @return collection object or null.
   * @throws CalFacadeException
   */
  BwCalendar getCollection(String path) throws CalFacadeException;

  /**
   * @param path for collection
   * @return true if it exists and user has access.
   * @throws CalFacadeException
   */
  boolean collectionExists(String path) throws CalFacadeException;

  /** Get a special calendar (e.g. Trash) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  BwCalendar addCollection(BwCalendar val,
                           String parentPath) throws CalFacadeException;

  /** Update the given collection
   *
   * @param col the collection
   * @throws CalFacadeException
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
   * @param freeBusy
   * @return BwCalendar
   * @throws CalFacadeException
   */
  BwCalendar resolveAlias(BwCalendar val,
                          boolean resolveSubAlias,
                          boolean freeBusy) throws CalFacadeException;

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  col          parent collection
   * @return Collection   of BwCalendar
   * @throws CalFacadeException
   */
  Collection<BwCalendar> getChildren(BwCalendar col)
          throws CalFacadeException;

  /** Move a calendar object from one parent to another
   *
   * @param  val         BwCalendar object
   * @param  newParent   BwCalendar potential parent
   * @throws CalFacadeException
   */
  void moveCollection(BwCalendar val,
                      BwCalendar newParent) throws CalFacadeException;

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
   * @param vpath
   * @return collection of collection objects - null for bad vpath
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  Collection<BwCalendar> getAddContentCollections(boolean includeAliases)
          throws CalFacadeException;

  /** Returns the tree of public calendars. The returned objects are those to
   * which the current user has access.
   *
   * @return BwCalendar   root with all children attached
   * @throws CalFacadeException
   */
  BwCalendar getPublicCalendars() throws CalFacadeException;

  /** Returns root of calendars owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *
   * @param  principal
   * @param freeBusy      true if this is for freebusy access
   * @return BwCalendar   user home.
   * @throws CalFacadeException
   */
  BwCalendar getHome(BwPrincipal principal,
                     boolean freeBusy) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  /**
   * @param name unique (for current user) value for the category -
   *             possible language set.
   * @return category entity or null.
   * @throws CalFacadeException
   */
  BwCategory getCategoryByName(BwString name) throws CalFacadeException;

  /**
   * @param uid of the category
   * @return category entity or null.
   * @throws CalFacadeException
   */
  BwCategory getCategory(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwCategory> getCategories() throws CalFacadeException;

  /** Return all entities for the given owner and to which the current
   * user has access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param ownerHref   String principal href, null for current user
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwCategory> getCategories(String ownerHref) throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwCategory> getEditableCategories() throws CalFacadeException;

  /** Check for existence
   *
   * @param cat the category
   * @return true if exists
   * @throws CalFacadeException
   */
  boolean categoryExists(BwCategory cat) throws CalFacadeException;

  /** Update a category.
   *
   * @param val   category object to be updated
   * @throws CalFacadeException
   */
  void updateCategory(BwCategory val) throws CalFacadeException;

  /** Add the given category.
   *
   * @param val
   * @throws CalFacadeException
   */
  void addCategory(BwCategory val) throws CalFacadeException;

  public interface DeleteReffedEntityResult {
    boolean getDeleted();

    Collection<EventPropertiesReference> getReferences();
  }

  /** Delete a category
   *
   * @param val      BwEventProperty object to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException
   */
  DeleteReffedEntityResult deleteCategory(BwCategory val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  /** Get the contact with the given uid.
   *
   * @param uid
   * @return contact object
   * @throws CalFacadeException
   */
  BwContact getContact(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwContact> getContacts() throws CalFacadeException;

  /** Return all entities for the given owner and to which the current
   * user has access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param ownerHref   String principal href, null for current user
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwContact> getContacts(String ownerHref) throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwContact> getEditableContacts() throws CalFacadeException;

  /** Find the contact.
   *
   * @param val
   * @return contact object
   * @throws CalFacadeException
   */
  BwContact findContact(BwString val) throws CalFacadeException;

  /** Add the contact
   * @param val
   * @throws CalFacadeException
   */
  void addContact(BwContact val) throws CalFacadeException;

  /** Update the contact
   * @param val
   * @throws CalFacadeException
   */
  void updateContact(BwContact val) throws CalFacadeException;

  /** Delete a contact
   *
   * @param val      contact to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException
   */
  DeleteReffedEntityResult deleteContact(BwContact val) throws CalFacadeException;

  /** Returned to show if an entity was added. entity is set to retrieved entity
   *
   * @param <T>
   */
  public interface CheckEntityResult<T> {
    /**
     *
     * @return true if was added
     */
    boolean getAdded();

    /**
     * @return the entity
     */
    T getEntity();
  }

  /** Ensure a contact exists. If it already does returns the object.
   * If not creates the entity.
   *
   * @param val     contact object.
   * @param ownerHref   String principal href, null for current user
   * @return contact object.
   * @throws CalFacadeException
   */
  CheckEntityResult<BwContact> ensureContactExists(final BwContact val,
                                                   final String ownerHref)
          throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  /** Get the location with the given uid.
   *
   * @param uid
   * @return Location object
   * @throws CalFacadeException
   */
  BwLocation getLocation(String uid) throws CalFacadeException;

  /** Return all current user entities.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwLocation> getLocations() throws CalFacadeException;

  /** Return all entities for the given owner and to which the current
   * user has access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @param ownerHref   String principal href, null for current user
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwLocation> getLocations(String ownerHref) throws CalFacadeException;

  /** Return all entities to which the current
   * user has edit access.
   *
   * <p>Returns an empty collection for none.
   *
   * <p>The returned objects may not be persistent objects but the result of a
   * report query.
   *
   * @return Collection     of objects
   * @throws CalFacadeException
   */
  Collection<BwLocation> getEditableLocations() throws CalFacadeException;

  /** Find the location given the address.
   *
   * @param address
   * @return Location object
   * @throws CalFacadeException
   */
  BwLocation findLocation(BwString address) throws CalFacadeException;

  /** Add the location
   * @param val
   * @return true if added
   * @throws CalFacadeException
   */
  boolean addLocation(BwLocation val) throws CalFacadeException;

  /** Update the location
   * @param val
   * @throws CalFacadeException
   */
  void updateLocation(BwLocation val) throws CalFacadeException;

  /** Delete a location
   *
   * @param val      location to be deleted
   * @return result indicating what happened - null for not there
   * @throws CalFacadeException
   */
  DeleteReffedEntityResult deleteLocation(BwLocation val) throws CalFacadeException;

  /** Ensure a location exists. If it already does returns the object.
   * If not creates the entity.
   *
   * @param val     location object.
   * @param ownerHref   String principal href, null for current user
   * @return location object.
   * @throws CalFacadeException
   */
  CheckEntityResult<BwLocation> ensureLocationExists(final BwLocation val,
                                                     final String ownerHref)
          throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  /** Claim ownership of this event
   *
   * @param ev  event
   * @throws CalFacadeException
   */
  void claimEvent(final BwEvent ev) throws CalFacadeException;

  /** Return a Collection of EventInfo objects. More than one for a recurring
   * event with overrides.
   *
   * @param path       collection to search
   * @param guid
   * @param rid
   * @param recurRetrieval How recurring event is returned.
   * @param scheduling
   * @return Collection of EventInfo
   * @throws CalFacadeException
   */
  Collection<EventInfo> getEvent(String path,
                                 String guid,
                                 String rid,
                                 RecurringRetrievalMode recurRetrieval,
                                 boolean scheduling)
          throws CalFacadeException;

  /** Get events given the calendar and String name. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   *
   * @param  colPath   String collection path
   * @param name       String possible name
   * @param recurRetrieval
   * @return EventInfo or null
   * @throws CalFacadeException
   */
  EventInfo getEvent(String colPath,
                     String name,
                     RecurringRetrievalMode recurRetrieval)
          throws CalFacadeException;

  public interface GetEventsResult {
    /**
     * @return true if result is paged
     */
    boolean getPaged();

    /**
     * @return total number in result set
     */
    long getCount();

    /**
     * @return the retrieved events
     */
    Collection<EventInfo> getEvents();
  }
  /** Get events that lie in the given range and are constrained by the
   * given filter. The current view filter will be ANDed
   *
   * @param start of range
   * @param end of range
   * @param filter the filter
   * @param forExport true if we are going to export the events in
   *                  which case we want overrides. Otherwise we getResource
   *                  an expanded form in which each instance of
   *                  recurring events is represented as a separate event.
   * @param exact true if we must have a true representation of the
   *              stored events. Otherwise we may fetch a cached set
   *              which doesn't necessarily have all the events due to
   *              commit delays.
   * @param pos entity number - start at 0
   * @param pageSize number of entities per page, -1 for all - may be ignored
   * @return events and information
   * @throws CalFacadeException
   */
  GetEventsResult getEvents(BwDateTime start,
                            BwDateTime end,
                            FilterBase filter,
                            boolean forExport,
                            boolean exact,
                            final int pos,
                            final int pageSize)
          throws CalFacadeException;

  /** Return the events for the current user within the given date and time
   * range. If retrieveList is supplied only those fields (and a few required
   * fields) will be returned.
   *
   * @param cal          BwCalendar object - non-null means limit to given calendar
   *                     null is limit to current user
   * @param filter       BwFilter object restricting search or null.
   * @param startDate    BwDateTime start - may be null
   * @param endDate      BwDateTime end - may be null.
   * @param retrieveList List of properties to retrieve or null for a full event.
   * @param recurRetrieval How recurring event is returned.
   * @return Collection  populated event value objects
   * @throws CalFacadeException
   */
  Collection<EventInfo> getEvents(BwCalendar cal,
                                  FilterBase filter,
                                  BwDateTime startDate,
                                  BwDateTime endDate,
                                  List<String> retrieveList,
                                  RecurringRetrievalMode recurRetrieval)
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
   * @param scheduling   True if this is to be added to an inbox - affects required
   *                     access.
   * @param rollbackOnError
   * @return UpdateResult Counts of changes.
   * @throws CalFacadeException
   */
  EventInfo.UpdateResult addEvent(EventInfo ei,
                                  boolean noInvites,
                                  boolean scheduling,
                                  boolean rollbackOnError) throws CalFacadeException;

  /** Update an event in response to an attendee. Exactly as normal update if
   * fromAtt is null. Otherwise no status update is sent to the given attendee
   *
   * <p>  Any changeset should be embedded in the event info object.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param fromAttUri   attendee responding
   * @return UpdateResult Counts of changes.
   * @throws CalFacadeException
   */
  EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                     final boolean noInvites,
                                     String fromAttUri) throws CalFacadeException;

  /** Delete an event.
   *
   * @param ei                 BwEvent object to be deleted
   * @param sendSchedulingMessage   Send a declined or cancel scheduling message
   * @return true if event deleted
   * @throws CalFacadeException
   */
  boolean deleteEvent(EventInfo ei,
                      boolean sendSchedulingMessage) throws CalFacadeException;

  /** For an event to which we have write access we simply mark it deleted.
   *
   * <p>Otherwise we add an annotation maarking the event as deleted.
   *
   * @param event to delete
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  NotificationType findNotification(String name) throws CalFacadeException;

  /** Remove the given notification from the notification collection for the
   * current calendar user.
   *
   * @param val notification to remove
   * @throws CalFacadeException
   */
  void removeNotification(NotificationType val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Resources
   * ------------------------------------------------------------ */
  /** Save a resource at the given collection path. The collection MUST exist.
   *
   * @param  path      String path to containing collection
   * @param  val       resource with attached content
   * @throws CalFacadeException for errors including duplicate name
   */
  void saveResource(String path,
                    BwResource val) throws CalFacadeException;

  /** Get a resource given the path - does not getResource content
   *
   * @param  path     String path to resource
   * @return BwResource null for unknown resource
   * @throws CalFacadeException
   */
  BwResource getResource(String path) throws CalFacadeException;


  /** Retrieve resource content given the resource. It will be set in the resource
   * object
   *
   * @param  val BwResource
   * @throws CalFacadeException
   */
  void getResourceContent(BwResource val) throws CalFacadeException;

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @return List     of BwResource
   * @throws CalFacadeException
   */
  List<BwResource> getAllResources(String path) throws CalFacadeException;

  /** Update a resource.
   *
   * @param  val          resource
   * @param updateContent if true we also update the content
   * @throws CalFacadeException for errors including duplicate name
   */
  void updateResource(BwResource val,
                      boolean updateContent) throws CalFacadeException;

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
   * @throws CalFacadeException
   */
  BwEvent getFreeBusy(final Collection<BwCalendar> fbset,
                      final BwPrincipal who,
                      final BwDateTime start,
                      final BwDateTime end,
                      final BwOrganizer org,
                      final String uid,
                      final String exceptUid)
          throws CalFacadeException;

  /** Get aggregated free busy for a ScheduleResult.
   *
   * @param sr ScheduleResult
   * @param start
   * @param end
   * @param granularity
   * @return FbResponses
   * @throws CalFacadeException
   */
  SchedulingI.FbResponses aggregateFreeBusy(ScheduleResult sr,
                                            BwDateTime start, BwDateTime end,
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
   * @param method - the scheduling method
   * @param recipient - non null to send to this recipient only (for REFRESH)
   * @param fromAttUri
   * @param iSchedule  true if it's an iSchedule request.
   * @return ScheduleResult
   * @throws CalFacadeException
   */
  public ScheduleResult schedule(EventInfo ei,
                                 int method,
                                 String recipient,
                                 String fromAttUri,
                                 boolean iSchedule) throws CalFacadeException;

  /** Return the users copy of the active meeting with the
   * same uid as that given.
   *
   * @param ev the event to search for
   * @return possibly null meeting
   * @throws CalFacadeException
   */
  EventInfo getStoredMeeting(BwEvent ev) throws CalFacadeException;

  /** Attendee wants a refresh
   *
   * @param ei event which is probably in a calendar.
   * @param comment - optional comment
   * @return   ScheduleResult
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  ShareResultType share(final String principalHref,
                        final BwCalendar col,
                        final ShareType share) throws CalFacadeException;


  /** Publish the collection - that is make it available for subscriptions.
   *
   * @param col
   * @throws CalFacadeException
   */
  void publish(BwCalendar col) throws CalFacadeException;

  /** Unpublish the collection - that is make it unavailable for subscriptions
   * and remove any existing subscriptions.
   *
   * @param col
   * @throws CalFacadeException
   */
  void unpublish(BwCalendar col) throws CalFacadeException;

  /**
   * @param reply the request
   * @return a ReplyResult object.
   * @throws CalFacadeException
   */
  SharingI.ReplyResult sharingReply(final InviteReplyType reply) throws CalFacadeException;

  /** Subscribe to the collection - must be a published collection.
   *
   * @param colPath
   * @param subscribedName name for new alias
   * @return path of new alias and flag
   * @throws CalFacadeException
   */
  SharingI.SubscribeResult subscribe(String colPath,
                                     String subscribedName) throws CalFacadeException;

  /** Subscribe to an external url.
   *
   * @param extUrl
   * @param subscribedName name for new alias
   * @param refresh - refresh rate in minutes <= 0 for default
   * @param remoteId - may be null
   * @param remotePw  - may be null
   * @return path of new alias and flag
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  BwSynchInfo getSynchInfo() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  /** Find the named view.
   *
   * @param  val     String view name - null means default
   * @return BwView  null view not found.
   * @throws CalFacadeException
   */
  BwView findView(String val) throws CalFacadeException;

  /** Add a view.
   *
   * @param  val           BwView to add
   * @param  makeDefault   boolean true for make this the default.
   * @return boolean false view not added, true - added.
   * @throws CalFacadeException
   */
  boolean addView(BwView val,
                  boolean makeDefault) throws CalFacadeException;

  /** Return the collection of views - named collections of collections
   *
   * @return collection of views
   * @throws CalFacadeException
   */
  Collection<BwView> getAllViews() throws CalFacadeException;

  /** Add a collection path to the named view.
   *
   * @param  name    String view name - null means default
   * @param  path     collection path to add
   * @return boolean false view not found, true - collection path added.
   * @throws CalFacadeException
   */
  boolean addViewCollection(String name,
                            String path) throws CalFacadeException;

  /** Remove a collection path from the named view.
   *
   * @param  name    String view name - null means default
   * @param  path    collection path to remove
   * @return boolean false view not found, true - collection path removed.
   * @throws CalFacadeException
   */
  boolean removeViewCollection(String name,
                               String path) throws CalFacadeException;

  /** Remove the view for the owner of the object.
   *
   * @param  val     BwView
   * @return boolean false - view not found.
   * @throws CalFacadeException
   */
  boolean removeView(BwView val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  /** Called if anything is changed which affects the state of the client, e.g
   * switching display flags, deleting collections.
   *
   * @throws CalFacadeException
   */
  public void flushState() throws CalFacadeException;

  /** Set the view to the given named view. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     String view name - null for default
   * @return boolean false - view not found.
   * @throws CalFacadeException
   */
  boolean setCurrentView(String val) throws CalFacadeException;

  /** Set the view to the given view object. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     view name - null for default
   * @throws CalFacadeException
   */
  void setCurrentView(final BwView val) throws CalFacadeException;

  /** Get the current view we have set
   *
   * @return BwView    named Collection of Collections or null for default
   * @throws CalFacadeException
   */
  BwView getCurrentView() throws CalFacadeException;

  /** Set the virtual path and unset any current view.
   *
   * <p>A virtual path is the apparent path for a user looking at an explorer
   * view of collections.
   *
   * <p>We might have,
   * <pre>
   *    home-->Arts-->Theatre
   * </pre>
   *
   * <p>In reality the Arts collection might be an alias to another alias which
   * is an alias to a collection containing aliases including "Theatre".
   *
   * <p>So the real picture might be something like...
   * <pre>
   *    home-->Arts             (categories="ChemEng")
   *            |
   *            V
   *           Arts             (categories="Approved")
   *            |
   *            V
   *           Arts-->Theatre   (categories="Arts" AND categories="Theatre")
   *                     |
   *                     V
   *                    MainCal
   * </pre>
   * where the vertical links are aliasing. The importance of thsi is that
   * each alias might introduce another filtering term, the intent of which is
   * to restrict the retrieval to a specific subset. The parenthesized terms
   * represent example filters.
   *
   * <p>The desired filter is the ANDing of all the above.
   *
   * @param  vpath  a String virtual path
   * @return false for bad path
   * @throws CalFacadeException
   */
  boolean setVirtualPath(final String vpath) throws CalFacadeException;

  /**
   * @return non-null if setVirtualPath was called successfully
   * @throws CalFacadeException
   */
  String getVirtualPath() throws CalFacadeException;

  /** Given a possible collection object return whatever is appropriate for the
   * current view.
   *
   * <p>If the collection is non-null go with that, otherwise go with the
   * current selected collection or the current selected view.
   *
   * @param col
   * @return BwFilter or null
   * @throws CalFacadeException
   */
  FilterBase getViewFilter(BwCalendar col) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  /** Move the contents of one collection to another
   *
   * @param cal where from
   * @param newCal where to
   * @return
   * @throws CalFacadeException
   */
  void moveContents(final BwCalendar cal,
                    final BwCalendar newCal) throws CalFacadeException;

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
   * @throws CalFacadeException
   */
  UpdateFromTimeZonesInfo updateFromTimeZones(int limit,
                                              boolean checkOnly,
                                              UpdateFromTimeZonesInfo info)
          throws CalFacadeException;

  /**
   * @param val
   * @throws CalFacadeException
   */
  void postMessage(Message val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Search
   * ------------------------------------------------------------ */

  /** Called to search an index. If publick is false use the principal to
   * identify the index.
   *
   * XXX Security implications here. We should probably not return a count for
   * searching another users entries - or we should getResource an accurate count of
   * entries this user has access to.
   *
   * @param publick true for public indec
   * @param principal ignored if publick is true
   * @param   query    Query string
   * @param   limits   limits or null
   * @return  int      Number found. 0 means none found,
   *                                -1 means indeterminate
   * @throws CalFacadeException
   */
  int search(boolean publick,
             String principal,
             String query,
             SearchLimits limits) throws CalFacadeException;

  /** Convenience method to limit to now onwards.
   *
   * @return SearchLimits
   */
  SearchLimits fromToday();

  /** Convenience method to limit to before now.
   *
   * @return SearchLimits
   */
  SearchLimits beforeToday();

  /** Called to retrieve results after a search of the index.
   *
   * @param start in result set
   * @param num of entries
   * @param   limits   limits or null
   * @return  Collection of BwIndexSearchResultEntry
   * @throws CalFacadeException
   */
  Collection<SearchResultEntry> getSearchResult(int start,
                                                int num,
                                                SearchLimits limits)
          throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Access
   * ------------------------------------------------------------ */

  /** Change the access to the given calendar entity.
   *
   * @param ent      BwShareableDbentity
   * @param aces     Collection of ace
   * @param replaceAll true to replace the entire access list.
   * @throws CalFacadeException
   */
  void changeAccess(BwShareableDbentity ent,
                    Collection<Ace> aces,
                    boolean replaceAll) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Calendar Suites
   * ------------------------------------------------------------ */

  /** Set the calendar suite we are running as. Must be running as an
   * unauthenticated user.
   *
   * @param name unique name for the suite
   * @throws CalFacadeException
   */
  void setCalSuite(String name) throws CalFacadeException;

  /** Get the current calendar suite.
   *
   * @return BwCalSuiteWrapper null for unknown calendar suite
   * @throws CalFacadeException
   */
  BwCalSuiteWrapper getCalSuite() throws CalFacadeException;

  /** Get a calendar suite given the 'owning' group
   *
   * @param  group     BwAdminGroup
   * @return BwCalSuiteWrapper null for unknown calendar suite
   * @throws CalFacadeException
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
   * @throws CalFacadeException
   */
  BwCalSuiteWrapper getCalSuite(String name) throws CalFacadeException;

  /** Create a new calendar suite
   *
   * @param name
   * @param adminGroupName - name of the admin group
   * @param rootCollectionPath
   * @param submissionsPath
   * @return BwCalSuiteWrapper for new object
   * @throws CalFacadeException
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
   * @param rootCollectionPath
   * @param submissionsPath
   * @throws CalFacadeException
   */
  void updateCalSuite(BwCalSuiteWrapper cs,
                      String adminGroupName,
                      String rootCollectionPath,
                      String submissionsPath) throws CalFacadeException;

  /** Delete a calendar suite object
   *
   * @param  val     BwCalSuiteWrapper object
   * @throws CalFacadeException
   */
  void deleteCalSuite(BwCalSuiteWrapper val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                   Calendar Suite Resources
   * ------------------------------------------------------------ */

  /** Get a list of resources. The content is not fetched.
   *
   * @param suite - calendar suite
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return list
   * @throws CalFacadeException
   */
  List<BwResource> getCSResources(BwCalSuite suite,
                                  String rc) throws CalFacadeException;

  /** Get named resource. The content is fetched.
   *
   * @param suite - calendar suite
   * @param name
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @return resource or null
   * @throws CalFacadeException
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
   * @param res
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException
   */
  void addCSResource(BwCalSuite suite,
                     BwResource res,
                     String rc) throws CalFacadeException;

  /** Delete named resource
   *
   * @param suite - calendar suite
   * @param name
   * @param rc - define class of resource "calsuite", "admin"or "global"
   * @throws CalFacadeException
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
   * @return BwFilter null for unknown filter
   * @throws CalFacadeException
   */
  BwFilterDef getFilter(String name) throws CalFacadeException;

  /** Parse the xml definition in the given filter object
   *
   * @param  val       BwFilterDef
   * @throws CalFacadeException
   */
  void parseFilter(BwFilterDef val) throws CalFacadeException;

  /** Get filter definitions to which this user has access
   *
   * @return Collection     of BwCalSuiteWrapper
   * @throws CalFacadeException
   */
  Collection<BwFilterDef> getAllFilters() throws CalFacadeException;

  /** Validate a filter definition.
   *
   * @param  val       String xml filter definition
   * @throws CalFacadeException
   */
  public void validateFilter(String val) throws CalFacadeException;

  /** Validate and persist a new filter definition
   *
   * @param  val       filter definition
   * @throws CalFacadeException for errrors including duplicate name
   */
  public void saveFilter(BwFilterDef val) throws CalFacadeException;

  /** Delete a filter given the name
   *
   * @param  name     String name of filter
   * @throws CalFacadeException
   */
  void deleteFilter(String name) throws CalFacadeException;
}
