/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.client.rw.InOutBoxInfo;
import org.bedework.appcommon.SelectId;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calsvci.SchedulingI.FbResponses;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModule;

import org.apache.struts.upload.FormFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * User: mike Date: 3/11/21 Time: 23:53
 */
public class BwRWActionForm extends BwActionFormBase {
  /* ..............................................................
   *                       Uploads and exports
   * .............................................................. */

  private FormFile eventImageUpload;

  private FormFile uploadFile;

  /* ..............................................................
   *           Fields for creating or editing objects
   * .............................................................. */

  private boolean addingEvent;

  /** Event copy before mods applied - temp till we finish change table stuff
   */
  private BwEvent savedEvent;

  private Collection<AddEventResult> addEventResults;

  private String summary;

  private String description;

  private String eventLink;

  private String eventCost;

  private String transparency;

  private Attendees attendees;

  private FbResponses fbResponses;

  private FormattedFreeBusy formattedFreeBusy;

  private Collection<DateTimeFormatter> formattedRdates;

  private Collection<DateTimeFormatter> formattedExdates;

  /* ..............................................................
   *                       Views
   * .............................................................. */

  private BwView view;

  private String viewName;

  private boolean addingView;

  /* ..............................................................
   *                       Collections
   * .............................................................. */

  private BwCalendar meetingCal;

  private String parentCalendarPath;

  private SelectId<String> calendarId = new SelectId<>(null,
                                                       SelectId.AHasPrecedence);

  /* ..............................................................
   *                       Event properties
   * .............................................................. */

  private Collection<EventPropertiesReference> propRefs;

  /* ..............................................................
   *                   Categories
   * .............................................................. */

  /** True if we are adding a new category
   */
  private boolean addingCategory;

  private BwCategory category;

  private BwString categoryWord;
  private String categoryDesc;
  private String categoryStatus;

  /* ....................................................................
   *                   Contacts
   * .................................................................... */

  private String contactUid;

  private SelectId<String> ctctId = new SelectId<>(null,
                                                   SelectId.AHasPrecedence);

  /** True if we are adding a new contact
   */
  private boolean addingContact;

  private BwContact contact;

  private BwString contactName;

  private String contactStatus;

  /* ..............................................................
   *                   Location fields
   * .............................................................. */

  /** True if we are adding a new location
   */
  private boolean addingLocation;

  protected String locationUid;

  /** Location address
   */
  protected BwString locationAddress;

  /** Location subAddress
   */
  private BwString locationSubaddress;

  private BwLocation location;

  private String locationStatus;

  private SelectId<String> locId = new SelectId<>(null,
                                                  SelectId.AHasPrecedence);

  /* ..............................................................
   *                   Preferences
   * .............................................................. */

  /** Last email address used to mail message. By default set to
   * preferences value.
   */
  private String lastEmail;

  private String lastSubject;

  /* ..............................................................
   *                   public events submission
   * .............................................................. */

  private String snfrom;

  private String sntext;

  private String snsubject;

  /* ..............................................................
   *                   Notifications, Inbox
   * .............................................................. */

  private NotificationInfo notificationInfo;

  private InOutBoxInfo inBoxInfo;

  /* ==============================================================
   *                   Uploads and exports
   * ============================================================== */

  /**
   * @param val FormFile
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
  public void setUploadFile(final FormFile val) {
    uploadFile = val;
  }

  /**
   * @return FormFile
   */
  public FormFile getUploadFile() {
    return uploadFile;
  }

  /* ====================================================================
   *                   Events
   * ==================================================================== */

  /** Not set - invisible to jsp
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

  /** Return the encoded root of the submissions calendars
   *
   * @return String path.
   */
  public String getEncodedSubmissionRoot() {
    final String appType = getAppType();

    if (appTypeWebsubmit.equals(appType) ||
            appTypeWebadmin.equals(appType)) {
      try {
        return URLEncoder
                .encode(getConfig().getSubmissionRoot(),
                        StandardCharsets.UTF_8);
      } catch (final Throwable t) {
        getErr().emit(t);
      }
    }

    return "";
  }

  /** Save the Path of the soon-to-be parent
   *
   * @param val - path
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

  /* ==============================================================
   *                   Event properties
   * ============================================================== */

  /** Only implemented in admin
   * @param val Property references
   */
  public void setPropRefs(final Collection<EventPropertiesReference> val) {
    propRefs = val;
  }

  /** Only implemented in admin
   * @return property refs
   */
  public Collection<EventPropertiesReference> getPropRefs() {
    return propRefs;
  }

  /** Reset objects used to select event entities.
   *
   */
  public void resetSelectIds() {
    calendarId = new SelectId<>(null, SelectId.AHasPrecedence);
    locId = new SelectId<>(null, SelectId.AHasPrecedence);
    ctctId = new SelectId<>(null, SelectId.AHasPrecedence);
  }

  /* ==============================================================
   *                   Categories
   * ============================================================== */

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
    setCategoryWord(null);
    categoryDesc = null;
    if (val != null) {
      final BwString s = val.getWord();
      if (s != null) {
        setCategoryWord((BwString)s.clone());
      }
      categoryDesc = val.getDescriptionVal();
      setCategoryStatus(val.getStatus());
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
   * @param val new cat word
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
  public void setCategoryDescription(final String val) {
    categoryDesc = val;
  }

  /**
   * @return BwString  Category Desc value object
   */
  public String getCategoryDescription() {
    return categoryDesc;
  }

  /**
   * @param val status of category
   */
  public void setCategoryStatus(final String val) {
    categoryStatus = val;
  }

  /**
   * @return String  Category Status
   */
  public String getCategoryStatus() {
    return Util.checkNull(categoryStatus);
  }

  /* ====================================================================
   *                   Contacts
   * ==================================================================== */

  /** Not set - invisible to jsp
   *
   * @param val true if adding contact
   */
  public void assignAddingContact(final boolean val) {
    addingContact = val;
  }

  /**
   * @return boolean true if adding contact
   */
  public boolean getAddingContact() {
    return addingContact;
  }

  /**
   * @param val a contact
   */
  public void setContact(final BwContact val) {
    contact = val;
    contactName = null;
    contactUid = null;
    if (val != null) {
      contactUid = val.getUid();

      BwString s = val.getCn();
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
   * @param val name
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
   * @param val status
   */
  public void setContactStatus(final String val) {
    contactStatus = val;
  }

  /**
   * @return contact status
   */
  public String getContactStatus() {
    return Util.checkNull(contactStatus);
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
   * @param val uid
   */
  public void setAllContactId(final String val) {
    if (Util.checkNull(val) != null) {
      ctctId.setA(val);
    }
  }

  /** This is the current contact, usually out of the current event. It is
   * used to select a particular contact in select lists.
   * @see org.bedework.client.web.rw.BwRWActionForm#getContactUid()
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
   * @param val uid
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

  /** Contact uid for next action
   *
   * @param val uid
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

  /* ====================================================================
   *                   Locations
   * ==================================================================== */

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
        setLocationStatus(val.getAddress().getLang());
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
   * @return populated location object
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

  /** Not set - invisible to jsp
   *
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
  public void setLocationStatus(final String val) {
    locationStatus = val;
  }

  /**
   * @return location status
   */
  public String getLocationStatus() {
    return Util.checkNull(locationStatus);
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
    return Util.checkNull(locationUid);
  }

  /* ..............................................................
   *                   public events submission
   * .............................................................. */

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

  /* ==============================================================
   *                       Modules
   * ============================================================== */

  public BwModule newModule(final String name) {
    return new RwBwModule(name);
  }

  /**
   * @param val NotificationInfo
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
}
