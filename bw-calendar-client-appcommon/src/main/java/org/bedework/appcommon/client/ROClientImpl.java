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

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.prefs.BwPreferences;
import org.bedework.calsvc.indexing.BwIndexer;
import org.bedework.calsvci.CalSvcI;
import org.bedework.calsvci.Categories;
import org.bedework.calsvci.EventProperties;
import org.bedework.calsvci.SchedulingI;

import edu.rpi.cmt.access.Ace;
import edu.rpi.sss.util.Util;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

import javax.xml.ws.Holder;

/**
 * User: douglm Date: 6/27/13 Time: 2:03
 */
public class ROClientImpl implements Client {
  protected boolean debug;

  protected CalSvcI svci;
  protected boolean publicView;

  private BwPrincipal currentPrincipal;
  private String currentCalendarAddress;

  private ClientState cstate;

  public ROClientImpl(final CalSvcI svci,
                      final boolean publicView) {
    this.svci = svci;
    this.publicView = publicView;
    cstate = new ClientState(this);
  }

  @Override
  public SystemProperties getSystemProperties()
          throws CalFacadeException {
    return svci.getSystemProperties();
  }

  @Override
  public void rollback() {
    try {
      svci.rollbackTransaction();
    } catch (Throwable t) {}

    try {
      svci.endTransaction();
    } catch (Throwable t) {}
  }

  @Override
  public long getUserMaxEntitySize() throws CalFacadeException {
    return svci.getUserMaxEntitySize();
  }

  @Override
  public BwPrincipal getCurrentPrincipal() throws CalFacadeException {
    if (currentPrincipal == null) {
      currentPrincipal = (BwPrincipal)svci.getPrincipal().clone();
    }

    return currentPrincipal;
  }

  @Override
  public BwPrincipal getOwner() throws CalFacadeException {
    return getCurrentPrincipal();
  }

  @Override
  public String getCurrentPrincipalHref() throws CalFacadeException {
    return currentPrincipal.getPrincipalRef();
  }

  @Override
  public String getCurrentAccount() throws CalFacadeException {
    return currentPrincipal.getAccount();
  }

  public String getCurrentCalendarAddress() throws CalFacadeException {
    if (currentCalendarAddress == null) {
      currentCalendarAddress = svci.getDirectories().principalToCaladdr(getCurrentPrincipal());
    }

    return currentCalendarAddress;
  }

  @Override
  public BwPrincipal getUser(final String val)
          throws CalFacadeException {
    return svci.getUsersHandler().getUser(val);
  }

  @Override
  public String getCalendarAddress(final String user)
          throws CalFacadeException {
    BwPrincipal u = svci.getUsersHandler().getUser(user);
    if (u == null) {
      return null;
    }

    return svci.getDirectories().principalToCaladdr(u);
  }

  @Override
  public String uriToCaladdr(final String val)
          throws CalFacadeException {
    return svci.getDirectories().uriToCaladdr(val);
  }

  @Override
  public BwPrincipal calAddrToPrincipal(final String cua)
          throws CalFacadeException {
    return svci.getDirectories().caladdrToPrincipal(cua);
  }

  @Override
  public String makePrincipalUri(final String id,
                                 final int whoType)
          throws CalFacadeException {
    return svci.getDirectories().makePrincipalUri(id, whoType);
  }

  @Override
  public BwPrincipal getPrincipal(final String href)
          throws CalFacadeException {
    return svci.getDirectories().getPrincipal(href);
  }

  @Override
  public boolean validPrincipal(final String href)
          throws CalFacadeException {
    return svci.getDirectories().validPrincipal(href);
  }

  @Override
  public BwPreferences getPreferences() throws CalFacadeException {
    return svci.getPrefsHandler().get();
  }

  @Override
  public void updateAdminPrefs(final boolean remove,
                               final BwCalendar col,
                               final BwCategory cat,
                               final BwLocation loc,
                               final BwContact contact)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public String getPreferredCollectionPath()
          throws CalFacadeException {
    BwCalendar col = svci.getCalendarsHandler().getPreferred();

    if (col == null) {
      return null;
    }

    return col.getPath();
  }

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  @Override
  public BwCalendar getHome() throws CalFacadeException {
    return svci.getCalendarsHandler().getHome();
  }

  @Override
  public BwCalendar getCollection(final String path)
          throws CalFacadeException {
    return svci.getCalendarsHandler().get(path);
  }

  @Override
  public BwCalendar getSpecial(final int calType,
                               final boolean create)
          throws CalFacadeException {
    return svci.getCalendarsHandler().getSpecial(calType, create);
  }

