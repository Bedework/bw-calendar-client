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

import org.bedework.calfacade.BwEvent;

import edu.rpi.cmt.calendar.IcalDefs;

import java.util.Calendar;

/** A class to check various data fields for consistency and correctness
 * Based on DataChecker class by Greg Barnes
 *
 * @author Mike Douglass   douglm@rpi.edu
 * @author Greg Barnes
 * @version 1.2
 */
public class CheckData {
  private static final int DATE_STRING_LENGTH = 8;
  private static final int YEAR_START_INDEX = 0;
  private static final int MONTH_START_INDEX = 4;
  private static final int DAY_START_INDEX = 6;

  private static final int maxMonth;
  private static final int maxDay;

  static {
    /** Used to obtain a number of values we might need.
     */
    Calendar refCal = Calendar.getInstance();

    maxMonth = refCal.getMaximum(Calendar.MONTH);
    maxDay = refCal.getMaximum(Calendar.DATE);
  }

  private CheckData() {}  // No instantiation allowed.

  /** Check that a date string, purportedly in YYYYMMDD format, actually is
   * and represents a valid date.
   *
   * Note that not all errors are flagged.  In particular, days that
   * are too large for a given month (e.g., Feb 30) or months too large
   * for a given year (not possible in the Gregorian calendar, but
   * perhaps in others) are not flagged as long as the day/month
   * represent valid values in *some* month/year.  These 'overflow'
   * dates are handled per the explanation in the
   * <code>java.util.Calendar</code> documentation (e.g., Feb 30, 1999
   * becomes Mar 2, 1999).
   *
   *  @param val       String to check. Should be of form yyyymmdd
   *  @return boolean  true for OK
   */
  public static boolean checkDateString(final String val) {
    if ((val == null) || (val.length() != DATE_STRING_LENGTH)) {
      return false;
    }

    for (int i = 0; i < DATE_STRING_LENGTH; i++) {
      if ((val.charAt(i) < '0') || (val.charAt(i) > '9')) {
        return false;
      }
    }

    int dayNum = Integer.parseInt(val.substring(DAY_START_INDEX,
                                                DAY_START_INDEX + 2));

    /* java.util.Date has 0 <= month <= 11 */
    int monthNum = Integer.parseInt(val.substring(MONTH_START_INDEX,
                                                  MONTH_START_INDEX + 2)) - 1;

    int yearNum = Integer.parseInt(val.substring(YEAR_START_INDEX,
                                                 YEAR_START_INDEX + 4));


    if ((monthNum < 0) ||
        (dayNum < 1) ||
        (monthNum > maxMonth) ||
        (dayNum > maxDay) ||
        (yearNum < 1)) {    // there was no year zero
      return false;
    }

    return true;
  }

  /** Check for valid transparency setting
   *
   * @param val
   * @return boolean true for ok
   */
  public static boolean checkTransparency(final String val) {
    return (val == null) ||  // Defaulted
           IcalDefs.transparencyOpaque.equals(val) ||
           IcalDefs.transparencyTransparent.equals(val);
  }

  /** Check for valid status setting
   *
   * @param val
   * @return boolean true for ok
   */
  public static boolean checkStatus(final String val) {
    return (val == null) ||  // Defaulted
           BwEvent.statusConfirmed.equals(val) ||
           BwEvent.statusTentative.equals(val) ||
           BwEvent.statusCancelled.equals(val);
  }
}
