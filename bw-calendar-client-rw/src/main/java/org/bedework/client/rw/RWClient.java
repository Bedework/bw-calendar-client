package org.bedework.client.rw;

import org.bedework.access.Ace;
import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.Participant;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.ShareableEntity;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EnsureEntityExistsResult;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.RealiasResult;
import org.bedework.calfacade.svc.SharingReplyResult;
import org.bedework.calfacade.svc.SubscribeResult;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.SchedulingI;
import org.bedework.util.misc.response.Response;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * User: mike Date: 3/8/21 Time: 15:52
 */
public interface RWClient extends Client {
  /**
   *
   * @return true if we are the web user client.
   */
  boolean getWebUser();

  /**
   * @param href to be unindexed
   */
  void unindex(String href);

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

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
   */
  BwCalendar addCollection(BwCalendar val,
                           String parentPath);

  /** Update the given collection
   *
   * @param col the collection
   */
  void updateCollection(BwCalendar col);

  /** Delete a collection. Also remove it from the current user preferences (if any).
   *
   * @param val      BwCalendar collection
   * @param emptyIt  true to delete contents
   * @return boolean  true if it was deleted.
   *                  false if it didn't exist
   */
  boolean deleteCollection(BwCalendar val,
                           boolean emptyIt);

  /** Move a calendar object from one parent to another
   *
   * @param  val         BwCalendar object
   * @param  newParent   BwCalendar potential parent
   */
  void moveCollection(BwCalendar val,
                      BwCalendar newParent);

  /** Refresh a subscription represented by this calendar object
   *
   * @param  val         BwCalendar object
   */
  Response refreshSubscription(BwCalendar val);

  /** Return a list of calendars in which calendar objects can be
   * placed by the current user.
   *
   * <p>Caldav does not allow collections inside collections so that
   * calendar collections are the leaf nodes only.
   *
   *
   * @return Set   of BwCalendar
   */
  Collection<BwCalendar> getAddContentCollections()
         ;

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  /**
   * @param uid of the category
   * @return category entity or null.
   */
  BwCategory getPersistentCategory(String uid);

  /** Add the given category.
   *
   * @param val category
   * @return false if not added because already exists
   */
  boolean addCategory(BwCategory val);

  /** Update a category.
   *
   * @param val   category object to be updated
   */
  void updateCategory(BwCategory val);

  interface DeleteReffedEntityResult {
    boolean getDeleted();

    Collection<EventPropertiesReference> getReferences();
  }

  /** Delete a category
   *
   * @param val      BwEventProperty object to be deleted
   * @return result indicating what happened - null for not there
   */
  DeleteReffedEntityResult deleteCategory(BwCategory val);

  /** Get the contact with the given uid.
   *
   * @param uid of contact
   * @return contact object
   */
  BwContact getPersistentContact(String uid);

  /** Add the contact
   * @param val contact
   */
  void addContact(BwContact val);

  /** Update the contact
   * @param val contact
   */
  void updateContact(BwContact val);

  /** Delete a contact
   *
   * @param val      contact to be deleted
   * @return result indicating what happened - null for not there
   */
  DeleteReffedEntityResult deleteContact(BwContact val);

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
  BwLocation getPersistentLocation(String uid);

  /** Add the location
   * @param val a location
   * @return true if added
   */
  boolean addLocation(BwLocation val);

  /** Update the location
   * @param val a location
   */
  void updateLocation(BwLocation val);

