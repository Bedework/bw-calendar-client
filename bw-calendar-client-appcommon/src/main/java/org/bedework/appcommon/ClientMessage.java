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

import java.io.Serializable;

/**
 * Define message property codes emitted by clients. Suggested English Language
 * is supplied in the comment for clarification only.
 *
 * @author Mike Douglass
 *
 */
public interface ClientMessage extends Serializable {
  /** Prefix for all these errors */
  public static final String prefix = "org.bedework.client.message.";

  /** Calendar added. */
  public static final String addedCalendar = prefix + "added.calendar";

  /** n categories added. */
  public static final String addedCategories = prefix + "added.categories";

  /** Contact added. */
  public static final String addedContact = prefix + "added.contact";

  /** n eventrefs added. */
  public static final String addedEventrefs = prefix + "added.eventrefs";

  /** n events added. */
  public static final String addedEvents = prefix + "added.events";

  /** Folder added. */
  public static final String addedFolder = prefix + "added.folder";

  /** n locations added. */
  public static final String addedLocations = prefix + "added.locations";

  /** n tasks added. */
  public static final String addedTasks = prefix + "added.tasks";

  /** Action cancelled. */
  public static final String cancelled = prefix + "cancelled";

  /** Administrator removed. */
  public static final String deletedAuthuser = prefix + "deleted.authuser";

  /** Calendar/folder deleted. */
  public static final String deletedCalendar = prefix + "deleted.calendar";

  /** Calendar/folder moved. */
  public static final String movedCalendar = prefix + "moved.calendar";

  /** Category deleted. */
  public static final String deletedCategory = prefix + "deleted.category";

  /** Contact deleted. */
  public static final String deletedContact = prefix + "deleted.contact";

  /** Events deleted. */
  public static final String deletedEvents = prefix + "deleted.events";

  /** Group deleted. */
  public static final String deletedGroup = prefix + "deleted.group";

  /** n locations deleted. */
  public static final String deletedLocations = prefix + "deleted.locations";

  /** Subscription deleted. */
  public static final String deletedSubscription = prefix + "deleted.subscription";

  /** View deleted. */
  public static final String deletedView = prefix + "deleted.view";
  
  /** Resource deleted. */
  public static final String deletedResource = prefix + "deleted.resource";

  /** Freebusy information is unavailable for x. */
  public static final String freebusyUnavailable = prefix + "freebusy.unavailable";

  /** Timezones successfully imported. */
  public static final String importedTimezones = prefix + "imported.timezones";

  /** Timezones successfully fixed. */
  public static final String fixedTimezones = prefix + "fixed.timezones";

  /** Event has been mailed. */
  public static final String mailedEvent = prefix + "mailed.event";

  /** Calendar refreshed. */
  public static final String refreshedCalendar = prefix + "refreshed.calendar";

  /** Schedule added. */
  public static final String scheduleAdded = prefix + "schedule.added";

  /** Schedule calendar copy deleted. */
  public static final String scheduleColCopyDeleted = prefix + "schedule.colcopy.deleted";

  /** Schedule deferred. */
  public static final String scheduleDeferred = prefix + "schedule.deferred";

  /** Schedule ignored. */
  public static final String scheduleIgnored = prefix + "schedule.ignored";

  /** Schedule rescheduled. */
  public static final String scheduleRescheduled = prefix + "schedule.rescheduled";

  /** Schedule send. */
  public static final String scheduleSent = prefix + "schedule.sent";

  /** Schedule updated. */
  public static final String scheduleUpdated = prefix + "schedule.updated";

  /** Alarm has been set. */
  public static final String setAlarm = prefix + "set.alarm";

  /** Administrator updated. */
  public static final String updatedAuthuser = prefix + "updated.authuser";

  /** Calendar updated. */
  public static final String updatedCalendar = prefix + "updated.calendar";

  /** Category updated. */
  public static final String updatedCategory = prefix + "updated.category";

  /** Contact updated. */
  public static final String updatedContact = prefix + "updated.contact";

  /** Event updated. */
  public static final String updatedEvent = prefix + "updated.event";

  /** Event updated. */
  public static final String updatedEvents = prefix + "updated.events";

  /** Folder updated. */
  public static final String updatedFolder = prefix + "updated.folder";

  /** Group updated. */
  public static final String updatedGroup = prefix + "updated.group";

  /** Location updated. */
  public static final String updatedLocation = prefix + "updated.location";

  /** Preferences updated. */
  public static final String updatedPrefs = prefix + "updated.prefs";

  /** System preferences updated. */
  public static final String updatedSyspars = prefix + "updated.syspars";

  /** Task updated. */
  public static final String updatedTask = prefix + "updated.task";

  /** User information updated. */
  public static final String updatedUserinfo = prefix + "updated.userinfo";
}
