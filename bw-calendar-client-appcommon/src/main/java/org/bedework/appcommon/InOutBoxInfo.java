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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.CalSvcI;

import edu.rpi.cmt.access.Acl.CurrentAccess;

import java.io.Serializable;
import java.util.Collection;

/** Class to hold info about a user's inbox or outbox.
 *
 * @author Mike Douglass   douglm@rpi.edu
 *  @version 1.0
 */
public class InOutBoxInfo implements Serializable {
  private CalSvcI svci;

  private boolean inbox; // false for outbox.

  private boolean changed; // Set true whenever the status changes.

  private String tagVal;

  private int numActive;

  private int numProcessed;

  /* Access set for this box */
  private CurrentAccess currentAccess;

  /** The events in the in/outbox
   */
  private FormattedEvents events;

  /** Constructor
   *
   * @param svci
   * @param inbox  boolean
   * @throws CalFacadeException
   */
  public InOutBoxInfo(CalSvcI svci,
                      boolean inbox) throws CalFacadeException {
    this.svci = svci;
    this.inbox = inbox;

    refresh(true);
  }

  /** Refresh the information
   *
   * @param all
   * @throws CalFacadeException
   */
  public void refresh(boolean all) throws CalFacadeException {
    int calType;
    if (inbox) {
      calType = BwCalendar.calTypeInbox;
    } else {
      calType = BwCalendar.calTypeOutbox;
    }

    BwCalendar cal = svci.getCalendarsHandler().getSpecial(calType, false);

    if (cal == null) {
      // Cannot go away - never existed - no change.
      changed = false;
      return;
    }

    if (!all) {
      synchronized (this) {
        currentAccess = cal.getCurrentAccess();

        if ((tagVal != null) && (tagVal.equals(cal.getLastmod().getTagValue()))) {
          changed = false;
          return;
        }

        tagVal = cal.getLastmod().getTagValue();
      }
    }

    // XXX need to be able to store and retrieve other types of info.

    RecurringRetrievalMode rrm =
      new RecurringRetrievalMode(Rmode.overrides);
    Collection<EventInfo> evs = svci.getEventsHandler().getEvents(cal,
                                                                  null, null,
                                                                  null,
                                                                  null, // retrieveList
                                                                  rrm);
    numActive = 0;
    numProcessed = 0;

    for (EventInfo ei: evs) {
      BwEvent ev = ei.getEvent();

      if (ev.getScheduleState() == BwEvent.scheduleStateNotProcessed) {
        numActive++;
      }
    }

    events = new FormattedEvents(svci, evs);
    changed = true;
  }

  /**
   * @param val boolean
   */
  public void setInbox(boolean val) {
    inbox = val;
  }

  /**
   * @return boolean
   */
  public boolean getInbox() {
    return inbox;
  }

  /**
   * @param val boolean
   */
  public void setChanged(boolean val) {
    changed = val;
  }

  /**
   * @return boolean
   */
  public boolean getChanged() {
    return changed;
  }

  /** Set the number active.
   *
   * @param val     numactive
   */
  public void setNumActive(int val) {
    numActive = val;
  }

  /** Get the number active.
   *
   * @return val     numactive
   */
  public int getNumActive() {
    return numActive;
  }

  /** Set the number Processed.
   *
   * @param val     numProcessed
   */
  public void setNumProcessed(int val) {
    numProcessed = val;
  }

  /** Get the number Processed.
   *
   * @return val     numProcessed
   */
  public int getNumProcessed() {
    return numProcessed;
  }

  /** Get the boxes current access
   *
   * @return CurrentAccess
   */
  public CurrentAccess getCurrentAccess() {
    return currentAccess;
  }

  /** Get the events. May be null.
   *
   * @return FormattedEvents or null.
   */
  public FormattedEvents getEvents() {
    return events;
  }
}
