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
  public static final String prefix = "org.bedework.client.error.";

  /** Exception text */
  public static final String exc = prefix + "exc";

  /** Error: this admin group is already assigned to calendar suite: */
  public static final String adminGroupAssignedCS = prefix + "admingroupassignedcs";

  /** Error: already a member: */
  public static final String alreadyMember = prefix + "alreadymember";

  /** Error: bad filter. */
  public static final String badFilter = prefix + "badfilter";

  /** Error: bad interval. */
  public static final String badInterval = prefix + "badinterval";

  /** Error: bad interval unit. */
  public static final String badIntervalUnit = prefix + "badintervalunit";

  /** Error: Bad request */
  public static final String badRequest = prefix + "badrequest";

  /** Error: Bad scheduling what parameter */
  public static final String badScheduleWhat = prefix + "badschedulewhat";

  /** Error: bad virtual path. */
  public static final String badVpath = prefix + "badvpath";

  /** Error: calendar suite not added. */
  public static final String calsuiteNotAdded = prefix + "calsuitenotadded";

  /** Forbidden: you are not allowed to change the mode of a non-empty collection. */
  public static final String cannotChangeCalmode = prefix + "cannotchangecalmode";

  /** Forbidden: you are not allowed to delete your calendar home. */
  public static final String cannotDeleteHome = prefix + "cannotdeletehome";

  /** Forbidden: you are not allowed to move your calendar home. */
  public static final String cannotMoveHome = prefix + "cannotmovehome";

  /** Error: choose group is suppressed.  You cannot perform that action at this time. */
  public static final String chooseGroupSuppressed = prefix + "choosegroupsuppressed";

  /** Cannot add: the category already exists. */
  public static final String duplicateCategory = prefix + "duplicatecategory";

  /** Cannot add: the contact already exists. */
  public static final String duplicateContact = prefix + "duplicatecontact";

  /** Error: duplicate admin group.   already exists. */
  public static final String duplicateGroup = prefix + "duplicategroup";

  /** Error: duplicate image.   already exists. */
  public static final String duplicateImage = prefix + "duplicateimage";

  /** Cannot add: the location already exists. */
  public static final String duplicateLocation = prefix + "duplicatelocation";

  /** Error: duplicate uid. must be unique within the calendar collection. */
  public static final String duplicateUid = prefix + "duplicateuid";

  /** Error: there were <n> failed overrides. */
  public static final String failedOverrides = prefix + "failed.overrides";

  /** Error: Error processing image. */
  public static final String imageError = prefix + "imageerror";

  /** Error: the email has no recipient. */
  public static final String mailNoRecipient = prefix + "mail.norecipient";

  /** Error: missing calendar path. */
  public static final String missingCalendarPath = prefix + "missingcalendarpath";

  /** Error: missing event key fields. */
  public static final String missingEventKeyFields = prefix + "missingeventkeyfields";

  /** Error: missing file name. */
  public static final String missingFileName = prefix + "missingfilename";

  /** Error: missing image directory. */
  public static final String missingImageDirectory = prefix + "missingimagedirectory";

  /** Error: Missing request parameter */
  public static final String missingRequestPar = prefix + "missingrequestpar";

  /** Error: Missing scheduling what parameter */
  public static final String missingScheduleWhat = prefix + "missingschedulewhat";

  /** System Error: Multiple events when one expected */
  public static final String multipleEvents = prefix + "multipleevents";

  /** Error: no access. */
  public static final String noAccess = prefix + "noaccess";

  /** Error: no access to calendar suite for group ... */
  public static final String noCalsuiteAccess = prefix + "nocalsuiteaccess";

  /** Error: no default view. */
  public static final String noDefaultView = prefix + "nodefaultview";

  /** Error: no meeting for refresh. */
  public static final String noMeeting = prefix + "nomeeting";

  /** Error: no instances for this recurring event. */
  public static final String noRecurrenceInstances = prefix + "norecurrenceinstances";

  /** Error: no scheduling access. */
  public static final String noSchedulingAccess = prefix + "noschedulingaccess";

  /** Error: a group may not be added to itself */
  public static final String onGroupPath = prefix + "ongrouppath";

  /** You can only respond from your inbox. */
  public static final String onlyFromInbox = prefix + "onlyfrominbox";

  /** You can only reply to an inbox. */
  public static final String onlyToInbox = prefix + "onlytoinbox";

  /** Cannot delete: the calendar is not empty. */
  public static final String referencedCalendar = prefix + "referenced.calendar";

  /** Cannot delete: the category is referenced by events. */
  public static final String referencedCategory = prefix + "referenced.category";

  /** Cannot delete: the contact is referenced by events. */
  public static final String referencedContact = prefix + "referenced.contact";

  /** Cannot delete: the location is referenced by events. */
  public static final String referencedLocation = prefix + "referenced.location";

  /** Cannot delete: the subscription is included in view .<br/>
      You must remove the subscription from this view before deleting. */
  public static final String referencedSubscription = prefix + "referenced.subscription";

  /** Timzone error: could not read file. */
  public static final String timezonesReadError = prefix + "timezones.readerror";

  /** Not found: there is no attendee identified by the uri. */
  public static final String unknownAttendee = prefix + "unknown.attendee";

  /** Not found: there is no calendar identified by the path . */
  public static final String unknownCalendar = prefix + "unknown.calendar";

  /** Not found: there is no calendar suite identified by the name . */
  public static final String unknownCalendarSuite = prefix + "unknown.calendarsuite";

  /** Not found: there is no calendar identified by the type . */
  public static final String unknownCalendarType = prefix + "unknown.calendartype";

  /** Not found: there is no category identified by the key . */
  public static final String unknownCategory = prefix + "unknown.category";

  /** Not found: there is no contact . */
  public static final String unknownContact = prefix + "unknown.contact";

  /** Not found: there is no such event. */
  public static final String unknownEvent = prefix + "unknown.event";

  /** Error: unknown filter: */
  public static final String unknownFilter = prefix + "unknown.filter";

  /** Error: unknown group: */
  public static final String unknownGroup = prefix + "unknown.group";

  /** Not found: there is no location identified by the id . */
  public static final String unknownLocation = prefix + "unknown.location";

  /** Not found: the user  was not found. */
  public static final String unknownUser = prefix + "unknown.user";

  /** Not found: there is no user identified by the id . */
  public static final String unknownUserid = prefix + "unknown.userid";

  /** Not found: there is no view identified by the name . */
  public static final String unknownView = prefix + "unknown.view";

  /** Not found: there is no resource identified by the name . */
  public static final String unknownResource = prefix + "unknown.resource";

  /** Error: view not added. */
  public static final String viewNotAdded = prefix + "viewnotadded";

  /** Error: already subscribed on path x. */
  public static final String alreadySubscribed = prefix + "already.subscribed";

  /** Error: not suggested to this group. */
  public static final String notSuggested = prefix + "not.suggested";


/* These may be unused

  public static final String badEntityId = prefix + "badentityid";
          / ** Error: bad entity id. * /

  public static final String noEntityId = prefix + "noentityid";
          / ** Error: no entity id. * /

  public static final String unimplemented = prefix + "unimplemented";
          / ** Unimplemented: the feature you are trying to use has not been implemented yet. * /

  public static final String badHow = prefix + "badhow";
          / ** Error: bad ACL request (bad how setting). * /

  / * These were all org.bedework.??.error. * /


/
  public static final String noSuchAdmingroup = prefix + "nosuchadmingroup";
          / ** Error: no such admin group "". * /

  public static final String badField = prefix + "badfield";
    / ** Please correct your data input for . * /
*/
}
