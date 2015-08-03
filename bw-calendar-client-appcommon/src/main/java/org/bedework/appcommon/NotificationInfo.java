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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.CalFacadeException;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Class to hold info about a user's notifications.
 *
 * @author Mike Douglass   douglm@rpi.edu
 *  @version 1.0
 */
public class NotificationInfo implements Serializable {
  protected boolean debug = false;

  private transient Logger log;

  private long lastRefresh;

  private final long minRefresh = 15 * 1000; // 15 seconds

  private boolean changed; // Set true whenever the status changes.

  private String tagVal;

  private final Map<String, NotifyResource> notes = new HashMap<>();

  /** Constructor
   *
   * @throws CalFacadeException
   */
  public NotificationInfo() throws CalFacadeException {
    debug = getLogger().isDebugEnabled();
  }

  /** Refresh the information
   *
   * @param cl the client
   * @param force - get it whatever the tag says
   * @throws CalFacadeException
   */
  public void refresh(final Client cl,
                      final boolean force) throws CalFacadeException {
    if ((lastRefresh != 0) &&
        ((System.currentTimeMillis() - lastRefresh) < minRefresh)) {
      // Too soon, too soon.
      changed = false;
      return;
    }

    lastRefresh = System.currentTimeMillis();

    final int calType = BwCalendar.calTypeNotifications;

    final BwCalendar col = cl.getSpecial(calType, false);

    if (col == null) {
      // Cannot go away - never existed - no change.
      changed = false;
      return;
    }

    if (!force) {
      synchronized (this) {
        if ((tagVal != null) &&
                (tagVal.equals(col.getLastmod().getTagValue()))) {
          changed = false;
          return;
        }

        tagVal = col.getLastmod().getTagValue();
      }
    }

    /* The notification folder contains a possibly large number of notification
     * resources. We should retrieve a max number of the latest ones.
     */

    final Collection<BwResource> rs =
            cl.getAllResources(col.getPath());

    final Map<String, NotifyResource> toDelete =
        new HashMap<>(notes);

    for (final BwResource r: rs) {
      final String rname = r.getName();

      toDelete.remove(rname);

      final NotifyResource mapRi = notes.get(rname);

      if ((mapRi != null) &&
          NotifyResource.makeTag(r).equals(mapRi.getTag())) {
        continue;
      }

      cl.getResourceContent(r);

      try {
        final NotifyResource ri = new NotifyResource(r);
        notes.put(rname, ri);
      } catch (final Throwable t) {
        if (debug) {
          error(t);
        }

        error(t.getLocalizedMessage());
      }

      changed = true;
    }

    /* Remove the resources we didn't see */
    for (final String n: toDelete.keySet()) {
      notes.remove(n);
    }
  }

  /**
   * @return boolean
   */
  public boolean getChanged() {
    return changed;
  }

  /** Get the number Processed.
   *
   * @return size of notification table
   */
  public int size() {
    return notes.size();
  }

  /** Get the notification.
   *
   * @return Collection of notifications.
   */
  public Collection<NotifyResource> getNotifications() {
    return notes.values();
  }

  /** Get a logger for messages
   *
   * @return Logger
   */
  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  /**
   * @param msg the message
   */
  private void error(final String msg) {
    getLogger().error(msg);
  }

  /**
   * @param t - the exception
   */
  private void error(final Throwable t) {
    getLogger().error(t);
  }
}
