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

import org.bedework.appcommon.TimeView;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.responses.CollectionsResponse;
import org.bedework.util.webaction.Request;

import java.io.Serializable;
import java.util.Collection;

/** This interface represents a session for the Bedework web interface.
 * Some user state will be retained here.
 * We also provide a number of methods which act as the interface between
 * the web world and the calendar world.
 *
 * @author Mike Douglass   douglm   rpi.edu
 */
public interface BwSession extends Serializable {
  String changeTokenAttr = "bw_change_token";

  /* =============================================================
   *                     Property methods
   *  ============================================================= */

  void finishInit(AuthProperties authpars);

  /**
   * Call to reset state and flush objects.
   * @param req - current request
   */
  void reset(Request req);

  /** This may not be entirely correct so should be used with care.
   * Really just provides some measure of use.
   *
   * @return long session number
   */
  long getSessionNum();

  /**
   *
   * @return true if this is first request for this session.
   */
  boolean isNewSession();

  /**
   * Flag this as not new
   */
  void resetNewSession();

  /** Prepare state of session for render
   *
   * @param req - current request
   */
  void prepareRender(BwRequest req);

  /** Flush any cached public info
   *
   */
  void flushPublicCache();
  /**
   * 
   * @param request current 
   * @return Collections object populated
   */
  CollectionsResponse getCollections(BwRequest request);
  
  /** Embed the current users calendars. For admin or guest mode this is the
   * same as calling embedPublicCalendars.
   *
   * <p>For the websubmit application we embed the root of the submission
   * calendars.
   *
   * @param request - current request
   */
  void embedCollections(BwRequest request);

  /**
   *
   * @param req - current request
   */
  void embedFilters(BwRequest req);

  /** Get the current view according to the current setting of curViewPeriod.
   * May be called when we change the view or if we need a refresh
   *
   * @param req - current request
   * @return current time view
   */
  TimeView getCurTimeView(BwRequest req);

  /**
   * @return AuthProperties object
   */
  AuthProperties getAuthpars();

  /** Make these available
   *
   * @param request - current request
   */
  void embedUserCollections(BwRequest request);

  /** Return a list of calendars in which calendar objects can be
   * placed by the current user.
   *
   * <p>Caldav currently does not allow collections inside collections so that
   * calendar collections are the leaf nodes only.
   *
   * @param request - current request
   */
  void embedAddContentCalendarCollections(BwRequest request);

  /* ==============================================================
   *                   Categories
   * ============================================================== */

  /** Embed the list of categories for this owner. Return a null list for
   * exceptions or no categories. For guest mode or public admin this is the
   * same as getPublicCategories. This is the method to call unless you
   * specifically want a list of public categories (for search of public events
   * perhaps.)
   *
   * @param request - current request
   * @param refresh - true to force refresh
   * @param kind of entity
   * @return collection of categories
   */
  Collection<BwCategory> embedCategories(BwRequest request,
                                         boolean refresh,
                                         int kind);

  /**
   * @param request - current request
   * @param kind of entity
   * @param forEventUpdate true if we are about to update an event
   * @return collection - never null.
   */
  Collection<BwCategory> getCategoryCollection(
          BwRequest request,
          int kind,
          final boolean includeArchived,
          boolean forEventUpdate);

  /* Kind of entity we are referring to */

  int ownersEntity = 1;
  int editableEntity = 2;
  int preferredEntity = 3;
  int defaultEntity = 4;

  /**
   * @param request - current request
   * @param kind of entity
   * @param forEventUpdate if we are updating an event
   * @return the contacts
   */
  Collection<BwContact> getContacts(BwRequest request,
                                    int kind,
                                    final boolean includeArchived,
                                    boolean forEventUpdate);

  /**
   *
   * @param request - current request
   * @param kind of entity
   */
  void embedContactCollection(BwRequest request,
                              int kind);

  /**
   * @param request - current request
   */
  void embedViews(BwRequest request);

  /**
   * @param request - current request
   * @param kind of entity
   * @param includeArchived true to add deleted entities
   * @param forEventUpdate if we are updating an event
   * @return the locations
   */
  Collection<BwLocation> getLocations(
          BwRequest request,
          int kind,
          boolean includeArchived,
          boolean forEventUpdate);

  /** Called by jsp when editing an event
   *
   * @param request - current request
   * @param kind of entity
   */
  void embedLocations(BwRequest request,
                      int kind);
}

