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

import java.io.Serializable;

/** This class will be exposed to JSP and defines the current state of
 * an event we are adding or modifying.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class EventState implements Serializable {
  /* ..............................................................
   *                   Alarm fields
   * .............................................................. */

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

  private String email;

  private String subject;

  /*
  public EventState(BwModuleState mstate) {
    this.mstate = mstate;
  }
  */
  public void reset(final TimeDateComponents now) {
    triggerDateTime = now;
  }

  /* ============================================================
   *                   Alarm fields
   * ============================================================ */

  /**
   * @return time date
   */
  public TimeDateComponents getTriggerDateTime() {
    return triggerDateTime;
  }

  /**
   * @param val duration
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
   * @param val rel start
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
   * @param val duration
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
   * @param val repeat
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
   * @param val true/false
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

  /**
   * @param val email address
   */
  public void setEmail(final String val) {
    email = val;
  }

  /**
   * @return email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param val subject
   */
  public void setSubject(final String val) {
    subject = val;
  }

  /**
   * @return email subject
   */
  public String getSubject() {
    return subject;
  }
}

