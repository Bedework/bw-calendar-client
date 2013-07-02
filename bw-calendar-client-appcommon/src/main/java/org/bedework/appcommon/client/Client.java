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
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPreferences;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.SchedulingI;

import edu.rpi.cmt.access.Ace;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * This interface defines the interactions with the back end system
 * for a client.
 *
 * @author  Mike Douglass douglm  rpi.edu
 */
public interface Client extends Serializable {
  /** Return properties about the system.
   *
   * @return SystemProperties object - never null.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  SystemProperties getSystemProperties() throws CalFacadeException;

  /**
   * If possible roll back the changes.
   */
  void rollback();

  /**
   * @return System limit or user overrride - bytes.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  long getUserMaxEntitySize() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Principals
   * ------------------------------------------------------------ */

   /**
   *
   * @return the current principal we are acting for
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwPrincipal getCurrentPrincipal() throws CalFacadeException;

  /** Get the current principal to set as owner
   *
   * @return BwPrincipal object
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwPrincipal getOwner() throws CalFacadeException;

  /**
   *
   * @param cua   calendar user address
   * @return principal corresponding or null for unknown
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwPrincipal calAddrToPrincipal(String cua) throws CalFacadeException;

  /**
   *
   * @return href for current principal
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  String getCurrentPrincipalHref() throws CalFacadeException;

  /**
   *
   * @return current principal account
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  String getCurrentAccount() throws CalFacadeException;

  /** Find the user with the given account name.
   *
   * @param val           String user id
   * @return User principal or null if not there
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwPrincipal getUser(final String val) throws CalFacadeException;

  /**
   *
   * @param user          String user id
   * @return calendar address or null for unknown user
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * For the moment we do a transform of the account to get a mailto.
   *
   * @param val        uri
   * @return caladdr for this system or null for an invalid uri
   * @throws org.bedework.calfacade.exc.CalFacadeException  for errors
   */
  String uriToCaladdr(String val) throws CalFacadeException;

