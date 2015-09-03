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

import org.bedework.calfacade.BwEvent;

import java.io.Serializable;

/** Key to an event or events.
 *
 * @author Mike Douglass douglm - rpi.edu
 */
public class EventKey implements Serializable {
  private String colPath;

  private String guid;

  private String href;

  private String recurrenceId;

  private String name;

  private boolean forExport;

  /** Constructor
   *
   * @param colPath
   * @param guid
   * @param recurrenceId
   * @param forExport
   */
  public EventKey(final String colPath,
                  final String guid, final String recurrenceId,
                  final boolean forExport) {
    this.colPath = colPath;
    this.guid = guid;
    this.recurrenceId = recurrenceId;
    this.forExport = forExport;
  }

  /** Constructor
   *
   * @param colPath
   * @param name
   * @param forExport
   */
  public EventKey(final String colPath,
                  final String name,
                  final boolean forExport) {
    this.colPath = colPath;
    this.name = name;
    this.href = colPath + "/" + name;
    this.forExport = forExport;
  }

  /** Constructor - make an EventKey from an event
   *
   * @param ev
   * @param byName
   */
  public EventKey(final BwEvent ev, final boolean byName) {
    colPath = ev.getColPath();

    if (byName) {
      name = ev.getName();
    } else {
      guid = ev.getUid();
    }

    recurrenceId = ev.getRecurrenceId();
  }

  /** Make an eventkey from an href
   *
   * @param href with possible fragment id for recurrence
   * @param forExport true if we are exporting the events.
   */
  public EventKey(final String href,
                  final boolean forExport) {
    this.href = href;
    this.forExport = forExport;

    if (href == null) {
      return;
    }

    final int pos = href.lastIndexOf("/");
    if (pos < 0) {
      throw new RuntimeException("Bad href: " + href);
    }

    final int fragPos = href.lastIndexOf("#");

    if (fragPos < pos) {
      name = href.substring(pos + 1);
      recurrenceId = null;
    } else {
      name = href.substring(pos + 1, fragPos);
      recurrenceId = href.substring(fragPos + 1);
    }

    colPath = href.substring(0, pos);
  }

  /** Set the event's collection path
   *
   * @param val    String event's name
   */
  public void setColPath(final String val) {
    colPath = val;
  }

  /** Get the event's collection path.
   *
   * @return String   event's name
   */
  public String getColPath() {
    return colPath;
  }

  /** Set the event's guid
   *
   * @param val    String event's guid
   */
  public void setGuid(final String val) {
    guid = val;
  }

  /** Get the event's guid
   *
   * @return String   event's guid
   */
  public String getGuid() {
    return guid;
  }

  /** Set the event's recurrence id
   *
   *  @param val     recurrence id
   */
  public void setRecurrenceId(final String val) {
     recurrenceId = val;
  }

  /** Get the event's recurrence id
   *
   * @return the event's recurrence id
   */
  public String getRecurrenceId() {
    return recurrenceId;
  }

  /** Set the event's name
   *
   * @param val    String event's name
   */
  public void setName(final String val) {
    name = val;
  }

  /** Get the event's name.
   *
   * @return String   event's name
   */
  public String getName() {
    return name;
  }

  /** Get the event's href
   *
   * @return String   event's href
   */
  public String getHref() {
    return href;
  }

  /** True if we should retrieve the event to export
   *
   * @param val
   */
  public void setForExport(final boolean val) {
    forExport = val;
  }

  /**
   * @return boolean
   */
  public boolean getForExport() {
    return forExport;
  }
}