  @Override
  public BwCalendar addCollection(final BwCalendar val,
                                  final String parentPath)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateCollection(final BwCalendar col)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwCalendar resolveAlias(final BwCalendar val,
                                 final boolean resolveSubAlias,
                                 final boolean freeBusy)
          throws CalFacadeException {
    return svci.getCalendarsHandler().resolveAlias(val,
                                                   resolveSubAlias,
                                                   freeBusy);
  }

  @Override
  public Collection<BwCalendar> getChildren(final BwCalendar col)
          throws CalFacadeException {
    return svci.getCalendarsHandler().getChildren(col);
  }

  @Override
  public void moveCollection(final BwCalendar val,
                             final BwCalendar newParent)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<BwCalendar> decomposeVirtualPath(final String vpath)
          throws CalFacadeException {
    return svci.getCalendarsHandler().decomposeVirtualPath(vpath);
  }

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  @Override
  public BwCategory getCategoryByName(final String name)
          throws CalFacadeException {
    return getCategoryByName(new BwString(null, name));
  }

  @Override
  public BwCategory getCategoryByName(final BwString name)
          throws CalFacadeException {
    return svci.getCategoriesHandler().find(name);
  }

  @Override
  public BwCategory getCategory(final String uid)
          throws CalFacadeException {
    return svci.getCategoriesHandler().get(uid);
  }

  @Override
  public boolean categoryExists(final BwCategory cat)
          throws CalFacadeException {
    return svci.getCategoriesHandler().exists(cat);
  }

