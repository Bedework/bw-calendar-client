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

/** Define all possible forwards. Internal routines should
 * return one of the following indices.
 *
 * @author  Mike Douglass  douglm@rpi.edu
 */
public interface ForwardDefs {
  // ENUM
  /** */
  String forwardSuccess = "success";

  /** */
  String forwardContinue = "continue";

  /** */
  String forwardRetry = "retry";

  /** */
  String forwardError = "error";

  /** */
  String forwardNoAccess = "noAccess";

  /** */
  String forwardNotFound = "notFound";

  /**
  String forwardNoSuchView = 6;

  /**
  String forwardNoViewDefined = 7;

  /** Set when an optional parameter is not found
  String forwardNoParameter = 8;

  /** Set when no action was taken */
  String forwardNoAction = "noAction";

  /** Something is referenced and cannot be removed */
  String forwardReffed = "reffed";

  /** an object was added/updated
  String forwardAdded = 11;

  /** */
  String forwardUpdated = "updated";

  /** */
  String forwardChooseGroup = "chooseGroup";

  /** */
  String forwardNoGroupAssigned = "noGroupAssigned";

  /** */
  String forwardEdit = "edit";

  /** */
  String forwardDuplicate = "duplicate";

  /** */
  String forwardValidationError = "validationError";

  /** */
  String forwardDelete = "delete";

  /** */
  String forwardCopy = "copy";

  /**
  String forwardFailed = 20;

  /** */
  String forwardGotomain = "gotomain";

  /** */
  String forwardBadValue = "badValue";

  /**
  String forwardCalendarNotFound = 23;

  /** */
  String forwardEventNotFound = "eventNotFound";

  /**
  String forwardBadDate = 25;

  /** */
  String forwardNotAllowed = "notAllowed";

  /** */
  String forwardInUse = "inUse";

  /** */
  String forwardNotAdded = "notAdded";

  /** */
  String forwardBadData = "baddata";

  /** */
  String forwardReferenced = "referenced";

  /** */
  String forwardBadPref = "retry";

  /** */
  String forwardCancelled = "cancelled";

  /** */
  String forwardBadRequest = "badRequest";

  /**
  String forwardNochange = 34;

  /** */
  String forwardDone = "done";

  /**
  String forwardExport = 36;

  /**
  String forwardStateSaved = 37;

  /**
  String forwardUpdateEventAccess = 38;

  /** */
  String forwardEditEventAttendees = "editEventAttendees";

  /** */
  String forwardEventIntoMeeting = "makeEventIntoMeeting";

  /** */
  String forwardEventDatesInited = "eventDatesInited";

  /**
  String forwardDeclined = 42;

  /** */
  String forwardNoMeeting = "noMeeting";

  /** */
  String forwardListAttendees = "listAttendees";

  /** */
  String forwardListEvents = "listEvents";

  /**
  String forwardList = 46;

  /** */
  String forwardNull = "FORWARD-NULL";

  /** */
  String forwardAddEventTab = "addEvent";

  /** */
  String forwardEventsTab = "events";

  /** */
  String forwardApprovalQTab = "approvalQueue";

  /** */
  String forwardPendingQTab = "pending";

  /** */
  String forwardSuggestionQTab = "suggestionQueue";

  /** */
  String forwardUsersTab = "users";

  /** */
  String forwardCalsuiteTab = "calsuite";

  /** */
  String forwardSystemTab = "system";

  /** */
  String forwardContactsTab = "contacts";

  /** */
  String forwardLocationsTab = "locations";

  /** */
  String forwardCategoriesTab = "categories";

  /** */
  String forwardHomePage = "homePage";

  /** */
  String forwardHelpPage = "helpPage";

  /** */
  String forwardLoggedOut = "loggedOut";
}
