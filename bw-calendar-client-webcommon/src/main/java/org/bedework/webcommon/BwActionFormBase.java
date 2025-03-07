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

import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.EventKey;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.struts.UtilActionForm;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import java.util.Collection;
import java.util.Set;

/** Base for action form used by bedework web applications
 *
 * @author  Mike Douglass     douglm - rpi.edu
 */
public class BwActionFormBase extends UtilActionForm
        implements Logged, BedeworkDefs {
  private String requestedUid;

  private boolean markDeleted;

  private BwFilterDef currentFilter;

  /* ....................................................................
   *           Event date and time fields
   * .................................................................... */

  private EventDates eventDates;

  //private TimeDateComponents rdate;

  private Collection<RecurRuleComponents> rruleComponents;

  /* ....................................................................
   *           Ids for fetching objects
   * .................................................................... */

  private EventKey eventKey;

  /* ..............................................................
   *           Fields for creating or editing objects
   * .............................................................. */

   private EventState eventState;

  /** Formatter for the current event
   */
  private EventFormatter curEventFmt;

  private EventInfo eventInfo;

  /** event is where we hold a Event object for adding/editing/displaying
   */
  protected BwEvent event;

  private String eventStatus;

  /** True if we should list all events
   */
  private boolean listAllEvents;

  /* ..............................................................
   *                       Uploads and exports
   * .............................................................. */

  private String eventRegAdminToken;
  private String eventRegWsUrl;

  /* ....................................................................
   *                       Calendars
   * .................................................................... */

  private BwCalendar calendar;

  private Set<String> calendarsOpenState;

  /* current calendar - should probably be the same as below. */
  private String calPath;

  private String calendarPath;

  /** True if we are adding a new calendar
   */
  private boolean addingCalendar;

  /* ..............................................................
   *                   Preferences
   * .............................................................. */

  private BwPreferences userPreferences;

  private UpdateFromTimeZonesInfo updateFromTimeZonesInfo;

  private String calendarUserAddress;

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /** This allows us to add "calForm." to form fields. For Struts 1
   * the actual form is specified in the action mapping. In struts 2
   * every action is the bean.
   *
   * This allows modifying the jsp and xsl ahead of switching to
   * struts 2.
   *
   * @return this form
   */
  public BwActionFormBase getCalForm() {
    return this;
  }

  /** One shot from a request.
   *
   * @param val
   */
  @StrutsParameter
  public void setRequestedUid(final String val) {
    requestedUid = val;
  }

  /**
   * @return uid or null
   */
  public String getRequestedUid() {
    final String uid = requestedUid;
    requestedUid = null; // Only one go
    return uid;
  }

  /**
   * @param val
   */
  public void setCurrentFilter(final BwFilterDef val) {
    currentFilter = val;
  }

  /**
   * @return BwFilterDef
   */
  public BwFilterDef getCurrentFilter() {
    return currentFilter;
  }

  /**
   * @param val the updateFromTimeZonesInfo to set
   */
  public void setUpdateFromTimeZonesInfo(final UpdateFromTimeZonesInfo val) {
    updateFromTimeZonesInfo = val;
  }

  /**
   * @return the updateFromTimeZonesInfo
   */
  public UpdateFromTimeZonesInfo getUpdateFromTimeZonesInfo() {
    return updateFromTimeZonesInfo;
  }

  /* ====================================================================
   *                   Admin groups
   * ==================================================================== */

  /**
   * @param val token for event registration
   */
  public void assignEventRegAdminToken(final String val) {
    eventRegAdminToken = val;
  }

  /**
   * @return token for event registration
   */
  public String getEventRegAdminToken() {
    return eventRegAdminToken;
  }

  public void assignEventRegWsUrl(final String val) {
    eventRegWsUrl = val;
  }

  public String getEventRegWsUrl() {
    return eventRegWsUrl;
  }

  public void assignMarkDeleted(final boolean val) {
    markDeleted = val;
  }

  public boolean getMarkDeleted() {
    return markDeleted;
  }

  public void assignCalendarUserAddress(final String val) {
    calendarUserAddress = val;
  }


  /**
   * @return current users calendar user address.
   */
  public String getCalendarUserAddress() {
    return calendarUserAddress;
  }

  /* ====================================================================
   *                   View Period methods
   * ==================================================================== */

  /**
   * @return names
   */
  public String[] getViewTypeNames() {
    return BedeworkDefs.viewPeriodNames;
  }

  /**
   * @param i
   * @return view name
   */
  public String getViewTypeName(final int i) {
    return BedeworkDefs.viewPeriodNames[i];
  }

  /* ====================================================================
   *                   Calendars
   * ==================================================================== */

  /**
   * @param val Set of String
   */
  @StrutsParameter
  public void setCalendarsOpenState(final Set<String> val) {
    calendarsOpenState = val;
  }

  /**
   * @return Set of String
   */
  public Set<String> getCalendarsOpenState() {
    return calendarsOpenState;
  }

  /** Save the Path of a calendar
   *
   * @param val the Path of a calendar
   */
  @StrutsParameter
  public void setCalPath(final String val) {
    calPath = val;
  }

  /**
   * @return cal Path
   */
  public String getCalPath() {
    return calPath;
  }

  /** Save the Path of a calendar
   *
   * @param val the Path of a calendar
   */
  @StrutsParameter
  public void setCalendarPath(final String val) {
    calendarPath = val;
  }

  /**
   * @return cal Path
   */
  public String getCalendarPath() {
    return calendarPath;
  }

  /** Not set - invisible to jsp
   *
   * @param val true for adding
   */
  public void assignAddingCalendar(final boolean val) {
    addingCalendar = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddingCalendar() {
    return addingCalendar;
  }

  /**
   * @param val
   */
  public void setCalendar(final BwCalendar val) {
    calendar = val;
  }

  /** If a calendar object exists, return that otherwise create an empty one.
   *
   * @return BwCalendar  populated calendar value object
   */
  public BwCalendar getCalendar() {
    if (calendar == null) {
      calendar = new BwCalendar();
    }

    return calendar;
  }

  /* ====================================================================
   *                   preferences
   * ==================================================================== */

  /** Set preferences for a given user
   *
   * @param  val   prefernces
   */
  public void setUserPreferences(final BwPreferences val) {
    userPreferences = val;
  }

  /** Set preferences for a given user
   *
   * @return  val   prefernces
   */
  public BwPreferences getUserPreferences() {
    return userPreferences;
  }

  /* ====================================================================
   *                   Events
   * ==================================================================== */

   public EventState getEventState() {
    if (eventState == null){
      eventState = new EventState(this);
    }
    return eventState;
  }

  /**
   * @param val
   */
  public void setCurEventFmt(final EventFormatter val) {
    curEventFmt = val;
  }

  /**
   * @return event formatter
   */
  public EventFormatter getCurEventFmt() {
    return curEventFmt;
  }

  /**
   * @param val
   */
  public void setListAllEvents(final boolean val) {
    listAllEvents = val;
  }

  /**
   * @return bool
   */
  public boolean getListAllEvents() {
    return listAllEvents;
  }

  /** Set an object containing the dates.
   *
   * @param val object representing date/times and duration
   */
  public void assignEventDates(final EventDates val) {
    eventDates = val;
  }

  /** Return an object containing the dates.
   *
   * @return EventDates  object representing date/times and duration
   */
  public EventDates getEventDates() {
    return eventDates;
  }

  /** Event key for next action
   *
   * @param val
   */
  public void setEventKey(final EventKey val) {
    eventKey = val;
  }

  /**
   * @return EventKey
   */
  public EventKey getEventKey() {
    return eventKey;
  }

  /**
   * @param val
   */
  public void setEventInfo(final EventInfo val,
                           final boolean newEvent) {
    event = val.getEvent();
    eventInfo = val;

    if (!newEvent) {
      getEventDates().setFromEvent(event);
    }
  }

  /**
   * @return EventInfo
   */
  public EventInfo getEventInfo() {
    return eventInfo;
  }

  /**
   * @return event
   */
  public BwEvent getEvent() {
    return event;
  }

  /** Return an initialised TimeDateComponents representing now
   *
   * @return TimeDateComponents  initialised object
   */
  public TimeDateComponents getNowTimeComponents() {
    return getEventDates().getNowTimeComponents();
  }

  /**
   * @param val
   */
  @StrutsParameter
  public void setEventStatus(final String val) {
    eventStatus = val;
  }

  /**
   * @return String eventStatus for event
   */
  public String getEventStatus() {
    return eventStatus;
  }

  /* ====================================================================
   *             Start and end Date and time and duration
   *
   * Methods related to selecting a particular date. These values may be
   * used when setting the current date or when setting the date of an event.
   * They will be distinguished by the action called.
   * ==================================================================== */

  /**
   * @param val Collection of RecurRuleComponents
   */
  @StrutsParameter
  public void setRruleComponents(final Collection<RecurRuleComponents> val) {
    rruleComponents = val;
  }

  /**
   * @return Collection of RecurRuleComponents
   */
  public Collection<RecurRuleComponents> getRruleComponents() {
    return rruleComponents;
  }

  /** Return an object representing an events start date.
   *
   * @return TimeDateComponents  object representing date and time
   */
  public TimeDateComponents getEventStartDate() {
    return getEventDates().getStartDate();
  }

  /** Return an object representing an events end date.
   *
   * @return TimeDateComponents  object representing date and time
   */
  public TimeDateComponents getEventEndDate() {
    return getEventDates().getEndDate();
  }

  /** Return an object representing an events duration.
   *
   * @return Duration  object representing date and time
   */
  public DurationBean getEventDuration() {
//    return getEventDates().getDuration();
    final DurationBean db = getEventDates().getDuration();
    if (debug()) {
      debug("Event duration=" + db);
    }

    return db;
  }

  /**
   * @param val event end type
   */
  @StrutsParameter
  public void setEventEndType(final String val) {
    getEventDates().setEndType(val);
  }

  /**
   * @return event end type
   */
  public String getEventEndType() {
    return getEventDates().getEndType();
  }

  @Override
  public void reset(final HttpServletRequest request) {
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
