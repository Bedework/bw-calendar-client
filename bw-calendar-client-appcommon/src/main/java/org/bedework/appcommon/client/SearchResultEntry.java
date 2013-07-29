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
package org.bedework.appcommon.client;

import org.bedework.appcommon.EventFormatter;
import org.bedework.calfacade.BwCalendar;

import java.io.Serializable;

/** Result entry from a search
 *
 * @author Mike Douglass
 *
 */
public class SearchResultEntry implements Serializable {
  private float score;

  private EventFormatter event;

  private BwCalendar cal;

  /** Constructor
  *
  * @param event
  * @param score
  */
  public SearchResultEntry(EventFormatter event, float score) {
    this.event = event;
    this.score = score;
  }

  /** Constructor
   *
   * @param cal
   * @param score
   */
  public SearchResultEntry(BwCalendar cal, float score) {
    this.cal = cal;
    this.score = score;
  }

  /** Non-null if we got an event
   *
   * @return EventInfo object
   */
  public EventFormatter getEvent() {
    return event;
  }

  /** Non-null if we got a calendar
   *
   * @return BwCalendar
   */
  public BwCalendar getCal() {
    return cal;
  }

  /** Score for this record
   *
   * @return float score
   */
  public float getScore() {
    return score;
  }
}