  /** Delete a location
   *
   * @param val      location to be deleted
   * @return result indicating what happened - null for not there
   */
  DeleteReffedEntityResult deleteLocation(BwLocation val);

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
  EventInfo.UpdateResult addEvent(EventInfo ei,
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
   * @param alwaysWrite  write and reindex whatever changetable says
   * @return UpdateResult Counts of changes.
   */
  EventInfo.UpdateResult updateEvent(EventInfo ei,
                                     boolean noInvites,
                                     String fromAttUri,
                                     boolean alwaysWrite);

  /** Move the event to the collection specified in newPath. There must
   * be no event with the same uid in the destination.
   *
   * @param ei           EventInfo object to be moved
   * @param newPath      Destination collection.
   * @return UpdateResult status.
   */
  Response moveEvent(EventInfo ei,
                     String newPath);

  /** Delete an event.
   *
   * @param ei                 BwEvent object to be deleted
   * @param sendSchedulingMessage   Send a declined or cancel scheduling message
   * @return Response with status ok  if event deleted
   */
  Response deleteEvent(EventInfo ei,
                       boolean sendSchedulingMessage);

  /** For an event to which we have write access we simply mark it deleted.
   *
   * <p>Otherwise we add an annotation maarking the event as deleted.
   *
   * @param event to delete
   */
  void markDeleted(BwEvent event);

  /* ------------------------------------------------------------
   *                     Notifications
   * ------------------------------------------------------------ */

  /** Find a notification in the notification collection for the
   * current principal with the given name.
   *
   * @param name - of the notification
   * @return null for no notification or the notification with that name
   */
  NotificationType findNotification(String name);

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
   */
  void removeNotification(NotificationType val);

  /** Remove all notifications for given principal
   *
   * @param principalHref principal to clean
   */
  void removeAllNotifications(String principalHref);

  void subscribe(String principalHref,
                 List<String> emails);

  void unsubscribe(String principalHref,
                   List<String> emails);

  /* ------------------------------------------------------------
   *                     Resources
   * ------------------------------------------------------------ */

  /** Save a resource at the given collection path. The collection MUST exist.
   *
   * @param  val       resource with attached content
   */
  void saveResource(BwResource val);

  /** Get a resource given the path - does not getResource content
   *
   * @param  path     String path to resource
   * @return BwResource null for unknown resource
   */
  BwResource getResource(String path);


  /** Retrieve resource content given the resource. It will be set in the resource
   * object
   *
   * @param  val BwResource
   * @return false if content missing
   */
  boolean getResourceContent(BwResource val);

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @return List     of BwResource
   */
  List<BwResource> getAllResources(String path);

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @param count this many
   * @return List     of BwResource
   */
  List<BwResource> getResources(String path,
                                int count);

  /** Update a resource.
   *
   * @param  val          resource
   * @param updateContent if true we also update the content
   */
  void updateResource(BwResource val,
                      boolean updateContent);

  /**
   *
   * @param val the resource object
   * @param content to embed
   */
  void setResourceValue(BwResource val,
                        String content);

  /**
   *
   * @param val the resource object
   * @param content to embed
   */
  void setResourceValue(BwResource val,
                        byte[] content);

  /**
   *
   * @param val the resource object
   * @param content input stream with content
   */
  void setResourceValue(BwResource val,
                        InputStream content,
                        long length);

  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  /** Find the attendee in this event which corresponds to the current user
   *
   * @param ei to search
   * @return attendee or null.
   */
  Participant findUserAttendee(EventInfo ei);

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
   */
  BwEvent getFreeBusy(Collection<BwCalendar> fbset,
                      BwPrincipal<?> who,
                      BwDateTime start,
                      BwDateTime end,
                      BwOrganizer org,
                      String uid,
                      String exceptUid)
         ;

  /** Get aggregated free busy for a ScheduleResult.
   *
   * @param sr ScheduleResult
   * @param start of period
   * @param end of period
   * @param granularity of result - eg 1 hour
   * @return FbResponses
   */
  SchedulingI.FbResponses aggregateFreeBusy(ScheduleResult sr,
                                            BwDateTime start,
                                            BwDateTime end,
                                            BwDuration granularity);

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
   * @param fromAttUri - identifies the attendee
   * @param iSchedule  true if it's an iSchedule request.
   * @return ScheduleResult
   */
  ScheduleResult schedule(EventInfo ei,
                          String recipient,
                          String fromAttUri,
                          boolean iSchedule);

  /** Return the users copy of the active meeting with the
   * same uid as that given.
   *
   * @param ev the event to search for
   * @return possibly null meeting
   */
  EventInfo getStoredMeeting(BwEvent ev);

  /** Attendee wants a refresh
   *
   * @param ei event which is probably in a calendar.
   * @param comment - optional comment
   * @return   ScheduleResult
   */
  ScheduleResult requestRefresh(EventInfo ei,
                                String comment);

  /* ------------------------------------------------------------
   *                     Sharing
   * ------------------------------------------------------------ */
  /**
   * @param principalHref share as this user.
   * @param col MUST be a sharable collection
   * @param share the request
   * @return list of ok and !ok sharees
   */
  ShareResultType share(String principalHref,
                        BwCalendar col,
                        ShareType share);


  /** Publish the collection - that is make it available for subscriptions.
   *
   * @param col collection object
   */
  void publish(BwCalendar col);

  /** Unpublish the collection - that is make it unavailable for subscriptions
   * and remove any existing subscriptions.
   *
   * @param col collection object
   */
  void unpublish(BwCalendar col);

  /**
   * @param reply the request
   * @return a ReplyResult object.
   */
  SharingReplyResult sharingReply(InviteReplyType reply);

  /** Subscribe to the collection - must be a published collection.
   *
   * @param colPath  collection path
   * @param subscribedName name for new alias
   * @return path of new alias and flag
   */
  SubscribeResult subscribe(String colPath,
                            String subscribedName);

  /** Subscribe to an external url.
   *
   * @param extUrl the url
   * @param subscribedName name for new alias
   * @param refresh - refresh rate in minutes <= 0 for default
   * @param remoteId - may be null
   * @param remotePw  - may be null
   * @return path of new alias and flag
   */
  SubscribeResult subscribeExternal(String extUrl,
                                    String subscribedName,
                                    int refresh,
                                    String remoteId,
                                    String remotePw);

  /* ------------------------------------------------------------
   *                     Synch
   * ------------------------------------------------------------ */

  /** Returns the synch service information.
   *
   * @return full synch info
   */
  BwSynchInfo getSynchInfo();

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  /** Add a view.
   *
   * @param  val           BwView to add
   * @param  makeDefault   boolean true for make this the default.
   * @return boolean false view not added, true - added.
   */
  boolean addView(BwView val,
                  boolean makeDefault);

  /** Add a collection path to the named view.
   *
   * @param  name    String view name - null means default
   * @param  path     collection path to add
   * @return boolean false view not found, true - collection path added.
   */
  boolean addViewCollection(String name,
                            String path);

  /** Remove a collection path from the named view.
   *
   * @param  name    String view name - null means default
   * @param  path    collection path to remove
   * @return boolean false view not found, true - collection path removed.
   */
  boolean removeViewCollection(String name,
                               String path);

  /** Remove the view for the owner of the object.
   *
   * @param  val     BwView
   * @return boolean false - view not found.
   */
  boolean removeView(BwView val);

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  /** Move the contents of one collection to another
   *
   * @param cal where from
   * @param newCal where to
   */
  void moveContents(BwCalendar cal,
                    BwCalendar newCal);

  /**
   * @param val mail message
   */
  void postMessage(Message val);

  /* ------------------------------------------------------------
   *                   Access
   * ------------------------------------------------------------ */

  /** Change the access to the given calendar entity.
   *
   * @param ent      BwShareableDbentity
   * @param aces     Collection of ace
   * @param replaceAll true to replace the entire access list.
   */
  void changeAccess(ShareableEntity ent,
                    Collection<Ace> aces,
                    boolean replaceAll);

  /* ------------------------------------------------------------
   *                     Filters
   * Possible these are used nowhere
   * ------------------------------------------------------------ */

  /** Validate a filter definition.
   *
   * @param  val       String xml filter definition
   */
  void validateFilter(String val);

  /** Validate and persist a new filter definition
   *
   * @param  val       filter definition
   */
  void saveFilter(BwFilterDef val);

  /** Delete a filter given the name
   *
   * @param  name     String name of filter
   */
  void deleteFilter(String name);
}