  @Override
  public void addCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void updateCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public DeleteCategoryResult deleteCategory(final BwCategory val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  @Override
  public BwContact getContact(final String uid)
          throws CalFacadeException {
    return svci.getContactsHandler().get(uid);
  }

  @Override
  public BwContact findContact(final BwString val)
          throws CalFacadeException {
    return svci.getContactsHandler().find(val,
                                          getOwner().getPrincipalRef());
  }

  @Override
  public void addContact(final BwContact val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public BwContact ensureContactExists(final BwContact val,
                                       final String ownerHref)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  @Override
  public BwLocation getLocation(final String uid)
          throws CalFacadeException {
    return svci.getLocationsHandler().get(uid);
  }

  @Override
  public BwLocation findLocation(final BwString address)
          throws CalFacadeException {
    return svci.getLocationsHandler().find(address,
                                           getOwner().getPrincipalRef());
  }

  @Override
  public void addLocation(final BwLocation val)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  @Override
  public void claimEvent(final BwEvent ev) throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public Collection<EventInfo> getEvent(final String path,
                                        final String guid,
                                        final String rid,
                                        final RecurringRetrievalMode recurRetrieval,
                                        final boolean scheduling)
          throws CalFacadeException {
    return svci.getEventsHandler().get(path, guid, rid,
                                       recurRetrieval, scheduling);
  }

  @Override
  public EventInfo getEvent(final String colPath,
                            final String name,
                            final RecurringRetrievalMode recurRetrieval)
          throws CalFacadeException {
    return svci.getEventsHandler().get(colPath, name, recurRetrieval);
  }

  private static class ClGetEventsResult implements GetEventsResult {
    private boolean paged;
    private long count;
    private Collection<EventInfo> events;

    @Override
    public boolean getPaged() {
      return paged;
    }

    @Override
    public long getCount() {
      return count;
    }

    @Override
    public Collection<EventInfo> getEvents() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
  }

  @Override
  public GetEventsResult getEvents(final BwDateTime start,
                                   final BwDateTime end,
                                   final FilterBase filter,
                                   final boolean forExport,
                                   final boolean exact,
                                   final int pos,
                                   final int pageSize)
          throws CalFacadeException {
    long curTime = System.currentTimeMillis();
    ClGetEventsResult ger = new ClGetEventsResult();

    FilterBase f = FilterBase.addAndChild(filter,
                                          cstate.getViewFilter(null));

    /* See if we can use the indexed version */
    boolean tryIndexer = publicView;

    BwIndexer idx = null;
    if (publicView) {
      idx = svci.getIndexingHandler().getIndexer(publicView,
                                                 getCurrentPrincipalHref());
    }

    if (publicView && idx.isFetchEnabled()) {
      // Fetch from the index.

      String from = null;
      String to = null;

      if (start !=null) {
        from = start.getDate();
      }

      if (end !=null) {
        to = end.getDate();
      }

      Holder<Integer> resSz = new Holder<>();

      ger.events = idx.fetch(f,
                             from,
                             to,
                             resSz,
                             pos,
                             pageSize);

      ger.count = resSz.value;
      ger.paged = true;

      int count = 0;
      if (!Util.isEmpty(ger.events)) {
        count = ger.events.size();
      }

      if (debug) {
        debugMsg("Indexer retrieved " + count + " events after " +
                         (System.currentTimeMillis() - curTime));
      }

      /* We need to implant categories and locations */

      if (count > 0) {
        Categories cats = svci.getCategoriesHandler();
        EventProperties<BwLocation> locs = svci.getLocationsHandler();

        for (EventInfo ei: ger.events) {
          BwEvent ev = ei.getEvent();

          List<String> catUids = ev.getCategoryUids();

          if (catUids != null) {
            for (String uid: catUids) {
              BwCategory cat = cats.get(uid);

              if (cat != null) {
                ev.addCategory(cat);
              }
            }
          }

          if (ev.getLocationUid() != null) {
            ev.setLocation(locs.getCached(ev.getLocationUid()));
          }
        }
      }

      if (debug) {
        debugMsg("Retrieved events fully populated: took " +
                         (System.currentTimeMillis() - curTime));
      }
    } else {
      RecurringRetrievalMode rrm;
      if (forExport) {
        rrm = new RecurringRetrievalMode(RecurringRetrievalMode.Rmode.overrides);
      } else {
        rrm = new RecurringRetrievalMode(RecurringRetrievalMode.Rmode.expanded);
      }

      ger.events = svci.getEventsHandler().getEvents((BwCalendar)null, f,
                                                 start,
                                                 end,
                                                 null, // retrieveList
                                                 rrm);

      if (!Util.isEmpty(ger.events)) {
        ger.count = ger.events.size();
      }
    }

    cstate.setColor(ger.events);

    return ger;
  }

  @Override
  public Collection<EventInfo> getEvents(final BwCalendar cal,
                                         final FilterBase filter,
                                         final BwDateTime startDate,
                                         final BwDateTime endDate,
                                         final List<String> retrieveList,
                                         final RecurringRetrievalMode recurRetrieval)
          throws CalFacadeException {
    return svci.getEventsHandler().getEvents(cal, filter,
                                             startDate,
                                             endDate,
                                             retrieveList,
                                             recurRetrieval);
  }

  @Override
  public EventInfo.UpdateResult addEvent(final EventInfo ei,
                                         final boolean noInvites,
                                         final boolean scheduling,
                                         final boolean rollbackOnError)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public EventInfo.UpdateResult updateEvent(final EventInfo ei,
                                            final boolean noInvites,
                                            final String fromAttUri)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public SchedulingI.FbResponses aggregateFreeBusy(final ScheduleResult sr,
                                                   final BwDateTime start,
                                                   final BwDateTime end,
                                                   final BwDuration granularity)
          throws CalFacadeException {
    return svci.getScheduler().aggregateFreeBusy(sr, start, end, granularity);
  }

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
  public Collection<BwView> getAllViews() throws CalFacadeException {
    return svci.getViewsHandler().getAll();
  }

  @Override
  public boolean removeViewCollection(final String name,
                                      final String path)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /* ------------------------------------------------------------
   *                     State of client
   * ------------------------------------------------------------ */

  @Override
  public void flushState() throws CalFacadeException {
    cstate.flush();
  }

  @Override
  public boolean setCurrentView(final String val)
          throws CalFacadeException {
    return cstate.setCurrentView(val);
  }

  @Override
  public void setCurrentView(final BwView val)
          throws CalFacadeException {
    cstate.setCurrentView(val);
  }

  @Override
  public BwView getCurrentView() throws CalFacadeException {
    return cstate.getCurrentView();
  }

  @Override
  public boolean setVirtualPath(final String vpath)
          throws CalFacadeException {
    return cstate.setVirtualPath(vpath);
  }

  @Override
  public String getVirtualPath() throws CalFacadeException {
    return cstate.getVirtualPath();
  }

  @Override
  public FilterBase getViewFilter(final BwCalendar col)
          throws CalFacadeException {
    return cstate.getViewFilter(col);
  }

  @Override
  public void moveContents(final BwCalendar cal,
                           final BwCalendar newCal)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  @Override
  public void changeAccess(final BwShareableDbentity ent,
                           final Collection<Ace> aces,
                           final boolean replaceAll)
          throws CalFacadeException {
    throw new CalFacadeException("org.bedework.read.only.client");
  }

  /**
   * @param msg
   */
  protected void debugMsg(final String msg) {
    Logger.getLogger(this.getClass()).debug(msg);
  }
}
