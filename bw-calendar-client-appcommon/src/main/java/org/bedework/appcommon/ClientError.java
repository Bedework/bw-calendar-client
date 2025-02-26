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
package org.bedework.appcommon;

import java.io.Serializable;

/**
 * Define error property codes emitted by clients. Suggested English Language
 * is supplied in the comment for clarification only.
 *
 * @author Mike Douglass
 *
 */
public interface ClientError extends Serializable {
  /** Prefix for all these errors */
  String prefix = "org.bedework.client.error.";

  /** Exception text */
  String exc = prefix + "exc";

  /** Error: this admin group is already assigned to calendar suite: */
  String adminGroupAssignedCS = prefix + "admingroupassignedcs";

  /** Error: already a member: */
  String alreadyMember = prefix + "alreadymember";

  /** Error: bad filter. */
  String badFilter = prefix + "badfilter";

  /** Error: bad interval. */
  String badInterval = prefix + "badinterval";

  /** Error: bad interval unit. */
  String badIntervalUnit = prefix + "badintervalunit";

  /** Error: Bad request */
  String badRequest = prefix + "badrequest";

  /** Error: Bad scheduling what parameter */
  String badScheduleWhat = prefix + "badschedulewhat";

  /** Error: bad virtual path. */
  String badVpath = prefix + "badvpath";

  /** Error: calendar suite not added. */
  String calsuiteNotAdded = prefix + "calsuitenotadded";

  /** Forbidden: you are not allowed to change the mode of a non-empty collection. */
  String cannotChangeCalmode = prefix + "cannotchangecalmode";

  /** Forbidden: you are not allowed to delete your calendar home. */
  String cannotDeleteHome = prefix + "cannotdeletehome";

  /** Forbidden: you are not allowed to move your calendar home. */
  String cannotMoveHome = prefix + "cannotmovehome";

  /** Error: choose group is suppressed.  You cannot perform that action at this time. */
  String chooseGroupSuppressed = prefix + "choosegroupsuppressed";

  /** Error: closed. */
  String closed = prefix + " interface closed";

  /** Cannot add: the category already exists. */
  String duplicateCategory = prefix + "duplicatecategory";

  /** Cannot add: the contact already exists. */
  String duplicateContact = prefix + "duplicatecontact";

  /** Error: duplicate admin group.   already exists. */
  String duplicateGroup = prefix + "duplicategroup";

  /** Error: duplicate image.   already exists. */
  String duplicateImage = prefix + "duplicateimage";

  /** Cannot add: the location already exists. */
  String duplicateLocation = prefix + "duplicatelocation";

  /** Error: duplicate uid. must be unique within the calendar collection. */
  String duplicateUid = prefix + "duplicateuid";

  /** Uid/recurrenceid mismatch: possibly updating in multiple browser tabs. */
  String eventMismatch = prefix + "event.mismatch";

  /** Error: there were <n> failed overrides. */
  String failedOverrides = prefix + "failed.overrides";

  /** Error: Error processing image. */
  String imageError = prefix + "imageerror";

  /** Error: Session was logged out. Redo the operation. */
  String loggedOut = prefix + "loggedout";

  /** Error: the email has no recipient. */
  String mailNoRecipient = prefix + "mail.norecipient";

  /** Error: missing calendar path. */
  String missingCalendarPath = prefix + "missingcalendarpath";

  /** Error: missing event key fields. */
  String missingEventKeyFields = prefix + "missingeventkeyfields";

  /** Error: missing file name. */
  String missingFileName = prefix + "missingfilename";

  /** Error: missing image directory. */
  String missingImageDirectory = prefix + "missingimagedirectory";

  /** Error: Missing request parameter */
  String missingRequestPar = prefix + "missingrequestpar";

  /** Error: Missing scheduling what parameter */
  String missingScheduleWhat = prefix + "missingschedulewhat";

  /** System Error: Multiple events when one expected */
  String multipleEvents = prefix + "multipleevents";

  /** Error: no access. */
  String noAccess = prefix + "noaccess";

