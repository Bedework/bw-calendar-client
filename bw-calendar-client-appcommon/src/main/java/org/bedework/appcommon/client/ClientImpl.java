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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.Categories;

import edu.rpi.cmt.access.Ace;

import java.util.Collection;

/**
 * User: douglm Date: 6/27/13 Time: 2:05 PM
 */
public class ClientImpl extends ROClientImpl {
  private boolean publicAdmin;

  public ClientImpl(final CalSvcI svci,
                    final boolean publicAdmin,
                    final boolean publicView) {
    super(svci, publicView);
    this.publicAdmin = publicAdmin;
  }

  /* ------------------------------------------------------------
   *                     Principals
   * ------------------------------------------------------------ */

  @Override
  public BwPrincipal getOwner() throws CalFacadeException {
    if (publicAdmin) {
      return svci.getUsersHandler().getPublicUser();
    }

    return getCurrentPrincipal();
  }

  @Override
  public void updateAdminPrefs(final boolean remove,
                               final BwCalendar col,
                               final BwCategory cat,
                               final BwLocation loc,
                               final BwContact contact)
          throws CalFacadeException {
    svci.getPrefsHandler().updateAdminPrefs(remove, col, cat, loc, contact);
  }

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  @Override
  public BwCalendar addCollection(final BwCalendar val,
                                  final String parentPath)
          throws CalFacadeException {
    return svci.getCalendarsHandler().add(val, parentPath);
  }

  @Override
  public void updateCollection(final BwCalendar col)
          throws CalFacadeException {
    svci.getCalendarsHandler().update(col);
  }

  @Override
  public void moveCollection(final BwCalendar val,
                             final BwCalendar newParent)
          throws CalFacadeException {
    svci.getCalendarsHandler().move(val, newParent);
  }

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  @Override
  public void updateCategory(final BwCategory val)
          throws CalFacadeException {
    svci.getCategoriesHandler().update(val);
  }

  private static class ClDeleteCategoryResult implements
          Client.DeleteCategoryResult {
    private boolean deleted;

    private Collection<EventPropertiesReference> references;

    private ClDeleteCategoryResult(final boolean deleted) {
      this.deleted = deleted;
    }

    private ClDeleteCategoryResult(final Collection<EventPropertiesReference> references) {
      this.references = references;
    }

    @Override
    public boolean getDeleted() {
      return deleted;
    }

    @Override
    public Collection<EventPropertiesReference> getReferences() {
      return references;
    }
  }
  @Override
  public Client.DeleteCategoryResult deleteCategory(final BwCategory val)
          throws CalFacadeException {
    Categories cats = svci.getCategoriesHandler();
    int delResult = cats.delete(val);

    if (delResult == 2) {
      return new ClDeleteCategoryResult(cats.getRefs(val));
    }

    if (delResult == 1) {
      return null;
    }

    return new ClDeleteCategoryResult(true);
  }

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  @Override
  public BwContact ensureContactExists(final BwContact val,
                                       final String ownerHref)
          throws CalFacadeException {
    return svci.getContactsHandler().ensureExists(val, ownerHref).entity;
  }

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  @Override
  public void claimEvent(final BwEvent ev) throws CalFacadeException {
    svci.getEventsHandler().claim(ev);
  }

  @Override
  public EventInfo.UpdateResult addEvent(final EventInfo ei,
                                         final boolean noInvites,
                                         final boolean scheduling,
                                         final boolean rollbackOnError)
          throws CalFacadeException {
    return svci.getEventsHandler().add(ei, noInvites, scheduling, rollbackOnError);
  }

  @Override
  public EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                            final boolean noInvites,
                                            final String fromAttUri)
          throws CalFacadeException {
    return svci.getEventsHandler().update(ei, noInvites, fromAttUri);
  }

  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  @Override
  public ScheduleResult schedule(final EventInfo ei,
                                 final int method,
                                 final String recipient,
                                 final String fromAttUri,
                                 final boolean iSchedule)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  @Override
  public boolean removeViewCollection(final String name,
                                      final String path)
          throws CalFacadeException {
    return svci.getViewsHandler().removeCollection(name,
                                                   path);
  }

  @Override
  public void moveContents(final BwCalendar cal,
                           final BwCalendar newCal)
          throws CalFacadeException {
    // TODO - get a set of keys then move each - or bulk mod?

    RecurringRetrievalMode rrm = new RecurringRetrievalMode(
            RecurringRetrievalMode.Rmode.overrides);

    Collection<EventInfo> eis = svci.getEventsHandler().getEvents(cal,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null, // retrieveList
                                                                  rrm);

    for (EventInfo ei: eis) {
      BwEvent ev = ei.getEvent();

      ev.setColPath(newCal.getPath());

      svci.getEventsHandler().update(ei, false, null);
    }

    Collection<BwCalendar> cals = svci.getCalendarsHandler().getChildren(cal);

    for (BwCalendar c: cals) {
      svci.getCalendarsHandler().move(c, newCal);
    }
  }

  @Override
  public void changeAccess(final BwShareableDbentity ent,
                           final Collection<Ace> aces,
                           final boolean replaceAll)
          throws CalFacadeException {
    svci.changeAccess(ent, aces, replaceAll);
  }
}
