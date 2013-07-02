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
import org.bedework.appcommon.CheckData;
import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.appcommon.DayView;
import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.FormattedEvents;
import org.bedework.appcommon.InOutBoxInfo;
import org.bedework.appcommon.MonthView;
import org.bedework.appcommon.MyCalendarVO;
import org.bedework.appcommon.NotificationInfo;
import org.bedework.appcommon.SelectId;
import org.bedework.appcommon.TimeView;
import org.bedework.appcommon.WeekView;
import org.bedework.appcommon.YearView;
import org.bedework.appcommon.client.Client;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwAuthUser;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventObj;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPreferences;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwStats.StatsEntry;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.SubContext;
import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.mail.MailerIntf;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.UserAuth;
import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.SchedulingI.FbResponses;
import org.bedework.calsvci.SysparsI;
import org.bedework.icalendar.RecurRuleComponents;
import org.bedework.webcommon.config.ConfigCommon;
import org.bedework.webcommon.search.SearchResultEntry;

import edu.rpi.cmt.timezones.TimeZoneName;
import edu.rpi.cmt.timezones.Timezones;
import edu.rpi.sss.util.Util;
import edu.rpi.sss.util.jsp.UtilActionForm;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;


/** Base for action form used by bedework web applications
 *
 * @author  Mike Douglass     douglm - rpi.edu
 */
public class BwActionFormBase extends UtilActionForm implements BedeworkDefs {
  private DateTimeFormatter today;

  private BwSynchInfo synchInfo;

  private CalendarInfo calInfo;

  private Collection<EventPropertiesReference> propRefs;

  /** This object will be set up appropriately for the kind of client,
   * e.g. admin, guest etc.
   */
  private ConfigCommon config;

  private Locale requestedLocale;

  private transient CollectionCollator<BwCalendar> calendarCollator;
  private transient CollectionCollator<BwContact> contactCollator;
  private transient CollectionCollator<BwCategory> categoryCollator;
  private transient CollectionCollator<BwLocation> locationCollator;

  /* This should be a cloned copy only */
  private SystemProperties syspars;

  /* This should be a cloned copy only */
  private DirectoryInfo dirInfo;

  /* Transient result that only needs to stick around till the next action URL */
  private Object lastResult;

  private Collection<StatsEntry> sysStats;

  private transient MailerIntf mailer;

  /* Kind of entity we are referring to */

  private static int ownersEntity = 1;
  private static int editableEntity = 2;

  private boolean newSession;

  private BwSession sess;

  private BwPrincipal userVO;

  private long timeIn;

  /** true if this is a guest (unauthenticated) user
   */
  private boolean guest;

  /**
   * The current administrative user.
   */
  protected String currentAdminUser;

  /** True if this user has more than the default rights
   */
  private boolean authorisedUser;

  private String appType;

  private boolean submitApp;

  /** true if we are showing the public face
   */
  private boolean publicView;

  private CalSvcI calsvci;
  private Client client;

  private String[] yearVals;
  private static final int numYearVals = 10;
  //private String curYear;

  /** Whether we show year data
   */
  private boolean showYearData;

  /** Id doing administration, May be a group id */
  private String adminUserId;

  /** Auth prefs for the currently logged in user
   */
  private BwAuthUserPrefs curAuthUserPrefs;

  /* Settings for current authenticated user */
  private boolean curUserAlerts;
  private boolean curUserPublicEvents;
  private boolean curUserContentAdminUser;

  private BwFilterDef currentFilter;

  private Collection<BwFilterDef> filters;

  /* ....................................................................
   *                   Alarm fields
   * .................................................................... */

  /* The trigger is a date/time or a duration.
   */

  private TimeDateComponents triggerDateTime;

  private DurationBean triggerDuration;

  /** Specified trigger is relative to the start of event or todo, otherwise
   * it's the end.
   */
  private boolean alarmRelStart = true;

  private DurationBean alarmDuration;

  private int alarmRepeatCount;

  private boolean alarmTriggerByDate;

  /* ....................................................................
   *                       Calendar suites
   * .................................................................... */

  private BwCalSuiteWrapper currentCalSuite;

  private String calSuiteName;

  private BwCalSuiteWrapper calSuite;

  private boolean addingCalSuite;

  private String resourceName;

  private String resourceClass;

  private boolean addingResource;

  private CalSuiteResource calSuiteResource;

  private List<CalSuiteResource> calSuiteResources;

  /* ....................................................................
   *                       Admin Groups
   * .................................................................... */

  /** True if we have set the user's group.
   */
  private boolean groupSet;

  /** True if user is in only one group
   */
  private boolean oneGroup;

  /** True if we are choosing the user's group.
   */
  private boolean choosingGroup;

  /** User's current group or null. */
  private String adminGroupName;

  /** The groups of which our user is a member
   */
  private Collection<BwGroup> userAdminGroups;

  /* The list of admin groups displayed for the use of the user client
   */
  private static Collection<BwGroup> adminGroupsInfo;

  private static long lastAdminGroupsInfoRefresh;
  private static long adminGroupsInfoRefreshInterval = 1000 * 60 * 5;

  private static volatile Object locker = new Object();

  /* ....................................................................
   *           Event date and time fields
   * .................................................................... */

  private EventDates eventDates;

  private boolean hour24;

  private String endDateType;

  private int minIncrement;

  private TimeDateComponents rdate;

  private Collection<RecurRuleComponents> rruleComponents;

  /* ....................................................................
   *           Ids for fetching objects
   * .................................................................... */

  private EventKey eventKey;

  private EventListPars eventListPars;

  /* ....................................................................
   *           Fields for creating or editing objects
   * .................................................................... */

  /** Formatter for the current event
   */
  private EventFormatter curEventFmt;

  /** event is where we hold a Event object for adding/editing/displaying
   */
  private BwEvent event;

  private boolean addingEvent;

  private EventInfo eventInfo;

  /** Event copy before mods applied - temp till we finish change table stuff
   */
  private BwEvent savedEvent;

  private Collection<AddEventResult> addEventResults;

  private String summary;

  private String description;

  private String eventStatus;

  private String eventLink;

  private String eventCost;

  private String transparency;

  /** True if we should list all events
   */
  private boolean listAllEvents;

  private FormattedEvents formattedEvents;

  private Collection<DateTimeFormatter> formattedRdates;

  private Collection<DateTimeFormatter> formattedExdates;

  private Attendees attendees;

  private FbResponses fbResponses;

  private FormattedFreeBusy formattedFreeBusy;

  /* ....................................................................
   *                       Uploads and exports
   * .................................................................... */

  private FormFile uploadFile;

  private FormFile eventImageUpload;

  private String imageUploadDirectory;

  /* ....................................................................
   *                       Selection type and selection
   * .................................................................... */

  // ENUM
  private String selectionType = selectionTypeView;

  /* ....................................................................
   *                       View period
   * .................................................................... */

  private static HashMap<String, Integer> viewTypeMap =
    new HashMap<String, Integer>();

  static {
    for (int i = 0; i < BedeworkDefs.viewPeriodNames.length; i++) {
      viewTypeMap.put(BedeworkDefs.viewPeriodNames[i], new Integer(i));
    }
  }

  /** Index of the view type set when the page was last generated
   */
  private int curViewPeriod = -1;

  /** This will be set if a refresh is needed - we do it on the way out.
   */
  private boolean refreshNeeded;

  /** Index of the view type requested this time round. We set curViewPeriod to
   * viewTypeI. This allows us to see if the view changed as a result of the
   * request.
   */
  private int viewTypeI;

  /** one of the viewTypeNames values
   */
  private String viewType;

  /** The current view with user selected date (day, week, month etc)
   */
  private TimeView curTimeView;

  /** MyCalendarVO version of the start date
   */
  private MyCalendarVO viewMcDate;

  private TimeDateComponents viewStartDate;

  private String date;

  /** ...................................................................
   *                   Application state
   *  ................................................................... */


  /** True if we are adding an alert
   */
  private boolean alertEvent;

  /** True if we are adding a new location
   */
  private boolean addingLocation;

  /** True if we are adding a new administrative group
   */
  private boolean addingAdmingroup;

  /** True to show members in list
   */
  private boolean showAgMembers;

  /* ....................................................................
   *                   Authorised user fields
   * .................................................................... */

  /** Auth users for list or mod
   */
  private Collection<BwAuthUser> authUsers;

  /** Value built out of checked boxes.
   */
  private int editAuthUserType;