  /** Error: no access to calendar suite for group ... */
  String noCalsuiteAccess = prefix + "nocalsuiteaccess";

  /** Error: no default view. */
  String noDefaultView = prefix + "nodefaultview";

  /** Error: no meeting for refresh. */
  String noMeeting = prefix + "nomeeting";

  /** Error: no instances for this recurring event. */
  String noRecurrenceInstances = prefix + "norecurrenceinstances";

  /** Error: no scheduling access. */
  String noSchedulingAccess = prefix + "noschedulingaccess";

  /** Error: a group may not be added to itself */
  String onGroupPath = prefix + "ongrouppath";

  /** You can only respond from your inbox. */
  String onlyFromInbox = prefix + "onlyfrominbox";

  /** You can only reply to an inbox. */
  String onlyToInbox = prefix + "onlytoinbox";

  /** Cannot delete: the calendar is not empty. */
  String referencedCalendar = prefix + "referenced.calendar";

  /** Cannot delete: the category is referenced by events. */
  String referencedCategory = prefix + "referenced.category";

  /** Cannot delete: the contact is referenced by events. */
  String referencedContact = prefix + "referenced.contact";

  /** Cannot delete: the location is referenced by events. */
  String referencedLocation = prefix + "referenced.location";

  /** Cannot delete: the subscription is included in view .<br/>
      You must remove the subscription from this view before deleting. */
  String referencedSubscription = prefix + "referenced.subscription";

  /** Refresh failed:  */
  String refreshCalendarFailed = prefix + "refresh.failed";

  /** Not found: there is no attendee identified by the uri. */
  String unknownAttendee = prefix + "unknown.attendee";

  /** Not found: there is no calendar identified by the path . */
  String unknownCalendar = prefix + "unknown.calendar";

  /** Not found: there is no calendar suite identified by the name . */
  String unknownCalendarSuite = prefix + "unknown.calendarsuite";

  /** Not found: there is no calendar identified by the type . */
  String unknownCalendarType = prefix + "unknown.calendartype";

  /** Not found: there is no category identified by the key . */
  String unknownCategory = prefix + "unknown.category";

  /** Not found: there is no contact . */
  String unknownContact = prefix + "unknown.contact";

  /** Not found: there is no such event. */
  String unknownEvent = prefix + "unknown.event";

  /** Error: unknown filter: */
  String unknownFilter = prefix + "unknown.filter";

  /** Error: unknown group: */
  String unknownGroup = prefix + "unknown.group";

  /** Not found: there is no location identified by the id . */
  String unknownLocation = prefix + "unknown.location";

  /** Not found: the user  was not found. */
  String unknownUser = prefix + "unknown.user";

  /** Not found: there is no user identified by the id . */
  String unknownUserid = prefix + "unknown.userid";

  /** Not found: there is no view identified by the name . */
  String unknownView = prefix + "unknown.view";

  /** Not found: there is no resource identified by the name . */
  String unknownResource = prefix + "unknown.resource";

  String duplicateResource = prefix + "duplicateresource";

  /** Error: view not added. */
  String viewNotAdded = prefix + "viewnotadded";

  /** Error: already subscribed on path x. */
  String alreadySubscribed = prefix + "already.subscribed";

  /** Error: not suggested to this group. */
  String notSuggested = prefix + "not.suggested";


/* These may be unused

  String badEntityId = prefix + "badentityid";
          / ** Error: bad entity id. * /

  String noEntityId = prefix + "noentityid";
          / ** Error: no entity id. * /

  String unimplemented = prefix + "unimplemented";
          / ** Unimplemented: the feature you are trying to use has not been implemented yet. * /

  String badHow = prefix + "badhow";
          / ** Error: bad ACL request (bad how setting). * /

  / * These were all org.bedework.??.error. * /


/
  String noSuchAdmingroup = prefix + "nosuchadmingroup";
          / ** Error: no such admin group "". * /

  String badField = prefix + "badfield";
    / ** Please correct your data input for . * /
*/
}
