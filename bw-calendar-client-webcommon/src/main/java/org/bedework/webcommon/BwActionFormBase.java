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
import org.bedework.appcommon.CalendarInfo;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.EventKey;
import org.bedework.appcommon.InOutBoxInfo;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.convert.RecurRuleComponents;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.struts.UtilActionForm;
import org.bedework.util.timezones.TimeZoneName;
import org.bedework.util.timezones.Timezones;

import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

/** Base for action form used by bedework web applications
 *
 * @author  Mike Douglass     douglm - rpi.edu
 */
public class BwActionFormBase extends UtilActionForm
        implements Logged, BedeworkDefs {
  private final Map<String, BwModule> modules = new ConcurrentHashMap<>();

  private DateTimeFormatter today;

  private BwSynchInfo synchInfo;

  private CalendarInfo calInfo;

  /** This object will be set up appropriately for the kind of client,
   * e.g. admin, guest etc.
   */
  private ConfigCommon config;

  private BwPrincipal adminUserId;

  private Locale requestedLocale;

  private String requestedUid;

  /* This should be a cloned copy only */
  private AuthProperties authpars;

  /* This should be a cloned copy only */
  private DirectoryInfo dirInfo;

  /* Transient result that only needs to stick around till the next action URL */
  private Object lastResult;

  private boolean newSession;

  private BwSession sess;

  private boolean markDeleted;

  /** true if this is a guest (unauthenticated) user
   */
  private boolean guest;

  /**
   * The current administrative user.
   */
  protected String currentAdminUser;

  private boolean superUser;

  //private String appType;

  /** true if we are showing the public face
   */
  private boolean publicView;

  private String[] yearVals;
  private static final int numYearVals = 10;

  /** Whether we show year data
   */
  private boolean showYearData;

  /** Auth prefs for the currently logged in user
   */
  private BwAuthUserPrefs curAuthUserPrefs;

  /* Settings for current authenticated user */
  private boolean curUserAlerts;
  private boolean curUserPublicEvents;
  private boolean curUserContentAdminUser;
  private boolean curUserApproverUser;

  private BwFilterDef currentFilter;

  /* ..............................................................
   *                       Calendar suites
   * .............................................................. */

  private BwCalSuiteWrapper currentCalSuite;

  private String calSuiteName;

  /** The groups of which our user is a member
   */
  private Collection<BwGroup> currentGroups;

  /* ....................................................................
   *           Event date and time fields
   * .................................................................... */

  private EventDates eventDates;

  private boolean hour24;

  private String endDateType;

  private int minIncrement;

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

  private String imageUploadDirectory;

  private String eventRegAdminToken;

  /* ..............................................................
   *                   Application state
   *  ............................................................. */


  /** True if we are adding an alert
   */
  private boolean alertEvent;

  private boolean userMaintOK;

  /* ....................................................................
   *                   Timezones
   * .................................................................... */

  private boolean uploadingTimeZones;

  /* ....................................................................
   *                       Views
   * .................................................................... */

  private BwView view;

  private String viewName;

  private boolean addingView;

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

  private BwCalendar meetingCal;

  /* ....................................................................
   *                   Preferences
   * .................................................................... */

  /** Last email address used to mail message. By default set to
   * preferences value.
   */
  private String lastEmail;

  private String lastSubject;

  private BwPreferences preferences;

  private BwPreferences userPreferences;

  /* ....................................................................
   *                   public events submission
   * .................................................................... */

  private String snfrom;

  private String sntext;

  private String snsubject;

  /* ....................................................................
   *                   Notifications, Inbox
   * .................................................................... */

  private NotificationInfo notificationInfo;

  private InOutBoxInfo inBoxInfo;

  private UpdateFromTimeZonesInfo updateFromTimeZonesInfo;

  private boolean reloadRequired;
  private String calendarUserAddress;

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /* ....................................................................
   *                       Modules
   * .................................................................... */

  public BwModule newModule(final String name) {
    return new BwModule(name);
  }

  public void setModule(final String name, final BwModule module) {
    modules.put(name, module);
  }

  public synchronized  BwModule fetchModule(final String name) {
    String n = name;

    if (n == null) {
      n = BwModule.defaultModuleName;
    }

    BwModule m = modules.get(n);

    if (m == null) {
      m = newModule(n);

      /* clone the client from any active module */
      if (modules.size() > 0) {
        for (final BwModule from: modules.values()) {
          if (from.getClient() != null) {
            m.setClient(from.getClient().copy(m.getModuleName()));
          }
        }
      }
      modules.put(n, m);
    }

    return m;
  }

  /** Called when we change the default client state enough to need
   * to ditch the other clients.
   */
  public void flushModules(String name) {
    String n = name;

    if (n == null) {
      n = BwModule.defaultModuleName;
    }

    ArrayList<String> mnames = new ArrayList<>(modules.keySet());

    for (String s: mnames) {
      if (s.equals(n)) {
        continue;
      }

      modules.remove(s);
    }
  }

  /**
   * @param val
   */
  public void setSynchInfo(final BwSynchInfo val) {
    synchInfo = val;
  }

  /**
   * @return info or null
   */
  public BwSynchInfo getSynchInfo() {
    return synchInfo;
  }

  /** null if we are going with defaults or browser selection.
   *
   * @param val
   */
  public void setRequestedLocale(final Locale val) {
    requestedLocale = val;
  }

  /**
   * @return locale or null
   */
  public Locale getRequestedLocale() {
    return requestedLocale;
  }

  /** One shot from a request.
   *
   * @param val
   */
  public void setRequestedUid(final String val) {
    requestedUid = val;
  }

  /**
   * @return uid or null
   */
  public String getRequestedUid() {
    String uid = requestedUid;
    requestedUid = null; // Only one go
    return uid;
  }

  /** Result of last action - only needed for rendering till another action changes it
   *
   * @param val
   */
  public void setLastResult(final Object val) {
    lastResult = val;
  }

  /**
   * @return locale or null
   */
  public Object getLastResult() {
    return lastResult;
  }

  /**
   * @param val
   */
  public void assignReloadRequired(final boolean val) {
    reloadRequired = val;
  }

  /**
   * @return boolean
   */
  public boolean getReloadRequired() {
    return reloadRequired;
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
   * @return default timezoneid
   */
  public String getDefaultTzid() {
    return Timezones.getThreadDefaultTzid();
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

  /**
   * @param val
   */
  public void setDirInfo(final DirectoryInfo val) {
    dirInfo = val;
  }

  /**
   * @return DirectoryInfo
   */
  public DirectoryInfo getDirInfo() {
    return dirInfo;
  }

  /* ====================================================================
   *                   Timezones
   * ==================================================================== */

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignUploadingTimezones(final boolean val) {
    uploadingTimeZones = val;
  }

  /**
   * @return true if uploading timezones
   */
  public boolean getUploadingTimezones() {
    return uploadingTimeZones;
  }

  /** Get a list of system timezones
   *
   * @return Collection of timezone names
   */
  public Collection<TimeZoneName> getSystemTimeZoneNames() {
    Collection<TimeZoneName> nms = new TreeSet<TimeZoneName>();

    try {
      nms.addAll(Timezones.getTzNames());
    } catch (Throwable t) {
      getErr().emit(t);
    }

    return nms;
  }

  /** Get a list of system and user timezones
   *
   * @return Collection of timezone names
   */
  public Collection<TimeZoneName> getTimeZoneNames() {
    try {
      return Timezones.getTzNames();
    } catch (Throwable t) {
      getErr().emit(t);
      return new TreeSet<TimeZoneName>();
    }
  }

  /* ====================================================================
   *                   Events
   * ==================================================================== */

  /** Not set - invisible to jsp
   */
  /**
   * @param val
   */
  public void assignAlertEvent(final boolean val) {
    alertEvent = val;
  }

  /**
   * @return bool
   */
  public boolean getAlertEvent() {
    return alertEvent;
  }

  /* ==============================================================
   *                   Authorised user maintenance
   * ============================================================== */

  /**
   * @param val
   */
  public void assignUserMaintOK(final boolean val) {
    userMaintOK = val;
  }

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * @return boolean    true if user maintenance is implemented.
   */
  public boolean getUserMaintOK() {
    return userMaintOK;
  }

  /* ====================================================================
   *                   Admin groups
   * ==================================================================== */

  /**
   * @val token for event registration
   */
  public void setEventRegAdminToken(String val) {
    eventRegAdminToken = val;
  }

  /**
   * @return token for event registration
   */
  public String getEventRegAdminToken() {
    return eventRegAdminToken;
  }

  public void setAuthPars(AuthProperties val) {
    authpars = val;
  }

  /**
   * @return SystemProperties object
   */
  public AuthProperties getAuthpars() {
    return authpars;
  }

  /** Set a copy of the config parameters
   *
   * @param val
   */
  public void setConfig(final ConfigCommon val) {
    config = val;

    /* Set defaults */
    setMinIncrement(config.getMinIncrement());
    assignShowYearData(config.getShowYearData());
  }

  /** Return a cloned copy of the config parameters
   *
   * @return Config object
   */
  public ConfigCommon getConfig() {
    return config;
  }

  /** True if we have a config object set.
   *
   * @return boolean
   */
  public boolean configSet() {
    return config != null;
  }
  /**
   * @param val
   */
  public void assignAdminUserPrincipal(final BwPrincipal val) {
    adminUserId = val;
  }

  /**
   * @return admin userid
   */
  public String getAdminUserId() {
    return adminUserId.getAccount();
  }

  /* ====================================================================
   *                   Current authenticated user Methods
   * DO NOT set with setXXX. Use assign
   * ==================================================================== */

  /**
   * @param val
   */
  public void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
    curAuthUserPrefs = val;
  }

  /**
   * @return auth user prefs
   */
  public BwAuthUserPrefs getCurAuthUserPrefs() {
    return curAuthUserPrefs;
  }

  /** Current user rights
   *
   * @param val
   */
  public void assignCurUserAlerts(final boolean val) {
    curUserAlerts = val;
  }

  /** Current user rights
  *
   * @return alerts
   */
  public boolean getCurUserAlerts() {
    return curUserAlerts;
  }

  /** Current user rights
   *
   * @param val
   */
  public void assignCurUserPublicEvents(final boolean val) {
    curUserPublicEvents = val;
  }

  /** Current user rights
  *
   * @return true for user who can edit public events
   */
  public boolean getCurUserPublicEvents() {
    return curUserPublicEvents;
  }

  /** True for contentAdminUser
   *
   * @param val boolean
   */
  public void assignCurUserContentAdminUser(final boolean val) {
    curUserContentAdminUser = val;
  }

  /** True for contentAdminUser
   *
   * @return boolean
   */
  public boolean getCurUserContentAdminUser() {
    return curUserContentAdminUser;
  }

  /** True for approver
   *
   * @param val boolean
   */
  public void assignCurUserApproverUser(final boolean val) {
    curUserApproverUser = val;
  }

  /** True for approver
   *
   * @return boolean
   */
  public boolean getCurUserApproverUser() {
    return curUserApproverUser;
  }

  /**
   * @param val
   */
  public void assignCurUserSuperUser(final boolean val) {
    superUser = val;
  }

  /**
  *
   * @return true for superuser
   */
  public boolean getCurUserSuperUser() {
    return superUser;
  }

  /** apptype
   *
   * @return boolean
   */
  public String getAppType() {
    return config.getAppType();
  }

  /** True for submitApp
   *
   * @return boolean
   */
  public boolean getSubmitApp() {
    return BedeworkDefs.appTypeWebsubmit.equals(getAppType());
  }

  public void assignMarkDeleted(final boolean val) {
    markDeleted = val;
  }

  public boolean getMarkDeleted() {
    return markDeleted;
  }

  /* ==============================================================
   *                   Calendar suites
   * ============================================================== */

  /** Current calSuite for the application
   *
   * @param val
   */
  public void setCurrentCalSuite(final BwCalSuiteWrapper val) {
    currentCalSuite = val;
  }

  /**
   * @return BwCalSuiteWrapper
   */
  public BwCalSuiteWrapper getCurrentCalSuite() {
    return currentCalSuite;
  }

  /** Name of current CalSuite.
   *
   * @param val
   */
  public void setCalSuiteName(final String val) {
    calSuiteName = val;
  }

  /** name of current CalSuite.
   *
   * @return String
   */
  public String getCalSuiteName() {
    return calSuiteName;
  }

  /* ==============================================================
   *                   groups
   * ============================================================== */

  /** The groups of which our user is a member
   *
   * @param val
   */
  public void setCurrentGroups(final Collection<BwGroup> val) {
    currentGroups = val;
  }

  /**
   * @return user admin groups
   */
  public Collection<BwGroup> getCurrentGroups() {
    return currentGroups;
  }

  public void assignCalendarUserAddress(String val) {
    calendarUserAddress = val;
  }


  /**
   * @return current users calendar user address.
   */
  public String getCalendarUserAddress() {
    return calendarUserAddress;
  }

  /**
   * @param val true for new session
   */
  public void assignNewSession(final boolean val) {
    newSession = val;
  }

  /**
   * @return boolean  true for new session
   */
  public boolean getNewSession() {
    return newSession;
  }

  /** This should not be setCurrentAdminUser as that exposes it to the incoming
   * request. This holds whatever account we are running as. We may be running
   * as something other than the authenticated account - e.g. public admin
   * of a calendar suite. We need this to hold that cvalue as we may
   * not have a client embedded on entry.
   *
   * @param val      String user id
   */
  public void assignCurrentAdminUser(final String val) {
    currentAdminUser = val;
  }

  /**
   * @return admin user id
   */
  public String fetchCurrentAdminUser() {
    return currentAdminUser;
  }

  /**
   * @param name - the client name or null
   * @return a named client object
   */
  public Client fetchClient(final String name) {
    BwModule m = fetchModule(name);

    if (m == null) {
      return null;
    }

    return m.getClient();
  }

  /**
   * @return DateTimeFormatter today
   */
  public DateTimeFormatter getToday() {
    if (today != null) {
      return today;
    }

      today = new DateTimeFormatter(
              BwDateTimeUtil.getDateTime(
                      new Date(System.currentTimeMillis())));

    return today;
  }

  /**
   * @param val true for guest
   */
  public void setGuest(final boolean val) {
    guest = val;
  }

  /**
   * @return true for guest
   */
  public boolean getGuest() {
    return guest;
  }

  /**
   * @param val
   */
  public void setPublicView(final boolean val) {
    publicView = val;
  }

  /**
   * @return bool
   */
  public boolean getPublicView() {
    return publicView;
  }

  /**
   * @param val
   */
  public void assignShowYearData(final boolean val) {
    showYearData = val;
  }

  /**
   * @return bool
   */
  public boolean getShowYearData() {
    return showYearData;
  }

  /**
   * @param val
   */
  public void assignImageUploadDirectory(final String val) {
    imageUploadDirectory = val;
  }

  /**
   * @return path or null
   */
  public String getImageUploadDirectory() {
    return imageUploadDirectory;
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
   *                   Views
   * ==================================================================== */

  /** Set the view name for fetch
   *
   * @param val    String name
   */
  public void setViewName(final String val) {
    viewName = val;
  }

  /** Get the view name
   *
   * @return String name
   */
  public String getViewName() {
    return viewName;
  }

  /** Set the view we are editing
   *
   * @param val    BwView  object or null
   */
  public void setView(final BwView val) {
    view = val;
  }

  /** Get the view we are editing
   *
   * @return BwView  object
   */
  public BwView getView() {
    if (view == null) {
      view = new BwView();
    }

    return view;
  }

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingView(final boolean val) {
    addingView = val;
  }

  /**
   * @return bool
   */
  public boolean getAddingView() {
    return addingView;
  }

  /* ====================================================================
   *                   Calendars
   * ==================================================================== */

  /**
   * @param val Set of String
   */
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

  /** Calendar containing copy of current in/outbox event.
   * @param val
   */
  public void setMeetingCal(final BwCalendar val) {
    meetingCal = val;
  }

  /** Calendar containing copy of current in/outbox event.
   *
   * @return BwCalendar
   */
  public BwCalendar getMeetingCal() {
    return meetingCal;
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
   * @return EventDates  object representing date/times and duration
   */
  public void assignEventDates(EventDates val) {
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
    try {
      event = val.getEvent();
      eventInfo = val;

      if (!newEvent) {
        getEventDates().setFromEvent(event);
      }
    } catch (final Throwable t) {
      err.emit(t);
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

  /* * Return an object containing the rdate.
   *
   * @return TimeDateComponents  for rdate
   * /
  public TimeDateComponents getRdate() {
    if (rdate == null) {
      try {
        rdate = new TimeDateComponents(getCalInfo(),
                                       config.getMinIncrement(),
                                       getHour24());
      } catch (Throwable t) {
        getErr().emit(t);
      }
    }

    return rdate;
  } */

  /**
   * @param val Collection of RecurRuleComponents
   */
  public void setRruleComponents(final Collection<RecurRuleComponents> val) {
    rruleComponents = val;
  }

  /**
   * @return Collection of RecurRuleComponents
   */
  public Collection<RecurRuleComponents> getRruleComponents() {
    return rruleComponents;
  }

  /**
   * @param val
   */
  public void setHour24(final boolean val) {
    hour24 = val;
    eventDates = null;   // reset it
  }

  /**
   * @return bool
   */
  public boolean getHour24() {
    return hour24;
  }

  /**
   * @param val
   */
  public void setEndDateType(final String val) {
    endDateType = val;
    eventDates = null;   // reset it
  }

  /**
   * @return bool
   */
  public String getEndDateType() {
    return endDateType;
  }

  /**
   * @param val
   */
  public void setMinIncrement(final int val) {
    minIncrement = val;
    eventDates = null;   // reset it
  }

  /**
   * @return int
   */
  public int getMinIncrement() {
    return minIncrement;
  }

  /**
   * @param val
   */
  public void setNotificationInfo(final NotificationInfo val) {
    notificationInfo = val;
  }

  /**
   * @return NotificationInfo
   */
  public NotificationInfo getNotificationInfo() {
    return notificationInfo;
  }

  /**
   * @param val
   */
  public void setInBoxInfo(final InOutBoxInfo val) {
    inBoxInfo = val;
  }

  /**
   * @return InOutBoxInfo
   */
  public InOutBoxInfo getInBoxInfo() {
    return inBoxInfo;
  }

  /* ....................................................................
   *                   public events submission
   * .................................................................... */

  /** Set submission notification from
   *
   * @param val
   */
  public void setSnfrom(final String val) {
    snfrom = val;
  }

  /** Get submission notification from
   *
   * @return submission notification from
   */
  public String getSnfrom() {
    return snfrom;
  }

  /** Set submission notification text
   *
   * @param val
   */
  public void setSntext(final String val) {
    sntext = val;
  }

  /** Get submission notification text
   *
   * @return submission notification text
   */
  public String getSntext() {
    return sntext;
  }

  /** Set submission notification subject
   *
   * @param val
   */
  public void setSnsubject(final String val) {
    snsubject = val;
  }

  /** Get submission notification subject
   *
   * @return submission notification subject
   */
  public String getSnsubject() {
    return snsubject;
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
    DurationBean db = getEventDates().getDuration();
    if (debug()) {
      debug("Event duration=" + db);
    }

    return db;
  }

  /**
   * @param val
   */
  public void setEventEndType(final String val) {
    getEventDates().setEndType(val);
  }

  /**
   * @return event end type
   */
  public String getEventEndType() {
    return getEventDates().getEndType();
  }

  /* ====================================================================
   *                Date and time labels for select boxes
   * ==================================================================== */

  /**
   * @return year values
   */
  public String[] getYearVals() {
    if (yearVals == null) {
      yearVals = new String[numYearVals];
      int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
      //curYear = String.valueOf(year);

      for (int i = 0; i < numYearVals; i++) {
        yearVals[i] = String.valueOf(year + i);
      }
    }

    return yearVals;
  }

  /**
   * @param val
   */
  public void setSession(final BwSession val) {
    sess = val;
  }

  /**
   * @return session
   */
  public BwSession getSession() {
    return  sess;
  }

  /**
   * Reset properties to their default values.
   *
   * @param mapping The mapping used to select this instance
   * @param request The servlet request we are processing
   */
  @Override
  public void reset(final ActionMapping mapping,
                    final HttpServletRequest request) {
    today = null;

    //key.reset();
  }

  /* ====================================================================
   *                Private methods
   * ==================================================================== */

  public boolean publicAdmin() {
    try {
      return getConfig().getPublicAdmin();
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
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
