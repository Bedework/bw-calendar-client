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

import org.bedework.appcommon.CalendarInfo;
import org.bedework.calsvci.CalSvcI;

import edu.rpi.sss.util.log.MessageEmit;

import java.io.Serializable;

/** The dates (and/or duration which define when an entity happens. These are
 * stored in objects which allow manipulation of individual date and time
 * components.
 *
 * This is the base class for entity specific classes.
 */
public class EntityDates implements Serializable {
  protected CalSvcI svci;
  private CalendarInfo calInfo;

  protected boolean hour24;

  protected int minIncrement;

  protected MessageEmit err;

  /** Used for generating labels
   */
  private TimeDateComponents forLabels;

  /** Constructor
   *
   * @param svci
   * @param calInfo
   * @param hour24
   * @param minIncrement
   * @param err
   */
  public EntityDates(final CalSvcI svci, final CalendarInfo calInfo,
                     final boolean hour24, final int minIncrement,
                     final MessageEmit err) {
    this.svci = svci;
    this.calInfo = calInfo;
    this.hour24 = hour24;
    this.minIncrement = minIncrement;
    this.err = err;

  }

  /**
   * @return  TimeDateComponents for labels
   */
  public TimeDateComponents getForLabels() {
    if (forLabels == null) {
      getNowTimeComponents();
    }

    return forLabels;
  }

  /** Return an initialised TimeDateComponents representing now
   *
   * @return TimeDateComponents  initialised object
   */
  public TimeDateComponents getNowTimeComponents() {
    try {
      TimeDateComponents tc = new TimeDateComponents(calInfo, minIncrement,
                                                     hour24);

      tc.setNow();

      forLabels = tc;

      return tc;
    } catch (Throwable t) {
      err.emit(t);
      return null;
    }
  }
}

