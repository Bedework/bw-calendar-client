/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.appcommon.DateTimeFormatter;
import org.bedework.calfacade.BwEvent;
import org.bedework.calsvci.SchedulingI.FbResponses;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.FormattedFreeBusy;

import org.apache.struts.upload.FormFile;

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
}
