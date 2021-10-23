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
  public static final int forwardSuccess = 0;
  /** */
  public static final int forwardContinue = 1;
  /** */
  public static final int forwardRetry = 2;

  /** */
  public static final int forwardError = 3;
  /** */
  public static final int forwardNoAccess = 4;

  /** */
  public static final int forwardNotFound = 5;

  /** */
  public static final int forwardNoSuchView = 6;

  /** */
  public static final int forwardNoViewDefined = 7;

  /** Set when an optional parameter is not found */
  public static final int forwardNoParameter = 8;

  /** Set when no action was taken */
  public static final int forwardNoAction = 9;

  /** Something is referenced and cannot be removed */
  public static final int forwardReffed = 10;

  /** an object was added/updated */
  public static final int forwardAdded = 11;
  /** */
  public static final int forwardUpdated = 12;

  /** */
  public static final int forwardChooseGroup = 13;

  /** */
  public static final int forwardNoGroupAssigned = 14;

  /** */
  public static final int forwardEdit = 15;

  /** */
  public static final int forwardDuplicate = 16;

  /** */
  public static final int forwardValidationError = 17;

  /** */
  public static final int forwardDelete = 18;

  /** */
  public static final int forwardCopy = 19;

  /** */
  public static final int forwardFailed = 20;

  /** */
  public static final int forwardGotomain = 21;

  /** */
  public static final int forwardBadValue = 22;

  /** */
  public static final int forwardCalendarNotFound = 23;

  /** */
  public static final int forwardEventNotFound = 24;

  /** */
  public static final int forwardBadDate = 25;

  /** */
  public static final int forwardNotAllowed = 26;

  /** */
  public static final int forwardInUse = 27;

  /** */
  public static final int forwardNotAdded = 28;

  /** */
  public static final int forwardBadData = 29;

  /** */
  public static final int forwardReferenced = 30;

  /** */
  public static final int forwardBadPref = 31;

  /** */
  public static final int forwardCancelled = 32;

  /** */
  public static final int forwardBadRequest = 33;

  /** */
  public static final int forwardNochange = 34;

  /** */
  public static final int forwardDone = 35;

  /** */
  public static final int forwardExport = 36;

  /** */
  public static final int forwardStateSaved = 37;

  /** */
  public static final int forwardUpdateEventAccess = 38;

  /** */
  public static final int forwardEditEventAttendees = 39;

  /** */
  public static final int forwardEventIntoMeeting = 40;

  /** */
  public static final int forwardEventDatesInited = 41;

  /** */
  public static final int forwardDeclined = 42;

  /** */
  public static final int forwardNoMeeting = 43;

  /** */
  public static final int forwardListAttendees = 44;

  /** */
  public static final int forwardListEvents = 45;

  /** */
  public static final int forwardList = 46;

  /** */
  public static final int forwardNull = 47;

  /** */
  public static final String[] forwards = {
    "success",
    "continue",
    "retry",
    "error",
    "noAccess",
    "notFound",  // 5
    "noSuchView",
    "noViewDef",
    "noParameter",
    "noAction",
    "reffed", // 10
    "added",
    "updated",
    "chooseGroup",
    "noGroupAssigned",
    "edit", //15
    "duplicate",
    "validationError",
    "delete",
    "copy",
    "failed",  //20
    "gotomain",
    "badValue",
    "calendarNotFound",
    "eventNotFound",
    "badDate", // 25
    "notAllowed",
    "inUse",
    "notAdded",
    "baddata",
    "referenced", //30
    "retry",   // was badPref
    "cancelled",
    "badRequest",
    "nochange",
    "done",       //35
    "export",
    "stateSaved",
    "updateEventAccess",
    "editEventAttendees",
    "makeEventIntoMeeting",  //40
    "eventDatesInited",
    "declined",
    "noMeeting",
    "listAttendees",
    "listEvents",     //45
    "list",
    "FORWARD-NULL",
  };
}
