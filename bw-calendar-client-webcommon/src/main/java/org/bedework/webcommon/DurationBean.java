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

import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.exc.CalFacadeException;

/** Class representing a duration for web applications. This takes String values
 * for the properties and validates them.
 *
 * @author Mike Douglass   douglm@bedework.edu
 *  @version 1.0
 */
public class DurationBean extends BwDuration {
  /** */
  public static final String weekDuration = "weeks";
  /** */
  public static final String dayTimeDuration = "daytime";

  /** One of the above */
  private String type;

  boolean valueError;

  /** Constructor
   *
   */
  public DurationBean() {
  }

  /** Set the type of duration. It can either represent a value in weeks or
   * in days/hours/minutes/seconds. (Don't blame us - it's in the rfc).
   *
   * @param val    String type
   */
  public void setType(String val) {
    type = val;
    if (dayTimeDuration.equals(val)) {
      setWeeks(0);
    }
  }

  /** Get the type
   *
   * @return String    the type
   */
  public String getType() {
    return type;
  }

  /** Set the days
   *
   * @param val    String days
   */
  public void setDaysStr(String val) {
    setDays(makeInt(val));
  }

  /** Get the days
   *
   * @return String    the days
   */
  public String getDaysStr() {
    return String.valueOf(getDays());
  }

  /** Set the hours
   *
   * @param val    String hours
   */
  public void setHoursStr(String val) {
    setHours(makeInt(val));
  }

  /** Get the hours
   *
   * @return String    the hours
   */
  public String getHoursStr() {
    return String.valueOf(getHours());
  }

  /** Set the minutes
   *
   * @param val    String minutes
   */
  public void setMinutesStr(String val) {
    setMinutes(makeInt(val));
  }

  /** Get the minutes
   *
   * @return String    the minutes
   */
  public String getMinutesStr() {
    return String.valueOf(getMinutes());
  }

  /** Set the seconds
   *
   * @param val    String seconds
   */
  public void setSecondsStr(String val) {
    setSeconds(makeInt(val));
  }

  /** Get the seconds
   *
   * @return String    the seconds
   */
  public String getSecondsStr() {
    return String.valueOf(getSeconds());
  }

  /** Set the weeks
   *
   * @param val    String weeks
   */
  public void setWeeksStr(String val) {
    setWeeks(makeInt(val));
  }

  /** Get the weeks
   *
   * @return String    the weeks
   */
  public String getWeeksStr() {
    return String.valueOf(getWeeks());
  }

  /** Flag a negative duration
   *
   * @param val    boolean negative
   */
  public void setNegativeStr(String val) {
    setNegative(makeBool(val));
  }

  /** Get the negative flag
   *
   * @return boolean    the negative
   */
  public String getNegativeStr() {
    return String.valueOf(getNegative());
  }

  /**
   *
   */
  public void reset() {
    valueError = false;
  }

  /**
   * @return truefor value error
   */
  public boolean getValueError() {
    return valueError;
  }

  /* ====================================================================
   *                        Convenience methods
   * ==================================================================== */

  /**
   * @param val
   * @return duration bean
   * @throws CalFacadeException
   */
  public static DurationBean makeDurationBean(String val) throws CalFacadeException {
    DurationBean db = new DurationBean();

    populate(db, val);

    if (db.getWeeks() != 0) {
      db.setType(weekDuration);
    } else {
      db.setType(dayTimeDuration);
    }

    return db;
  }

  /**
   * @return one hour
   * @throws CalFacadeException
   */
  public static DurationBean makeOneHour() throws CalFacadeException {
    DurationBean db = new DurationBean();

    db.setType(dayTimeDuration);
    db.setHours(1);

    return db;
  }

  /**
   * @return one day
   * @throws CalFacadeException
   */
  public static DurationBean makeOneDay() throws CalFacadeException {
    DurationBean db = new DurationBean();

    db.setType(dayTimeDuration);
    db.setDays(1);

    return db;
  }

  /* ====================================================================
   *                        Private methods
   * ==================================================================== */

  private int makeInt(String val) {
    try {
      return Integer.parseInt(val);
    } catch (Throwable t) {
      valueError = true;
      return 0;
    }
  }

  private boolean makeBool(String val) {
    try {
      return Boolean.valueOf(val).booleanValue();
    } catch (Throwable t) {
      valueError = true;
      return false;
    }
  }
}