  /**
   *
   * @return current calendar address
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  String getCurrentCalendarAddress() throws CalFacadeException;

  /**
   * @param id
   * @param whoType - from WhoDefs
   * @return String principal uri
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  String makePrincipalUri(String id,
                          int whoType) throws CalFacadeException;

  /** Return principal for the given href.
   *
   * @param href
   * @return Principal
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  boolean validPrincipal(String href) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  /** Returns the current user preferences.
   *
   * @return BwPreferences   prefs for the current user
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwPreferences getPreferences() throws CalFacadeException;

  /** Update preferred entity list for admin user.
   *
   * @param remove - true if removing object
   * @param col to add or remove
   * @param cat to add or remove
   * @param loc to add or remove
   * @param contact to add or remove
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void updateAdminPrefs(boolean remove,
                        BwCalendar col,
                        BwCategory cat,
                        BwLocation loc,
                        BwContact contact) throws CalFacadeException;

  /** Get the default calendar path for the current user.
   *
   * @return String path or null for no preference
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  public String getPreferredCollectionPath() throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  /**
   * @return home for current principal
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCalendar getHome() throws CalFacadeException;

  /**
   *
   * @param path for collection
   * @return collection object or null.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCalendar getCollection(String path) throws CalFacadeException;

  /** Get a special calendar (e.g. Trash) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCalendar addCollection(BwCalendar val,
                           String parentPath) throws CalFacadeException;

  /** Update the given collection
   *
   * @param col the collection
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void updateCollection(BwCalendar col) throws CalFacadeException;

  /** Attempt to get collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val collection
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy
   * @return BwCalendar
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCalendar resolveAlias(BwCalendar val,
                          boolean resolveSubAlias,
                          boolean freeBusy) throws CalFacadeException;

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  col          parent collection
   * @return Collection   of BwCalendar
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  Collection<BwCalendar> getChildren(BwCalendar col)
          throws CalFacadeException;

  /** Move a calendar object from one parent to another
   *
   * @param  val         BwCalendar object
   * @param  newParent   BwCalendar potential parent
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  Collection<BwCalendar> decomposeVirtualPath(String vpath) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  /**
   * @param name unique (for current user) value for the category -
   *             current default language assumed.
   * @return category entity or null.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCategory getCategoryByName(String name) throws CalFacadeException;

  /**
   * @param name unique (for current user) value for the category -
   *             possible language set.
   * @return category entity or null.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCategory getCategoryByName(BwString name) throws CalFacadeException;

  /**
   * @param uid of the category
   * @return category entity or null.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwCategory getCategory(String uid) throws CalFacadeException;

  /** Check for existence
   *
   * @param cat the category
   * @return true if exists
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  boolean categoryExists(BwCategory cat) throws CalFacadeException;

  /** Update a category.
   *
   * @param val   category object to be updated
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void updateCategory(BwCategory val) throws CalFacadeException;

  /** Add the given category.
   *
   * @param val
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void addCategory(BwCategory val) throws CalFacadeException;

  public interface DeleteCategoryResult {
    boolean getDeleted();

    Collection<EventPropertiesReference> getReferences();
  }

  /** Delete an entity
   *
   * @param val      BwEventProperty object to be deleted
   * @return result indicating what happened - null for not there
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  DeleteCategoryResult deleteCategory(BwCategory val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  /** Get the contact with the given uid.
   *
   * @param uid
   * @return contact object
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwContact getContact(String uid) throws CalFacadeException;

  /** Find the contact.
   *
   * @param val
   * @return contact object
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwContact findContact(BwString val) throws CalFacadeException;

  /** Add the contact
   * @param val
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void addContact(BwContact val) throws CalFacadeException;

  /** Ensure a contact exists. If it already does returns the object.
   * If not creates the entity.
   *
   * @param val     contact object.
   * @param ownerHref   String principal href, null for current user
   * @return contact object.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwContact ensureContactExists(final BwContact val,
                                final String ownerHref)
          throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  /** Get the location with the given uid.
   *
   * @param uid
   * @return Location object
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwLocation getLocation(String uid) throws CalFacadeException;

  /** Find the location given the address.
   *
   * @param address
   * @return Location object
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  BwLocation findLocation(BwString address) throws CalFacadeException;

  /** Add the location
   * @param val
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void addLocation(BwLocation val) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  /** Claim ownership of this event
   *
   * @param ev  event
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   *                  which case we want overrides. Otherwise we get
   *                  an expanded form in which each instance of
   *                  recurring events is represented as a separate event.
   * @param exact true if we must have a true representation of the
   *              stored events. Otherwise we may fetch a cached set
   *              which doesn't necessarily have all the events due to
   *              commit delays.
   * @param pos entity number - start at 0
   * @param pageSize number of entities per page, -1 for all - may be ignored
   * @return events and information
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                     final boolean noInvites,
                                     String fromAttUri) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  /** Get aggregated free busy for a ScheduleResult.
   *
   * @param sr ScheduleResult
   * @param start
   * @param end
   * @param granularity
   * @return FbResponses
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @param method - the scheduling method
   * @param recipient - non null to send to this recipient only (for REFRESH)
   * @param fromAttUri
   * @param iSchedule  true if it's an iSchedule request.
   * @return ScheduleResult
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  public ScheduleResult schedule(EventInfo ei,
                                 int method,
                                 String recipient,
                                 String fromAttUri,
                                 boolean iSchedule) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  /** Return the collection of views - named collections of collections
   *
   * @return collection of views
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  Collection<BwView> getAllViews() throws CalFacadeException;

  /** Remove a collection path from the named view.
   *
   * @param  name    String view name - null means default
   * @param  path    collection path to remove
   * @return boolean false view not found, true - collection path removed.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  boolean removeViewCollection(String name,
                               String path) throws CalFacadeException;

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  /** Called if anything is changed which affects the state of the client, e.g
   * switching display flags, deleting collections.
   *
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  public void flushState() throws CalFacadeException;

  /** Set the view to the given named view. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     String view name - null for default
   * @return boolean false - view not found.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  boolean setCurrentView(String val) throws CalFacadeException;

  /** Set the view to the given view object. Null means reset to default.
   * Unset current calendar.
   *
   * @param  val     view name - null for default
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void setCurrentView(final BwView val) throws CalFacadeException;

  /** Get the current view we have set
   *
   * @return BwView    named Collection of Collections or null for default
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  boolean setVirtualPath(final String vpath) throws CalFacadeException;

  /**
   * @return non-null if setVirtualPath was called successfully
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
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
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void moveContents(final BwCalendar cal,
                    final BwCalendar newCal) throws CalFacadeException;

  /* ====================================================================
   *                   Access
   * ==================================================================== */

  /** Change the access to the given calendar entity.
   *
   * @param ent      BwShareableDbentity
   * @param aces     Collection of ace
   * @param replaceAll true to replace the entire access list.
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  void changeAccess(BwShareableDbentity ent,
                    Collection<Ace> aces,
                    boolean replaceAll) throws CalFacadeException;
 }
