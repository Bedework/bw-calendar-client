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
package org.bedework.client.rw;

import org.bedework.access.CurrentAccess;
import org.bedework.appcommon.FormattedEvents;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.svc.EventInfo;

import java.io.Serializable;
import java.util.Collection;

/** Class to hold info about a user's inbox or outbox.
 *
 * @author Mike Douglass   douglm rpi.edu
 *  @version 1.0
 */
public class InOutBoxInfo implements Serializable {
  private boolean inbox; // false for outbox.

  private String colPath;

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
   * @param cl client
   * @param inbox  boolean
) {   */
  public InOutBoxInfo(final Client cl,
                      final boolean inbox) {
    this.inbox = inbox;

    refresh(cl, true);
  }

  /** Refresh the information
   *
   * @param cl client
   * @param all true to fetch all events.
) {   */
  public void refresh(final Client cl,
                      final boolean all) {
    final int calType;
    if (inbox) {
      calType = BwCollection.calTypeInbox;
    } else {
      calType = BwCollection.calTypeOutbox;
    }

    final BwCollection col = cl.getSpecial(calType, false);

    if (col == null) {
      // Cannot go away - never existed - no change.
      changed = false;
      return;
    }

    colPath = col.getPath();

    if (!all) {
      synchronized (this) {
        currentAccess = col.getCurrentAccess();

        if ((tagVal != null) &&
                (tagVal.equals(col.getLastmod().getTagValue()))) {
          changed = false;
          return;
        }

        tagVal = col.getLastmod().getTagValue();
      }
    }

    // XXX need to be able to store and retrieve other types of info.

    final String fexpr = "(colPath='" + colPath + "')";

    final Collection<EventInfo> evs = cl.getEvents(fexpr,
                                                   null, null,
                                                   false);
    numActive = 0;
    numProcessed = 0;

    for (final EventInfo ei: evs) {
      final BwEvent ev = ei.getEvent();

      if (ev.getScheduleState() == BwEvent.scheduleStateNotProcessed) {
        numActive++;
      }
    }

    events = new FormattedEvents(cl, evs);
    changed = true;
  }

  /**
   * @param val boolean
   */
  public void setInbox(final boolean val) {
    inbox = val;
  }

  /**
   * @return boolean
   */
  public boolean getInbox() {
    return inbox;
  }

  /**
   *
   * @return path of collection
   */
  public String getColPath() {
    return colPath;
  }

  /**
   * @param val boolean
   */
  public void setChanged(final boolean val) {
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
  public void setNumActive(final int val) {
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
  public void setNumProcessed(final int val) {
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