  /** User we want to fetch or modify
   */
  private String editAuthUserId;

  /** User object we are creating or modifying
   */
  private BwAuthUser editAuthUser;

  /* ....................................................................
   *                   Admin group fields
   * .................................................................... */

  private BwAdminGroup updAdminGroup;

  /** Group owner and group event owner */
  private String adminGroupGroupOwner;
  private String adminGroupEventOwner;

  /** Group member to add/delete
   */
  private String updGroupMember;

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
   *                   Location fields
   * .................................................................... */

  private String locationUid;

  /** Location address
   */
  private BwString locationAddress;

  /** Location subAddress
   */
  private BwString locationSubaddress;

  private BwLocation location;

  private SelectId<String> locId = new SelectId<String>(null,
                                                        SelectId.AHasPrecedence);

  /* ....................................................................
   *                   Categories
   * .................................................................... */

  /** True if we are adding a new category
   */
  private boolean addingCategory;

  private BwCategory category;

  private BwString categoryWord;
  private BwString categoryDesc;

  /* ....................................................................
   *                   Contacts
   * .................................................................... */

  private String contactUid;

  private SelectId<String> ctctId = new SelectId<String>(null,
                                                         SelectId.AHasPrecedence);

  /** True if we are adding a new contact
   */
  private boolean addingContact;

  private BwContact contact;

  private BwString contactName;

  /* ....................................................................
   *                       Calendars
   * .................................................................... */

  private BwCalendar beforeCalendar;

  private BwCalendar calendar;

  private Set<String> calendarsOpenState;

  /* current calendar - should probably be the same as below. */
  private String calPath;

  private String calendarPath;

  private String parentCalendarPath;

  /** True if we are adding a new calendar
   */
  private boolean addingCalendar;

  private BwCalendar meetingCal;

  private SelectId<String> calendarId = new SelectId<String>(null,
                                                               SelectId.AHasPrecedence);

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
   *                   Notifications, Inbox and Outbox
   * .................................................................... */

  private NotificationInfo notificationInfo;

  private InOutBoxInfo inBoxInfo;
  private InOutBoxInfo outBoxInfo;

  /* ....................................................................
   *                   Searches
   * .................................................................... */

  private int resultSize;

  private int resultStart;

  private int resultCt;

  private String query;

  private Collection<SearchResultEntry> searchResult;

  private String searchLimits = "fromToday";

  /* Values based on users page size */

  private int prevPage; // 1+  0 for none

  private int curPage; // 1+  0 for none

  private int nextPage; // 1+  0 for none

  private int numPages;

  private UpdateFromTimeZonesInfo updateFromTimeZonesInfo;

  private boolean reloadRequired;

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /**
   * @return String
   */
  public String getPublicAdminUri() {
    return getConfig().getPublicAdminUri();
  }

  /**
   * @return String
   */
  public String getPublicCalendarUri() {
    return getConfig().getPublicCalendarUri();
  }

  /**
   * @return String
   */
  public String getPersonalCalendarUri() {
    return getConfig().getPersonalCalendarUri();
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
   * @param val
   */
  public void setFilters(final Collection<BwFilterDef> val) {
    filters = val;
  }

  /**
   * @return Collection of BwFilterDef
   */
  public Collection<BwFilterDef> getFilters() {
    return filters;
  }

  /**
   * @return default timezoneid
   */
  public String getDefaultTzid() {
    try {
      return Timezones.getThreadDefaultTzid();
    } catch (Throwable t) {
      return "Exception: " + t.getLocalizedMessage();
    }
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
   * @return int
   */
  public int getMaxDescriptionLength() {
    try {
      return getSyspars().getMaxPublicDescriptionLength();
    } catch (Throwable t) {
      err.emit(t);
      return 0;
    }
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

  /* ====================================================================
   *                   Locations
   * ==================================================================== */

  /** Not set - invisible to jsp
   */
  /**
   * @param val
   */
  public void assignAddingLocation(final boolean val) {
    addingLocation = val;
  }

  /**
   * @return bool
   */
  public boolean getAddingLocation() {
    return addingLocation;
  }

  /**
   * @param val
   */
  public void setLocation(final BwLocation val) {
    location = val;
    if (val == null) {
      setLocationUid(null);
    } else {
      setLocationUid(val.getUid());
      if (val.getAddress() != null) {
        setLocationAddress((BwString)val.getAddress().clone());
      } else {
        setLocationAddress(null);
      }
      if (val.getSubaddress() != null) {
        setLocationSubaddress((BwString)val.getSubaddress().clone());
      } else {
        setLocationSubaddress(null);
      }
    }
  }

  /** If a location object exists, return that otherwise create an empty one.
   *
   * @return LocationVO  populated location value object
   */
  public BwLocation getLocation() {
    if (location == null) {
      location = BwLocation.makeLocation();
    }

    return location;
  }

  /**
   * @return SelectId id object
   */
  public SelectId<String> retrieveLocId() {
    return locId;
  }

  /** We have a preferred and all locations form field. One of them will be
   * unset so we ignore negative values.
   *
   * @param val
   */
  public void setAllLocationId(final String val) {
    if (Util.checkNull(val) != null) {
      locId.setA(val);
    }
  }

  /**
   * @return String
   */
  public String getAllLocationId() {
    return getLocation().getUid();
  }

  /**
   * @return String
   */
  public String getOriginalLocationId() {
    if (locId == null) {
      return null;
    }

    return locId.getOriginalVal();
  }

  /**
   * @param val
   */
  public void setPrefLocationId(final String val) {
    if (Util.checkNull(val) != null) {
      locId.setB(val);
    }
  }

  /**
   * @return String
   */
  public String getPrefLocationId() {
    return getLocation().getUid();
  }

  /** Get the preferred locations for the current user
   *
   * @return Collection  preferred locations
   */
  public Collection<BwLocation> getPreferredLocations() {
    return getLocationCollator().getCollatedCollection(
           getCurAuthUserPrefs().getLocationPrefs().getPreferred());
  }

  /* ====================================================================
   *                   Calendars
   * ==================================================================== */

  /**
   * @return SelectId id object
   */
  public SelectId<String> retrieveCalendarId() {
    return calendarId;
  }

  /** We have a preferred and all calendars form field. One of them will be
   * unset so we ignore null values.
   */
  /**
   * @param val
   */
  public void setCalendarId(final String val) {
    if (Util.checkNull(val) != null) {
      calendarId.setA(val);
    }
  }

  /**
   * @return cal id
   */
  public String getCalendarId() {
    return getCalendar().getPath();
  }

  /**
   * @return cal id (path)
   */
  public String getOriginalCalendarId() {
    if (calendarId == null) {
      return null;
    }

    return calendarId.getOriginalVal();
  }

  /**
   * @param val
   */
  public void setPrefCalendarId(final String val) {
    if (Util.checkNull(val) != null) {
      calendarId.setB(val);
    }
  }

  /**
   * @return id
   */
  public String getPrefCalendarId() {
    return getCalendar().getPath();
  }

  /** Get the preferred calendars for the current user
   *
   * @return Collection  preferred calendars
   */
  public Collection<BwCalendar> getPreferredCalendars() {
    return getCurAuthUserPrefs().getCalendarPrefs().getPreferred();
  }

  /* ====================================================================
   *                   Authorised user maintenance
   * ==================================================================== */

  /** Show whether user entries can be displayed or modified with this
   * class. Some sites may use other mechanisms.
   *
   * @return boolean    true if user maintenance is implemented.
   */
  public boolean getUserMaintOK() {
    try {
      return fetchSvci().getUserAuth().getUserMaintOK();
    } catch (Throwable t) {
      err.emit(t);
      return false;
    }
  }

  /**
   * @param val list of auth users
   */
  public void setAuthUsers(final Collection<BwAuthUser> val) {
    authUsers = val;
  }

  /**
   * @return list of auth users
   */
  public Collection<BwAuthUser> getAuthUsers() {
    return authUsers;
  }

  /** Only called if the flag is set - it's a checkbox.
   *
   * @param val
   */
  public void setEditAuthUserPublicEvents(final boolean val) {
    editAuthUserType |= UserAuth.publicEventUser;
  }

  /**
   *
   * @return boolean
   */
  public boolean getEditAuthUserPublicEvents() {
    return (editAuthUser.getUsertype() & UserAuth.publicEventUser) != 0;
  }

  /** New auth user rights
  *
   * @return int rights
   */
  public int getEditAuthUserType() {
    return editAuthUserType;
  }

  /**
   * @param val
   */
  public void setEditAuthUserId(final String val) {
    editAuthUserId = val;
  }

  /**
   * @return id
   */
  public String getEditAuthUserId() {
    return editAuthUserId;
  }

  /**
   * @param val
   */
  public void setEditAuthUser(final BwAuthUser val) {
    editAuthUser = val;
  }

  /**
   * @return auth user object
   */
  public BwAuthUser getEditAuthUser() {
    return editAuthUser;
  }

  /* ====================================================================
   *                   Admin groups
   * ==================================================================== */

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingAdmingroup(final boolean val) {
    addingAdmingroup = val;
  }

  /**
   * @return adding group
   */
  public boolean getAddingAdmingroup() {
    return addingAdmingroup;
  }

  /**
   * @param val
   */
  public void setShowAgMembers(final boolean val) {
    showAgMembers = val;
  }

  /**
   * @return group members
   */
  public boolean getShowAgMembers() {
    return showAgMembers;
  }

  /** Show whether admin group maintenance is available.
   * Some sites may use other mechanisms.
   *
   * @return boolean    true if admin group maintenance is implemented.
   */
  public boolean getAdminGroupMaintOK() {
    try {
      return fetchSvci().getAdminDirectories().getGroupMaintOK();
   } catch (Throwable t) {
      err.emit(t);
      return false;
    }
  }

  /**
   * @return groups
   */
  public Collection<BwGroup> getAdminGroups() {
    try {
      return fetchSvci().getAdminDirectories().getAll(showAgMembers);
   } catch (Throwable t) {
      err.emit(t);
      return new ArrayList<BwGroup>();
    }
  }

  /**
   * @return groups information
   */
  public Collection<BwGroup> getAdminGroupsInfo() {
    try {
      refreshAdminGroupsInfo: {
        if (System.currentTimeMillis() > (lastAdminGroupsInfoRefresh +
                           adminGroupsInfoRefreshInterval)) {
          synchronized (locker) {
            if (System.currentTimeMillis() < (lastAdminGroupsInfoRefresh +
                adminGroupsInfoRefreshInterval)) {
              // Somebody else got there
              break refreshAdminGroupsInfo;
            }

            adminGroupsInfo = new ArrayList<BwGroup>();

            Collection<BwGroup> ags = fetchSvci().getAdminDirectories().getAll(false);

            for (BwGroup g: ags) {
              BwGroup cg = (BwGroup)g.clone();

              Collection<BwGroup> mgs = fetchSvci().getAdminDirectories().getAllGroups(g);

              for (BwGroup mg: mgs) {
                BwGroup cmg = (BwGroup)mg.clone();

                cg.addGroup(cmg);
              }

              adminGroupsInfo.add(cg);
            }

            lastAdminGroupsInfoRefresh = System.currentTimeMillis();
          }
        }
      }

      return adminGroupsInfo;
    } catch (Throwable t) {
      err.emit(t);
      return new ArrayList<BwGroup>();
    }
  }

  /**
   * @param val
   */
  public void setUpdAdminGroup(final BwAdminGroup val) {
    if (val == null) {
      updAdminGroup = new BwAdminGroup();
    } else {
      updAdminGroup = val;
    }

    try {
      String href = updAdminGroup.getGroupOwnerHref();

      if (href != null) {
        setAdminGroupGroupOwner(href);
      }

      href = updAdminGroup.getOwnerHref();

      if (href != null) {
        setAdminGroupEventOwner(href);
      }
   } catch (Throwable t) {
      err.emit(t);
    }
  }

  /**
   * @return group
   */
  public BwAdminGroup getUpdAdminGroup() {
    if (updAdminGroup == null) {
      updAdminGroup = new BwAdminGroup();
    }

    return updAdminGroup;
  }

  /**
   * @param val
   */
  public void setAdminGroupGroupOwner(final String val) {
    adminGroupGroupOwner = val;
  }

  /**
   * @return group owner
   */
  public String getAdminGroupGroupOwner() {
    return adminGroupGroupOwner;
  }

  /**
   * @param val
   */
  public void setAdminGroupEventOwner(final String val) {
    adminGroupEventOwner = val;
  }

  /**
   * @return owner
   */
  public String getAdminGroupEventOwner() {
    return  adminGroupEventOwner;
  }

  /**
   * @param val
   */
  public void setUpdGroupMember(final String val) {
    updGroupMember = val;
  }

  /**
   * @return group member
   */
  public String getUpdGroupMember() {
    return updGroupMember;
  }

  /** Set a (cloned) copy of the system parameters
   * @param val
   */
  public void setSyspars(final SystemProperties val) {
    syspars = val.cloneIt();
  }

  /**
   * @return BwSystem object
   */
  public SystemProperties getSyspars() {
    if (syspars == null) {
      try {
        syspars = fetchSvci().getSystemProperties();
      } catch (Throwable t) {
        getErr().emit(t);
      }
    }

    return syspars;
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

  /** Set system statistics
  *
  * @param val      Collection of BwStats.StatsEntry objects
  */
  public void assignSysStats(final Collection<StatsEntry> val) {
    sysStats = val;
  }

  /** Get system statistics
  *
  * @return Collection of BwStats.StatsEntry objects
  */
  public Collection<StatsEntry> getSysStats() {
    if (sysStats == null) {
      sysStats = new ArrayList<StatsEntry>();
    }

    return sysStats;
  }

  /** Get system statistics enabled state
  *
   * @return boolean true if statistics collection enabled
  */
  public boolean getSysStatsEnabled() {
    try {
      return fetchSvci().getDbStatsEnabled();
    } catch (Throwable t) {
      return false;
    }
  }

  /** This will default to the current user. Superusers will be able to
   * specify a creator.
   *
   * @param val    String creator used to limit searches
   */
  public void setAdminUserId(final String val) {
    adminUserId = val;
  }

  /**
   * @return admin userid
   */
  public String getAdminUserId() {
    return adminUserId;
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

  /** Current user rights
  *
   * @return true for superuser
   */
  public boolean getCurUserSuperUser() {
    return fetchSvci().getSuperUser();
  }

  /** apptype
   *
   * @return boolean
   */
  public String getAppType() {
    return appType;
  }

  /**
   * @param val
   */
  public void assignAppType(final String val) {
    appType = val;
  }

  /** True for submitApp
   *
   * @return boolean
   */
  public boolean getSubmitApp() {
    return submitApp;
  }

  /**
   * @param val
   */
  public void assignSubmitApp(final boolean val) {
    submitApp = val;
  }

  /* ====================================================================
   *                   Calendar suites
   * ==================================================================== */

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

  /** Name of CalSuite to fetch.
   *
   * @param val
   */
  public void setCalSuiteName(final String val) {
    calSuiteName = val;
  }

  /** name of CalSuite to fetch.
   *
   * @return String
   */
  public String getCalSuiteName() {
    return calSuiteName;
  }

  /** CalSuite we are editing or creating.
   *
   * @param val
   */
  public void setCalSuite(final BwCalSuiteWrapper val) {
    calSuite = val;
  }

  /** CalSuite we are editing or creating.
   *
   * @return BwCalSuiteWrapper
   */
  public BwCalSuiteWrapper getCalSuite() {
    return calSuite;
  }

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingCalSuite(final boolean val) {
    addingCalSuite = val;
  }

  /**
   * @return bool
   */
  public boolean getAddingCalSuite() {
    return addingCalSuite;
  }

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingResource(final boolean val) {
    addingResource = val;
  }

  /**
   * @return the resource name
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Sets the resource name.
   * @param name
   */
  public void setResourceName(final String name) {
    this.resourceName = name;
  }

  /**
   * @return the resource class
   */
  public String getResourceClass() {
    return resourceClass;
  }

  /**
   * Sets the resource class.
   * @param resourceClass
   */
  public void setResourceClass(final String resourceClass) {
    this.resourceClass = resourceClass;
  }

  /**
   * @return bool
   */
  public boolean getAddingResource() {
    return addingResource;
  }

  /** Return the collection of cal suites
   *
   * @return Calendar suites
   */
  public Collection<BwCalSuite> getCalSuites() {
    try {
      Map<String, SubContext> suiteToContextMap = new HashMap<String, SubContext>();
      SysparsI sysi = fetchSvci().getSysparsHandler();
      BwSystem syspars = sysi.get();
      Set<SubContext> contexts = syspars.getContexts();
      for (SubContext subContext : contexts) {
        suiteToContextMap.put(subContext.getCalSuite(), subContext);
      }

      Collection<BwCalSuite> suites = fetchSvci().getCalSuitesHandler().getAll();
      for (BwCalSuite bwCalSuite : suites) {
        SubContext subContext = suiteToContextMap.get(bwCalSuite.getName());
        if (subContext != null) {
          bwCalSuite.setContext(subContext.getContextName());
          bwCalSuite.setDefaultContext(subContext.getDefaultContext());
        } else {
          bwCalSuite.setContext(null);
          bwCalSuite.setDefaultContext(false);
        }
      }
      return suites;
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Current resource fetched
   *
   * @param val
   */
  public void setCalSuiteResource(final CalSuiteResource val) {
    calSuiteResource = val;
  }

  /**
   * @return resource or null
   */
  public CalSuiteResource getCalSuiteResource() {
    return calSuiteResource;
  }

  /** Current resources fetched
   *
   * @param val
   */
  public void setCalSuiteResources(final List<CalSuiteResource> val) {
    calSuiteResources = val;
  }

  /**
   * @return list or null
   */
  public List<CalSuiteResource> getCalSuiteResources() {
    return calSuiteResources;
  }

  /* ====================================================================
   *                   Admin groups
   * ==================================================================== */

  /**
   * @param val
   */
  public void assignGroupSet(final boolean val) {
    groupSet = val;
  }

  /**
   * @return true for group set
   */
  public boolean getGroupSet() {
    return groupSet;
  }

  /**
   * @param val
   */
  public void setOneGroup(final boolean val) {
    oneGroup = val;
  }

  /**
   * @return true if there is only one group
   */
  public boolean getOneGroup() {
    return oneGroup;
  }

  /**
   * @param val
   */
  public void assignChoosingGroup(final boolean val) {
    choosingGroup = val;
  }

  /**
   * @return true for choosing group
   */
  public boolean retrieveChoosingGroup() {
    return choosingGroup;
  }

  /** Current admin group name, or null for none
   *
   * @param val      BwAdminGroup representing users group or null
   */
  public void assignAdminGroupName(final String val) {
    adminGroupName = val;
    assignGroupSet(true);
  }

  /**
   * @return String admin group name
   */
  public String getAdminGroupName() {
    return adminGroupName;
  }

  /** The groups of which our user is a member
   *
   * @param val
   */
  public void setUserAdminGroups(final Collection<BwGroup> val) {
    userAdminGroups = val;
  }

  /**
   * @return user admin groups
   */
  public Collection<BwGroup> getUserAdminGroups() {
    return userAdminGroups;
  }

  /**
   * @return mailer object
   */
  public MailerIntf getMailer() {
    if (mailer == null) {
      try {
        mailer = fetchSvci().getMailer();
      } catch (Throwable t) {
        err.emit(t);
      }
    }

    return mailer;
  }

  /**
   * @param val
   */
  public void assignUserVO(final BwPrincipal val) {
    userVO = val;
  }

  /**
   * @return BwPrincipal
   */
  public BwPrincipal getUserVO() {
    return userVO;
  }

  /**
   * @return current users calendar user address.
   */
  public String getCalendarUserAddress() {
    try {
      return client.getCurrentCalendarAddress();
    } catch (Throwable t) {
      getErr().emit(t);
      return "**error**";
    }
  }

  /**
   * @param val - entry time - millisecs
   */
  public void setTimeIn(final long val) {
    timeIn = val;
  }

  /**
   * @return long  - entry time - millisecs
   */
  public long getTimeIn() {
    return timeIn;
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
   * request.
   *
   * @param val      String user id
   */
  public void assignCurrentAdminUser(final String val) {
    currentAdminUser = val;
  }

  /**
   * @return admin user id
   */
  public String getCurrentAdminUser() {
    return currentAdminUser;
  }

  /**
   * @param val svci
   */
  public void setCalSvcI(final CalSvcI val) {
    calsvci = val;
  }

  /**
   * @return svci
   */
  public CalSvcI fetchSvci() {
    return calsvci;
  }

  /**
   * @param val a client object
   */
  public void setClient(final Client val) {
    client = val;
  }

  /**
   * @return a client object
   */
  public Client fetchClient() {
    return client;
  }

  /** Set flag to show if this user has any admin rights.
   *
   * @param val   boolean true if admin user
   */
  public void assignAuthorisedUser(final boolean val) {
    authorisedUser = val;
  }

  /** See if user has some form of access
   *
   * @return true for auth user
   */
  public boolean getAuthorisedUser() {
    return authorisedUser;
  }

  /**
   * @return DateTimeFormatter today
   */
  public DateTimeFormatter getToday() {
    if (today != null) {
      return today;
    }

    try {
      today = new DateTimeFormatter(BwDateTimeUtil.getDateTime(
                                   new Date(System.currentTimeMillis())));
    } catch (Throwable t) {
      getErr().emit(t);
    }

    return today;
  }

  /**
   * @param val calendar info
   */
  public void setCalInfo(final CalendarInfo val) {
    calInfo = val;
  }

  /**
   * @return calendar info
   */
  public CalendarInfo getCalInfo() {
    return calInfo;
  }

  /**
   * @param val Property references
   */
  public void setPropRefs(final Collection<EventPropertiesReference> val) {
    propRefs = val;
  }

  /**
   * @return property refs
   */
  public Collection<EventPropertiesReference> getPropRefs() {
    return propRefs;
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
  public void setUploadFile(final FormFile val) {
    uploadFile = val;
  }

  /**
   * @return FormFile
   */
  public FormFile getUploadFile() {
    return uploadFile;
  }

  /**
   * @param val
   */
  public void setEventImageUpload(final FormFile val) {
    eventImageUpload = val;
  }

  /**
   * @return FormFile
   */
  public FormFile getEventImageUpload() {
    return eventImageUpload;
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

  /** Index of the view type set when the page was last generated
   *
   * @param val  int valid view index
   */
  public void setCurViewPeriod(final int val) {
    curViewPeriod = val;
  }

  /**
   * @return view index
   */
  public int getCurViewPeriod() {
    return curViewPeriod;
  }

  /** Index of the view type requested this time round. We set curViewPeriod to
   * viewTypeI. This allows us to see if the view changed as a result of the
   * request.
   *
   * @param val index
   */
  public void setViewTypeI(final int val) {
    viewTypeI = val;
  }

  /**
   * @return index
   */
  public int getViewTypeI() {
    return viewTypeI;
  }

  /** This often appears as the request parameter specifying the view.
   * It should be one of the viewTypeNames
   *
   * @param  val   String viewType
   */
  public void setViewType(final String val) {
    viewType = Util.checkNull(val);

    if (viewType == null) {
      viewTypeI = -1;
      return;
    }

    Integer i = viewTypeMap.get(viewType);

    if (i == null) {
      viewTypeI = BedeworkDefs.defaultView;
    } else {
      viewTypeI = i;
    }
  }

  /** Return the value or a default if it's invalid
   *
   * @param val
   * @return String valid view period
   */
  public String validViewPeriod(String val) {
    int vt = BedeworkDefs.defaultView;

    val = Util.checkNull(val);
    if (val != null) {
      Integer i = viewTypeMap.get(val);

      if (i != null) {
        vt = i;
      }
    }

    return BedeworkDefs.viewPeriodNames[vt];
  }

  /**
   * @return String
   */
  public String getViewType() {
    return viewType;
  }

  /** Date of the view as a MyCalendar object
   *
   * @param val
   */
  public void setViewMcDate(final MyCalendarVO val) {
    viewMcDate = val;
  }

  /** Date of the view as a MyCalendar object
   *
   * @return MyCalendarVO date last set
   */
  public MyCalendarVO getViewMcDate() {
    return viewMcDate;
  }

  /** The current view (day, week, month etc)
   *
   * @param val
   */
  public void setCurTimeView(final TimeView val) {
    curTimeView = val;
  }

  /**
   * @return current view (day, week, month etc) and refresh the events.
   */
  public TimeView getCurTimeViewRefreshed() {
    TimeView tv = getCurTimeView();
    if (tv != null) {
      tv.refreshEvents();
    }

    return tv;
  }

  /** Call to save some vm between requests
   *
   */
  public void purgeCurTimeView() {
    if (curTimeView != null) {
      curTimeView.refreshEvents();
    }
  }

  /**
   *
   * @return current view (day, week, month etc)
   */
  public TimeView getCurTimeView() {
    /* We might be called before any time is set. Set a week view by
       default
       */

    try {
      if (curTimeView == null) {
        /** Figure out the default from the properties
         */
        String vn;

        try {
          vn = fetchSvci().getPrefsHandler().get().getPreferredViewPeriod();
          if (vn == null) {
            vn = "week";
          }
        } catch (Throwable t) {
          System.out.println("Exception setting current view");
          vn = "week";
        }

        if (curViewPeriod < 0) {
          for (int i = 1; i < BedeworkDefs.viewPeriodNames.length; i++) {
            if (BedeworkDefs.viewPeriodNames[i].startsWith(vn)) {
              curViewPeriod = i;
              break;
            }
          }

          if (curViewPeriod < 0) {
            curViewPeriod = BedeworkDefs.weekView;
          }

          setViewMcDate(new MyCalendarVO(new Date(System.currentTimeMillis())));
        }

        refreshView();
      }
    } catch (Throwable t) {
      getLog().error("Exception in getCurTimeView", t);
    }

    if (curTimeView == null) {
      getLog().warn("Null time view!!!!!!!!!!!");
    }

    return curTimeView;
  }

  /**
   * @return time date
   */
  public TimeDateComponents getViewStartDate() {
    if (viewStartDate == null) {
      viewStartDate = getNowTimeComponents();
    }

    return viewStartDate;
  }

  /** This often appears as the request parameter specifying the date for an
   * action. Always YYYYMMDD format
   *
   * @param  val   String date in YYYYMMDD format
   */
  public void setDate(final String val) {
    if (!CheckData.checkDateString(val)) {
      date = new MyCalendarVO(new Date(System.currentTimeMillis())).getDateDigits();
    } else {
      date = val;
    }
  }

  /**
   * @return date
   */
  public String getDate() {
    return date;
  }

  /** XXX This looks wrong we might be refreshing twice.
   *
   */
  public void refreshIsNeeded() {
//    refreshView();
    curTimeView = null;
    refreshNeeded = true;
  }

  /** set refreh needed flag
   *
   * @param val   boolean
   */
  public void setRefreshNeeded(final boolean val) {
    refreshNeeded = val;
  }

  /**
   * @return true if we need to refresh
   */
  public boolean isRefreshNeeded() {
    return refreshNeeded;
  }

  /** Reset the view according to the current setting of curViewPeriod.
   * May be called when we change the view or if we need a refresh
   *
   */
  public void refreshView() {
    if (debug) {
      getLog().debug(" set new view to ViewTypeI=" + curViewPeriod);
    }

    try {
      if ((curViewPeriod == BedeworkDefs.todayView) ||
          (viewMcDate == null)) {
        viewMcDate = new MyCalendarVO(new Date(System.currentTimeMillis()));
      }

     FilterBase filter = getFilter(null);

      switch (curViewPeriod) {
      case BedeworkDefs.todayView:
        setCurTimeView(new DayView(fetchClient(),
                                   getErr(),
                                   viewMcDate,
                                   filter));
        break;
      case BedeworkDefs.dayView:
        setCurTimeView(new DayView(fetchClient(),
                                   getErr(),
                                   viewMcDate,
                                   filter));
        break;
      case BedeworkDefs.weekView:
        setCurTimeView(new WeekView(fetchClient(),
                                    getErr(),
                                    viewMcDate,
                                    filter));
        break;
      case BedeworkDefs.monthView:
        setCurTimeView(new MonthView(fetchClient(),
                                     getErr(),
                                     viewMcDate,
                                     filter));
        break;
      case BedeworkDefs.yearView:
        setCurTimeView(new YearView(fetchClient(),
                                    getErr(),
                                    viewMcDate,
                       getShowYearData(), filter));
        break;
      }
    } catch (Throwable t) {
      // Not much we can do here
      setException(t);
    }
  }

  /** If a name is defined fetch it, or use the current filter if it exists
   *
   * @param filterName
   * @return BwFilter or null
   * @throws Throwable
   */
  private FilterBase getFilter(final String filterName) throws Throwable {
    CalSvcI svci = fetchSvci();
    BwFilterDef fdef = null;

    if (filterName != null) {
      fdef = svci.getFiltersHandler().get(filterName);

      if (fdef == null) {
        getErr().emit(ClientError.unknownFilter, filterName);
      }
    }

    if (fdef == null) {
      fdef = getCurrentFilter();
    }

    if (fdef == null) {
      return null;
    }

    if (fdef.getFilters() == null) {
      try {
        svci.getFiltersHandler().parse(fdef);
      } catch (CalFacadeException cfe) {
        getErr().emit(cfe);
      }
    }

    return fdef.getFilters();
  }

  /* ....................................................................
   *                       Selection type
   * .................................................................... */

  /**
   * @param val
   */
  public void setSelectionType(final String val) {
    selectionType = val;
  }

  /**
   * @return String
   */
  public String getSelectionType() {
    return selectionType;
  }

  /* ====================================================================
   *                   Views
   * ==================================================================== */

  /** Return the collection of views - named collections of subscriptions
   *
   * @return views
   */
  public Collection<BwView> getViews() {
    try {
      return fetchSvci().getViewsHandler().getAll();
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Get the current collection from the client state
   *
   * @return BwCalendar object  object or null for all events
   */
  public BwCalendar getCurrentCollection() {
    return null; // clientState always returns null
  }

  /** Get the current virtual path from the client state
   *
   * @return BwCalendar object  object or null for all events
   */
  public String getCurrentVirtualPath() {
    try {
      return client.getVirtualPath();
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Get the current view we have set
   *
   * @return BwView    named Collection of Collections or null for default
   * @throws CalFacadeException
   */
  public BwView getCurrentView() throws CalFacadeException {
    try {
      return client.getCurrentView();
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

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

  /** Return the encoded root of the submissions calendars
   *
   * @return String path.
   */
  public String getSubmissionsRoot() {
    String appType = getAppType();

    if (appTypeWebsubmit.equals(appType) ||
        appTypeWebadmin.equals(appType)) {
      try {
        return URLEncoder.encode(getConfig().getSubmissionRoot(), "UTF-8");
      } catch (Throwable t) {
        getErr().emit(t);
      }
    }

    return "";
  }

  /** Return the unencoded root of the submissions calendars
   *
   * @return String path.
   */
  public String getUnencodedSubmissionsRoot() {
    String appType = getAppType();

    if (appTypeWebsubmit.equals(appType) ||
        appTypeWebadmin.equals(appType)) {
      return getConfig().getSubmissionRoot();
    }

    return "";
  }

  /** Return the public calendars
   *
   * @return BwCalendar   root calendar
   */
  public BwCalendar getPublicCalendars() {
    try {
      return fetchSvci().getCalendarsHandler().getPublicCalendars();
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Return the current users calendars. For admin or guest mode this is the
   * same as calling getPublicCalendars.
   *
   * <p>For the websubmit application we return the root of the submission
   * calendars.
   *
   * @return BwCalendar   root of calendar tree
   */
  public BwCalendar getCalendars() {
    BwCalendar calendar;

    try {
      if (getSubmitApp()) {
        // Use submission root
        calendar = fetchSvci().getCalendarsHandler().get(getConfig().getSubmissionRoot());
      } else {
        // Current owner
        calendar = fetchSvci().getCalendarsHandler().getHome();
      }

      if (calendar != null) {
        Set<String> cos = getCalendarsOpenState();

        if (cos != null) {
          calendar.setOpen(cos.contains(calendar.getPath()));
        }
      }

      fetchSvci().getCalendarsHandler().resolveAlias(calendar, true, false);

      return calendar;
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Return the current users calendars.
   *
   * @return BwCalendar   root of calendar sub-tree
   */
  public BwCalendar getUserCalendars() {
    BwCalendar calendar;

    try {
      BwPrincipal p;

      if (publicAdmin()) {
        // Use calendar suite owner
        p = fetchSvci().getUsersHandler().getPrincipal(
                              currentCalSuite.getGroup().getOwnerHref());
      } else {
        p = getUserVO();
      }

      calendar = fetchSvci().getCalendarsHandler().getHome(p, false);

      if (calendar != null) {
        Set<String> cos = getCalendarsOpenState();

        if (cos != null) {
          calendar.setOpen(cos.contains(calendar.getPath()));
        }
      }

      fetchSvci().getCalendarsHandler().resolveAlias(calendar, true, false);

      return calendar;
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

  /** Return a list of calendars in which calendar objects can be
   * placed by the current user.
   *
   * <p>Caldav currently does not allow collections inside collections so that
   * calendar collections are the leaf nodes only.
   *
   * @return Collection   of BwCalendar
   */
  public Collection<BwCalendar> getAddContentCalendarCollections() {
    try {
      return getCalendarCollator().getCollatedCollection(
                    fetchSvci().getCalendarsHandler().getAddContentCollections(!publicAdmin()));
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }

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
   * @param val
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
   * @param val
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

  /** Save the Path of the soon-to-be parent
   *
   * @param val
   */
  public void setParentCalendarPath(final String val) {
    parentCalendarPath = val;
  }

  /**
   * @return cal Path
   */
  public String getParentCalendarPath() {
    return parentCalendarPath;
  }

  /** Not set - invisible to jsp
   */
  /**
   * @param val
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
  public void assignBeforeCalendar(final BwCalendar val) {
    beforeCalendar = val;
  }

  /** Saved copy of object before changes made..
   *
   * @return BwCalendar
   */
  public BwCalendar fetchBeforeCalendar() {
    return beforeCalendar;
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

  /** Last email address used
   *
   * @param  val   email address
   */
  public void setLastEmail(final String val) {
    lastEmail = val;
  }

  /**
   * @return last email
   */
  public String getLastEmail() {
    if (lastEmail == null) {
      if (getUserVO() != null) {
        lastEmail = getPreferences().getEmail();
      }
    }

    return lastEmail;
  }

  /** Last subject used
   *
   * @param  val   subject
   */
  public void setLastSubject(final String val) {
    lastSubject = val;
  }

  /**
   * @return last subject
   */
  public String getLastSubject() {
    return lastSubject;
  }

  /** Get the preferences for the current user
   *
   * @return prefs
   */
  public BwPreferences getPreferences() {
    if (preferences == null) {
      try {
        preferences = fetchSvci().getPrefsHandler().get();
      } catch (Throwable t) {
        err.emit(t);
      }
    }

    return preferences;
  }

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
   *                   Categories
   * ==================================================================== */

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingCategory(final boolean val) {
    addingCategory = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddingCategory() {
    return addingCategory;
  }

  /**
   * @param val
   */
  public void setCategory(final BwCategory val) {
    category = val;
    categoryWord = null;
    categoryDesc = null;
    if (val != null) {
      BwString s = val.getWord();
      if (s != null) {
        categoryWord = (BwString)s.clone();
      }
      s = val.getDescription();
      if (s != null) {
        categoryDesc = (BwString)s.clone();
      }
    }
  }

  /** If a Category object exists, return that otherwise create an empty one.
   *
   * @return BwCategory  Category value object
   */
  public BwCategory getCategory() {
    if (category == null) {
      category = BwCategory.makeCategory();
    }

    return category;
  }

  /**
   * @param val
   */
  public void setCategoryWord(final BwString val) {
    categoryWord = val;
  }

  /** If a category word object exists, return that otherwise create an empty one.
   *
   * @return BwString  Category word value object
   */
  public BwString getCategoryWord() {
    if (categoryWord == null) {
      categoryWord = new BwString();
    }

    return categoryWord;
  }

  /**
   * @param val
   */
  public void setCategoryDesc(final BwString val) {
    categoryDesc = val;
  }

  /** If a category Desc object exists, return that otherwise create an empty one.
   *
   * @return BwString  Category Desc value object
   */
  public BwString getCategoryDesc() {
    if (categoryDesc == null) {
      categoryDesc = new BwString();
    }

    return categoryDesc;
  }

  /** This is the current category,
   *
   * @return current category
   */
  public String getCategoryKey() {
    return getCategory().getWordVal();
  }

  /** Get the preferred categories for the current user
   *
   * @return Collection  preferred categories
   */
  public Collection<BwCategory> getPreferredCategories() {
    try {
      return fetchSvci().getCategoriesHandler().getCached(
                             getCurAuthUserPrefs().getCategoryPrefs().getPreferred());
    } catch (Throwable t) {
      getErr().emit(t);
      return new ArrayList<BwCategory>();
    }
  }

  /** Get the list of categories for this owner. Return a null list for
   * exceptions or no categories. For guest mode or public admin this is the
   * same as getPublicCategories. This is the method to call unless you
   * specifically want a list of public categories (for search of public events
   * perhaps.)
   *
   * @return Collection    of BwCategory
   */
  public Collection<BwCategory> getCategories() {
    return getCategoryCollection(ownersEntity, true);
  }

  /** Get the list of editable categories for this user. Return a null list for
   * exceptions or no categories.
   *
   * @return Collection    of BwCategory
   */
  public Collection<BwCategory> getEditableCategories() {
    return getCategoryCollection(editableEntity, false);
  }

  /** Get the default categories for the current user
   *
   * @return Set  default categories
   */
  public Set<BwCategory> getDefaultCategories() {
    Set<BwCategory> cats = new TreeSet<BwCategory>();

    try {
      Set<String> catuids = fetchSvci().getPrefsHandler().get().getDefaultCategoryUids();

      for (String uid: catuids) {
        BwCategory cat = fetchSvci().getCategoriesHandler().get(uid);

        if (cat != null) {
          cats.add(cat);
        }
      }
    } catch (Throwable t) {
      getErr().emit(t);
    }

    return cats;
  }

  /* ====================================================================
   *                   Contacts
   * ==================================================================== */

  /** Not set - invisible to jsp
   *
   * @param val
   */
  public void assignAddingContact(final boolean val) {
    addingContact = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddingContact() {
    return addingContact;
  }

  /**
   * @param val
   */
  public void setContact(final BwContact val) {
    contact = val;
    contactName = null;
    contactUid = null;
    if (val != null) {
      contactUid = val.getUid();

      BwString s = val.getName();
      if (s != null) {
        contactName = (BwString)s.clone();
      }
    }
  }

  /** If a Contact object exists, return that otherwise create an empty one.
   *
   * @return BwContact  Contact value object
   */
  public BwContact getContact() {
    if (contact == null) {
      contact = new BwContact();
    }

    return contact;
  }

  /**
   * @param val
   */
  public void setContactName(final BwString val) {
    contactName = val;
  }

  /** If a contact name object exists, return that otherwise create an empty one.
   *
   * @return BwString  Contact name value object
   */
  public BwString getContactName() {
    if (contactName == null) {
      contactName = new BwString();
    }

    return contactName;
  }

  /**
   * @return SelectId id object
   */
  public SelectId<String> retrieveCtctId() {
    return ctctId;
  }

  /** We have a preferred and all contacts form field. One of them may be
   * unset so we ignore null values
   *
   * @param val
   */
  public void setAllContactId(final String val) {
    if (Util.checkNull(val) != null) {
      ctctId.setA(val);
    }
  }

  /** This is the current contact, usually out of the current event. It is
   * used to select a particular contact in select lists.
   * @see org.bedework.webcommon.BwActionFormBase#getContactUid()
   *
   * @return int
   */
  public String getAllContactId() {
    return getContact().getUid();
  }

  /**
   * @return int
   */
  public String getOriginalContactId() {
    if (ctctId == null) {
      return null;
    }

    return ctctId.getOriginalVal();
  }

  /**
   * @param val
   */
  public void setPrefContactId(final String val) {
    if (Util.checkNull(val) != null) {
      ctctId.setB(val);
    }
  }

  /** This is the current contact, usually out of the current event. It is
   * used to select a particular contact in select lists.
   *
   * @return String
   */
  public String getPrefContactId() {
    return getContact().getUid();
  }

  /** Get the preferred contacts for the current user
   *
   * @return Collection  preferred contacts
   */
  public Collection<BwContact> getPreferredContacts() {
    return getContactCollator().getCollatedCollection(
          getCurAuthUserPrefs().getContactPrefs().getPreferred());
  }

  /** Contact uid for next action
   *
   * @param val
   */
  public void setContactUid(final String val) {
    contactUid = val;
  }

  /**
   * @return id
   */
  public String getContactUid() {
    return contactUid;
  }

  /** Get the list of contacts for this owner. Return a null list for
   * exceptions or no contacts.
   *
   * @return Collection    of BwContact
   */
  public Collection<BwContact> getContacts() {
    return getContactCollection(ownersEntity);
  }

  /** Get the list of editable contacts for this user. Return a null list for
   * exceptions or no contacts.
   *
   * @return Collection    of BwContact
   */
  public Collection<BwContact> getEditableContacts() {
    return getContactCollection(editableEntity);
  }

  /* ====================================================================
   *                   Locations
   * ==================================================================== */

  /**
   * @param val
   */
  public void setLocationAddress(final BwString val) {
    locationAddress = val;
  }

  /**
   * @return location address
   */
  public BwString getLocationAddress() {
    if (locationAddress == null) {
      locationAddress = new BwString();
    }

    return locationAddress;
  }

  /**
   * @param val
   */
  public void setLocationSubaddress(final BwString val) {
    locationSubaddress = val;
  }

  /**
   * @return location subaddress
   */
  public BwString getLocationSubaddress() {
    if (locationSubaddress == null) {
      locationSubaddress = new BwString();
    }

    return locationSubaddress;
  }

  /** Location id for next action
   *
   * @param val id
   */
  public void setLocationUid(final String val) {
    locationUid = val;
  }

  /**
   * @return uid
   */
  public String getLocationUid() {
    return locationUid;
  }

  /** Called by jsp when editing an event
   *
   * @return locations
   */
  public Collection<BwLocation> getLocations() {
    return getLocations(ownersEntity, true);
  }

  /**
   * @return editable locations
   */
  public Collection<BwLocation> getEditableLocations() {
    return getLocations(editableEntity, false);
  }

  /* ====================================================================
   *                   Events
   * ==================================================================== */

  /** Not set - invisible to jsp
   */
  /**
   * @param val
   */
  public void assignAddingEvent(final boolean val) {
    addingEvent = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddingEvent() {
    return addingEvent;
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

  /**
   *
   * @param val formatted events
   */
  public void setFormattedEvents(final FormattedEvents val) {
    formattedEvents = val;
  }

  /** Return a formatted events object. If doing alerts we pick them out
   * otherwise exclude them
   *
   * @return FormattedEvents  populated event value objects
   */
  public FormattedEvents getFormattedEvents() {
    return formattedEvents;
  }

  /**
   *
   * @param val Collection of formatted rdates
   */
  public void setFormattedRdates(final Collection<DateTimeFormatter> val) {
    formattedRdates = val;
  }

  /** Return formatted rdate objects.
   *
   * @return Collection
   */
  public Collection<DateTimeFormatter> getFormattedRdates() {
    return formattedRdates;
  }

  /**
   *
   * @param val Collection of formatted exdates
   */
  public void setFormattedExdates(final Collection<DateTimeFormatter> val) {
    formattedExdates = val;
  }

  /** Return formatted exdate objects.
   *
   * @return Collection
   */
  public Collection<DateTimeFormatter> getFormattedExdates() {
    return formattedExdates;
  }

  /**
   * @param val
   */
  public void setAttendees(final Attendees val) {
    attendees = val;
  }

  /**
   * @return Attendees
   */
  public Attendees getAttendees() {
    return attendees;
  }

  /**
   * @param val
   */
  public void setFbResponses(final FbResponses val) {
    fbResponses = val;
  }

  /**
   * @return FbResponses
   */
  public FbResponses getFbResponses() {
    return fbResponses;
  }

  /**
   * @param val
   */
  public void setFormattedFreeBusy(final FormattedFreeBusy val) {
    formattedFreeBusy = val;
  }

  /**
   * @return FormattedFreeBusy
   */
  public FormattedFreeBusy getFormattedFreeBusy() {
    return formattedFreeBusy;
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

  /** Event key for next action
   *
   * @param val
   */
  public void setEventListPars(final EventListPars val) {
    eventListPars = val;
  }

  /**
   * @return EventListPars
   */
  public EventListPars getEventListPars() {
    return eventListPars;
  }

  /**
   * @return event
   */
  public BwEvent getEvent() {
    return event;
  }

  /**
   * @param val
   */
  public void setEventInfo(final EventInfo val) {
    try {
      if (val == null) {
        event = new BwEventObj();
        getEventDates().setNewEvent(event);
        eventInfo = new EventInfo(event);

        TimeView tv = getCurTimeView();

        getEventStartDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
        getEventEndDate().setDateTime(tv.getCurDayFmt().getDateTimeString());
     } else {
        event = val.getEvent();
        eventInfo = val;
        getEventDates().setFromEvent(event);
      }

      locationAddress = null;
      locationUid = null;
    } catch (Throwable t) {
      err.emit(t);
    }
  }

  /**
   * @return EventInfo
   */
  public EventInfo getEventInfo() {
    return eventInfo;
  }

  /** Event passed between actions
   * @param val
   */
  public void assignSavedEvent(final BwEvent val) {
    savedEvent = val;
  }

  /**
   * @return event
   */
  public BwEvent retrieveSavedEvent() {
    return savedEvent;
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
  public void setAddEventResults(final Collection<AddEventResult> val) {
    addEventResults = val;
  }

  /**
   * @return failed overrides from add of event.
   */
  public Collection<AddEventResult> getAddEventResults() {
    return addEventResults;
  }

  /**
   * @param val
   */
  public void setSummary(final String val) {
    summary = val;
  }

  /**
   * @return summary
   */
  public String getSummary() {
    return summary;
  }

  /**
   * @param val
   */
  public void setDescription(final String val) {
    description = val;
  }

  /**
   * @return String description for event
   */
  public String getDescription() {
    return description;
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

  /**
   * @param val
   */
  public void setEventLink(final String val) {
    eventLink = val;
  }

  /**
   * @return String eventLink for event
   */
  public String getEventLink() {
    return eventLink;
  }

  /**
   * @param val
   */
  public void setEventCost(final String val) {
    eventCost = val;
  }

  /**
   * @return String eventLink for event
   */
  public String getEventCost() {
    return eventCost;
  }

  /**
   * @param val
   */
  public void setTransparency(final String val) {
    transparency = val;
  }

  /**
   * @return String transparency for event
   */
  public String getTransparency() {
    return transparency;
  }

  /* ====================================================================
   *             Start and end Date and time and duration
   *
   * Methods related to selecting a particular date. These values may be
   * used when setting the current date or when setting the date of an event.
   * They will be distinguished by the action called.
   * ==================================================================== */

  /** Return an object containing the dates.
   *
   * @return EventDates  object representing date/times and duration
   */
  public EventDates getEventDates() {
    if (eventDates == null) {
      try {
        eventDates = new EventDates(fetchClient().getCurrentPrincipalHref(),
                                    getCalInfo(),
                                    getHour24(), getEndDateType(),
                                    config.getMinIncrement(),
                                    err);
      } catch (Throwable t) {
        err.emit(t);
      }
    }

    return eventDates;
  }

  /** Return an object containing the rdate.
   *
   * @return TimeDateComponents  for rdate
   */
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
  }

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

  /**
   * @return InOutBoxInfo
   */
  public InOutBoxInfo getInBoxInfoRefreshed() {
    if (inBoxInfo != null) {
      try {
        inBoxInfo.refresh(true);
      } catch (CalFacadeException cfe) {
        getErr().emit(cfe);
      }
    }
    return inBoxInfo;
  }

  /**
   * @param val
   */
  public void setOutBoxInfo(final InOutBoxInfo val) {
    outBoxInfo = val;
  }

  /**
   * @return InOutBoxInfo
   */
  public InOutBoxInfo getOutBoxInfo() {
    return outBoxInfo;
  }

  /**
   * @return InOutBoxInfo
   */
  public InOutBoxInfo getOutBoxInfoRefreshed() {
    if (outBoxInfo != null) {
      try {
        outBoxInfo.refresh(true);
      } catch (CalFacadeException cfe) {
        getErr().emit(cfe);
      }
    }
    return outBoxInfo;
  }

  /* ====================================================================
   *                   Alarm fields
   * ==================================================================== */

  /* *
   * @param val
   * /
  private void setTriggerDateTime(TimeDateComponents val) {
    triggerDateTime = val;
  } */

  /**
   * @return time date
   */
  public TimeDateComponents getTriggerDateTime() {
    if (triggerDateTime == null) {
      triggerDateTime = getNowTimeComponents();
    }

    return triggerDateTime;
  }

  /**
   * @param val
   */
  public void setTriggerDuration(final DurationBean val) {
    triggerDuration = val;
  }

  /**
   * @return duration
   */
  public DurationBean getTriggerDuration() {
    if (triggerDuration == null) {
      triggerDuration = new DurationBean();
    }

    return triggerDuration;
  }

  /**
   * @param val
   */
  public void setAlarmRelStart(final boolean val) {
    alarmRelStart = val;
  }

  /**
   * @return alarm rel start
   */
  public boolean getAlarmRelStart() {
    return alarmRelStart;
  }

  /**
   * @param val
   */
  public void setAlarmDuration(final DurationBean val) {
    alarmDuration = val;
  }

  /**
   * @return duration
   */
  public DurationBean getAlarmDuration() {
    if (alarmDuration == null) {
      alarmDuration = new DurationBean();
    }

    return alarmDuration;
  }

  /**
   * @param val
   */
  public void setAlarmRepeatCount(final int val) {
    alarmRepeatCount = val;
  }

  /**
   * @return int
   */
  public int getAlarmRepeatCount() {
    return alarmRepeatCount;
  }

  /**
   * @param val
   */
  public void setAlarmTriggerByDate(final boolean val) {
    alarmTriggerByDate = val;
  }

  /**
   * @return  bool
   */
  public boolean getAlarmTriggerByDate() {
    return alarmTriggerByDate;
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

  /* ....................................................................
   *                   Searches
   * .................................................................... */

  /** Set result set size for last search
   * @param val
   */
  public void setResultSize(final int val) {
    resultSize = val;
  }

  /**
   * @return result set size for last search
   */
  public int getResultSize() {
    return resultSize;
  }

  /** Set retrieval start
   * @param val
   */
  public void setResultStart(final int val) {
    resultStart = val;
  }

  /**
   * @return start position
   */
  public int getResultStart() {
    return resultStart;
  }

  /** Set count found in last search
   * @param val
   */
  public void setResultCt(final int val) {
    resultCt = val;
  }

  /**
   * @return count found in last search
   */
  public int getResultCt() {
    return resultCt;
  }

  /** Set query from last search
   *
   * @param val
   */
  public void setQuery(final String val) {
    query = val;
  }

  /** Get query from last search
   *
   * @return count found in last search
   */
  public String getQuery() {
    return query;
  }

  /** Set last search result (segment)
   *
   * @param val
   */
  public void setSearchResult(final Collection<SearchResultEntry> val) {
    searchResult = val;
  }

  /**
   * @return last search result
   */
  public Collection<SearchResultEntry> getSearchResult() {
    return searchResult;
  }

  /** Set search limits
   *
   * @param val
   */
  public void setSearchLimits(final String val) {
    searchLimits = val;
  }

  /**
   * @return search limits
   */
  public String getSearchLimits() {
    return searchLimits;
  }

  /** Set previous page in search result
   * @param val
   */
  public void setPrevPage(final int val) {
    prevPage = val;
  }

  /**
   * @return previous page in search result
   */
  public int getPrevPage() {
    return prevPage;
  }

  /** Set current page in search result
   * @param val
   */
  public void setCurPage(final int val) {
    curPage = val;
  }

  /**
   * @return current page in search result
   */
  public int getCurPage() {
    return curPage;
  }

  /** Set next page in search result
   * @param val
   */
  public void setNextPage(final int val) {
    nextPage = val;
  }

  /**
   * @return next page in search result
   */
  public int getNextPage() {
    return nextPage;
  }

  /** Set number of pages in search result
   * @param val
   */
  public void setNumPages(final int val) {
    numPages = val;
  }

  /**
   * @return result set size for last search
   */
  public int getNumPages() {
    return numPages;
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
    if (debug) {
      debugMsg("Event duration=" + db);
    }

    return db;
  }

/*
  public void resetEventStartDate() {
    eventStartDate = null;
  }

  public void resetEventEndDate() {
    eventEndDate = null;
  }*/

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
   * @return TimeDateComponents used for labels
   */
  public TimeDateComponents getForLabels() {
    return getEventDates().getForLabels();
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
  public void reset(final ActionMapping mapping, final HttpServletRequest request) {
    viewTypeI = -1;
    date = null;
    today = null;

    //key.reset();

    editAuthUserType = 0;
  }

  /** Reset objects used to select event entities.
   *
   */
  public void resetSelectIds() {
    calendarId = new SelectId<String>(null, SelectId.AHasPrecedence);
    locId = new SelectId<String>(null, SelectId.AHasPrecedence);
    ctctId = new SelectId<String>(null, SelectId.AHasPrecedence);
  }

  /* ====================================================================
   *                Private methods
   * ==================================================================== */

  private BwPrincipal getPublicUser() throws Throwable {
    return calsvci.getUsersHandler().getUser(calsvci.getBasicSystemProperties().getPublicUser());
  }

  // ENUM
  private Collection<BwCategory> getCategoryCollection(final int kind,
                                                       final boolean forEventUpdate) {
    try {
      Collection<BwCategory> vals = null;

      if (kind == ownersEntity) {

        String appType = getAppType();
        if (appTypeWebsubmit.equals(appType) ||
            appTypeWebpublic.equals(appType) ||
            appTypeFeeder.equals(appType)) {
          // Use public
          vals = calsvci.getCategoriesHandler().get(getPublicUser().getPrincipalRef(), null);
        } else {
          // Current owner
          vals = calsvci.getCategoriesHandler().get();

          if (!publicAdmin() && forEventUpdate &&
              (getEvent() != null) &&
              (getEvent().getCategories() != null)) {
            for (BwCategory cat: getEvent().getCategories()) {
              if (!cat.getOwnerHref().equals(client.getCurrentPrincipalHref())) {
                vals.add(cat);
              }
            }
          }
        }
      } else if (kind == editableEntity) {
        vals = calsvci.getCategoriesHandler().getEditable();
      }

      if (vals == null) {
        // Won't need this with 1.5
        throw new Exception("Software error - bad kind " + kind);
      }

      return getCategoryCollator().getCollatedCollection(vals);
    } catch (Throwable t) {
      if (debug) {
        t.printStackTrace();
      }
      err.emit(t);
      return new ArrayList<BwCategory>();
    }
  }

  private Collection<BwLocation> getLocations(final int kind,
                                              final boolean forEventUpdate) {
    try {
      Collection<BwLocation> vals = null;

      if (kind == ownersEntity) {
        if (getSubmitApp()) {
          // Use public
          vals = calsvci.getLocationsHandler().get(getPublicUser().getPrincipalRef(), null);
        } else {
          // Current owner
          vals = calsvci.getLocationsHandler().get();

          if (!publicAdmin() && forEventUpdate && (getEvent() != null)) {
            BwLocation loc = getEvent().getLocation();

            if ((loc != null) &&
                (!loc.getOwnerHref().equals(client.getCurrentPrincipalHref()))) {
              vals.add(loc);
            }
          }
        }
      } else if (kind == editableEntity) {
        vals = calsvci.getLocationsHandler().getEditable();
      }

      if (vals == null) {
        // Won't need this with 1.5
        throw new Exception("Software error - bad kind " + kind);
      }

      return getLocationCollator().getCollatedCollection(vals);
    } catch (Throwable t) {
      if (debug) {
        t.printStackTrace();
      }
      err.emit(t);
      return new ArrayList<BwLocation>();
    }
  }

  private Collection<BwContact> getContactCollection(final int kind) {
    try {
      Collection<BwContact> vals = null;

      if (kind == ownersEntity) {
        if (getSubmitApp()) {
          // Use public
          vals = calsvci.getContactsHandler().get(getPublicUser().getPrincipalRef(), null);
        } else {
          // Current owner
          vals = calsvci.getContactsHandler().get();
        }
      } else if (kind == editableEntity) {
        vals = calsvci.getContactsHandler().getEditable();
      }

      // Won't need this with 1.5
      if (vals == null) {
        throw new Exception("Software error - bad kind " + kind);
      }

      return getContactCollator().getCollatedCollection(vals);
    } catch (Throwable t) {
      if (debug) {
        t.printStackTrace();
      }
      err.emit(t);
      return new ArrayList<BwContact>();
    }
  }

  private CollectionCollator<BwCalendar> getCalendarCollator() {
    if (calendarCollator == null) {
      calendarCollator = new CollectionCollator<BwCalendar>();
    }

    return calendarCollator;
  }

  private CollectionCollator<BwContact> getContactCollator() {
    if (contactCollator == null) {
      contactCollator = new CollectionCollator<BwContact>();
    }

    return contactCollator;
  }

  private CollectionCollator<BwCategory> getCategoryCollator() {
    if (categoryCollator == null) {
      categoryCollator = new CollectionCollator<BwCategory>();
    }

    return categoryCollator;
  }

  private CollectionCollator<BwLocation> getLocationCollator() {
    if (locationCollator == null) {
      locationCollator = new CollectionCollator<BwLocation>();
    }

    return locationCollator;
  }

  private boolean publicAdmin() {
    try {
      return getConfig().getPublicAdmin();
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
  }
}
