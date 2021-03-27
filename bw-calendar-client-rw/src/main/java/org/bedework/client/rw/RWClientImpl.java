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
package org.bedework.client.rw;

import org.bedework.access.Ace;
import org.bedework.appcommon.BedeworkDefs;
import org.bedework.appcommon.ConfigCommon;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.ROClientImpl;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwResourceContent;
import org.bedework.calfacade.EventPropertiesReference;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.base.BwShareableDbentity;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.BwView;
import org.bedework.calfacade.svc.CalSvcIPars;
import org.bedework.calfacade.svc.EnsureEntityExistsResult;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calfacade.svc.RealiasResult;
import org.bedework.calfacade.svc.SharingReplyResult;
import org.bedework.calfacade.svc.SubscribeResult;
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.CalSvcFactoryDefault;
import org.bedework.calsvci.Categories;
import org.bedework.calsvci.Contacts;
import org.bedework.calsvci.Locations;
import org.bedework.calsvci.SchedulingI;
import org.bedework.util.misc.response.Response;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.Collection;
import java.util.List;

import static org.bedework.calfacade.indexing.BwIndexer.DeletedState.includeDeleted;

/**
 * User: douglm Date: 6/27/13 Time: 2:05 PM
 */
public class RWClientImpl extends ROClientImpl
        implements RWClient {
  protected boolean defaultFilterContextSet;
  protected FilterBase defaultFilterContext;

  public RWClientImpl(final ConfigCommon conf,
                      final String id,
                      final String authUser,
                      final String runAsUser,
                      final String appType) {
    this(conf, id);

    reinit(authUser, runAsUser, appType);
  }

  protected RWClientImpl(final ConfigCommon conf,
                         final String id) {
    super(conf, id);
  }

  public void reinit(final String authUser,
                     final String runAsUser,
                     final String appType) {
    currentPrincipal = null;
    this.appType = appType;

    pars = CalSvcIPars.getRwClientPars(id,
                                       authUser,
                                       runAsUser);

    svci = new CalSvcFactoryDefault().getSvc(pars);
    resetIndexers();
  }

  @Override
  public Client copy(final String id) {
    final RWClientImpl cl = new RWClientImpl(conf, id);

    copyCommon(id, cl);

    return cl;
  }

  @Override
  public boolean getWebUser() {
    return BedeworkDefs.appTypeWebuser.equals(appType);
  }

  @Override
  public void unindex(final String href) throws CalFacadeException {
    getIndexer(isDefaultIndexPublic(),
               BwIndexer.docTypeEvent).unindexEntity(href);
  }

  /* ------------------------------------------------------------
   *                     Principals
   * ------------------------------------------------------------ */

  @Override
  public boolean isGuest() {
    return false;
  }

  @Override
  public BwPrincipal getOwner() {
    if (publicAdmin) {
      return svci.getUsersHandler().getPublicUser();
    }

    return getCurrentPrincipal();
  }

  /* ------------------------------------------------------------
   *                     Preferences
   * ------------------------------------------------------------ */

  @Override
  public void updatePreferences(final BwPreferences val) {
    svci.getPrefsHandler().update(val);
  }

  /* ------------------------------------------------------------
   *                     Collections
   * ------------------------------------------------------------ */

  @Override
  public BwCalendar getCollection(final String path)
          throws CalFacadeException {
    checkUpdate();
    BwCalendar col = svci.getCalendarsHandler().get(path);
    if (col != null) {
      col = (BwCalendar)svci.merge(col);
    }

    return col;
  }

  @Override
  public BwCalendar addCollection(final BwCalendar val,
                                  final String parentPath)
          throws CalFacadeException {
    try {
      return update(svci.getCalendarsHandler().add(val, parentPath));
    } finally {
      if (val.getPublick()) {
        flushCached();
      }
    }
  }

  @Override
  public void updateCollection(final BwCalendar col)
          throws CalFacadeException {
    svci.getCalendarsHandler().update(col);
    updated();

    if (col.getPublick()) {
      flushCached();
    }
  }

  @Override
  public boolean deleteCollection(final BwCalendar val,
                                  final boolean emptyIt)
          throws CalFacadeException {
    try {
      return update(svci.getCalendarsHandler().delete(val, emptyIt,
                                                      true));
    } finally {
      if (val.getPublick()) {
        flushCached();
      }
    }
  }

  @Override
  public void moveCollection(final BwCalendar val,
                             final BwCalendar newParent)
          throws CalFacadeException {
    svci.getCalendarsHandler().move(val, newParent);
    updated();
    if (val.getPublick()) {
      flushCached();
    }
  }

  @Override
  public Collection<BwCalendar> getAddContentCollections(final boolean includeAliases)
          throws CalFacadeException {
    checkUpdate();
    return getCalendarCollator().getCollatedCollection(
            svci.getCalendarsHandler().getAddContentCollections(includeAliases));
  }

  /* ------------------------------------------------------------
   *                     Categories
   * ------------------------------------------------------------ */

  @Override
  public BwCategory getPersistentCategory(final String uid)
          throws CalFacadeException {
    return svci.getCategoriesHandler().getPersistent(uid);
  }

  @Override
  public boolean addCategory(final BwCategory val) {
    final Response resp = svci.getCategoriesHandler().add(val);
    checkResponse(resp);
    return update(resp.isOk());
  }

  @Override
  public void updateCategory(final BwCategory val)
          throws CalFacadeException {
    final BwCategory pval =
            svci.getCategoriesHandler().getPersistent(val.getUid());

    if (pval == null) {
      throw new CalFacadeException("No such category");
    }

    if (pval.updateFrom(val)) {
      svci.getCategoriesHandler().update(pval);
    }

    updated();
  }

  private static class ClDeleteReffedEntityResult implements
          DeleteReffedEntityResult {
    private boolean deleted;

    private Collection<EventPropertiesReference> references;

    private ClDeleteReffedEntityResult(final boolean deleted) {
      this.deleted = deleted;
    }

    private ClDeleteReffedEntityResult(final Collection<EventPropertiesReference> references) {
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
  public DeleteReffedEntityResult deleteCategory(final BwCategory val)
          throws CalFacadeException {
    if (val == null) {
      return null;
    }

    final Categories cats = svci.getCategoriesHandler();
    final int delResult = cats.delete(val);

    updated();

    if (delResult == 2) {
      return new ClDeleteReffedEntityResult(cats.getRefs(val));
    }

    if (delResult == 1) {
      return null;
    }

    return new ClDeleteReffedEntityResult(true);
  }

  /* ------------------------------------------------------------
   *                     Contacts
   * ------------------------------------------------------------ */

  @Override
  public BwContact getPersistentContact(final String uid)
          throws CalFacadeException {
    return svci.getContactsHandler().getPersistent(uid);
  }

  @Override
  public void addContact(final BwContact val) {
    svci.getContactsHandler().add(val);
    updated();
  }

  @Override
  public void updateContact(final BwContact val)
          throws CalFacadeException {
    final BwContact pval =
            svci.getContactsHandler().getPersistent(val.getUid());

    if (pval == null) {
      throw new CalFacadeException("No such contact");
    }

    if (pval.updateFrom(val)) {
      svci.getContactsHandler().update(pval);
    }

    updated();
  }

  @Override
  public DeleteReffedEntityResult deleteContact(final BwContact val)
          throws CalFacadeException {
    if (val == null) {
      return null;
    }

    final Contacts cs = svci.getContactsHandler();
    final int delResult = cs.delete(val);

    updated();

    if (delResult == 2) {
      return new ClDeleteReffedEntityResult(cs.getRefs(val));
    }

    if (delResult == 1) {
      return null;
    }

    return new ClDeleteReffedEntityResult(true);
  }

  @Override
  public EnsureEntityExistsResult<BwContact> ensureContactExists(final BwContact val,
                                                                 final String ownerHref) {
    final EnsureEntityExistsResult<BwContact> eer =
            svci.getContactsHandler().ensureExists(val, ownerHref);
    updated();

    return eer;
  }

  /* ------------------------------------------------------------
   *                     Locations
   * ------------------------------------------------------------ */

  @Override
  public BwLocation getPersistentLocation(final String uid)
          throws CalFacadeException {
    return svci.getLocationsHandler().getPersistent(uid);
  }

  @Override
  public boolean addLocation(final BwLocation val) {
    final var resp = svci.getLocationsHandler().add(val);
    checkResponse(resp);
    return update(resp.isOk());
  }

  @Override
  public void updateLocation(final BwLocation val)
          throws CalFacadeException {
    final BwLocation pval =
            svci.getLocationsHandler().getPersistent(val.getUid());

    if (pval == null) {
      throw new CalFacadeException("No such location");
    }

    if (pval.updateFrom(val)) {
      svci.getLocationsHandler().update(pval);
    }

    updated();
  }

  @Override
  public DeleteReffedEntityResult deleteLocation(final BwLocation val)
          throws CalFacadeException {
    if (val == null) {
      return null;
    }

    final Locations locs = svci.getLocationsHandler();
    final int delResult = locs.delete(val);

    updated();

    if (delResult == 2) {
      return new ClDeleteReffedEntityResult(locs.getRefs(val));
    }

    if (delResult == 1) {
      return null;
    }

    return new ClDeleteReffedEntityResult(true);
  }

  @Override
  public EnsureEntityExistsResult<BwLocation> ensureLocationExists(final BwLocation val,
                                                            final String ownerHref) {
    final EnsureEntityExistsResult<BwLocation> eer =
            svci.getLocationsHandler().ensureExists(val, ownerHref);

    updated();

    return eer;
  }

  /* ------------------------------------------------------------
   *                     Events
   * ------------------------------------------------------------ */

  @Override
  public void claimEvent(final BwEvent ev) {
    svci.getEventsHandler().claim(ev);
  }

  @Override
  public RealiasResult reAlias(final BwEvent ev) {
    return svci.getEventsHandler().reAlias(ev);
  }

  @Override
  public UpdateResult addEvent(final EventInfo ei,
                               final boolean noInvites,
                               final boolean rollbackOnError) {
    return update(svci.getEventsHandler().add(ei, noInvites,
                                              false,
                                              false, rollbackOnError));
  }

  @Override
  public UpdateResult updateEvent(final EventInfo ei,
                                  final boolean noInvites,
                                  final String fromAttUri,
                                  final boolean alwaysWrite) {
    return update(svci.getEventsHandler().update(ei,
                                                 noInvites,
                                                 fromAttUri,
                                                 alwaysWrite,
                                                 true, // it's a client
                                                 false)); // autocreate
  }

  @Override
  public boolean deleteEvent(final EventInfo ei,
                             final boolean sendSchedulingMessage)
          throws CalFacadeException {
    return update(svci.getEventsHandler().delete(ei,
                                                 sendSchedulingMessage));
  }

  @Override
  public void markDeleted(final BwEvent event)
          throws CalFacadeException {
    svci.getEventsHandler().markDeleted(event);
    updated();
  }

  /* ------------------------------------------------------------
   *                     Notifications
   * ------------------------------------------------------------ */

  @Override
  public NotificationType findNotification(final String name)
          throws CalFacadeException {
    return svci.getNotificationsHandler().
            find(getCurrentPrincipal().getPrincipalRef(),
                 name);
  }

  @Override
  public void removeNotification(final String name) {
    try {
      svci.getNotificationsHandler().
              remove(getCurrentPrincipal().getPrincipalRef(),
                     name);
    } catch (final CalFacadeException e) {
      throw new RuntimeException(e);
    }
    updated();
  }

  @Override
  public void removeNotification(final NotificationType val)
          throws CalFacadeException {
    svci.getNotificationsHandler().remove(val);
    updated();
  }

  @Override
  public void removeAllNotifications(final String principalHref) throws CalFacadeException {
    svci.getNotificationsHandler().removeAll(principalHref);
    updated();
  }

  @Override
  public void subscribe(final String principalHref,
                        final List<String> emails)
          throws CalFacadeException {
    svci.getNotificationsHandler().subscribe(principalHref, emails);
  }

  @Override
  public void unsubscribe(final String principalHref,
                          final List<String> emails)
          throws CalFacadeException {
    svci.getNotificationsHandler().unsubscribe(principalHref, emails);
  }

  /* ------------------------------------------------------------
   *                     Resources
   * ------------------------------------------------------------ */

  @Override
  public BwResource getResource(final String path) throws CalFacadeException {
    checkUpdate();
    return svci.getResourcesHandler().get(path);
  }

  @Override
  public boolean getResourceContent(final BwResource val)
          throws CalFacadeException {
    checkUpdate();
    try {
      svci.getResourcesHandler().getContent(val);
    } catch (final CalFacadeException cfe) {
      if (CalFacadeException.missingResourceContent.equals(cfe.getMessage())) {
        return false;
      }

      throw cfe;
    }

    return true;
  }

  @Override
  public List<BwResource> getAllResources(final String path)
          throws CalFacadeException {
    checkUpdate();
    return svci.getResourcesHandler().getAll(path);
  }

  @Override
  public List<BwResource> getResources(final String path,
                                       final int count)
          throws CalFacadeException {
    checkUpdate();
    return svci.getResourcesHandler().get(path, count);
  }

  @Override
  public void saveResource(final BwResource val) throws CalFacadeException {
    svci.getResourcesHandler().save(val, false);
    updated();
  }

  @Override
  public void updateResource(final BwResource val,
                             final boolean updateContent)
          throws CalFacadeException {
    svci.getResourcesHandler().update(val, updateContent);
    updated();
  }

  @Override
  public void setResourceValue(final BwResource val,
                               final String content) throws CalFacadeException {
    final byte[] bytes;
    bytes = content.getBytes(StandardCharsets.UTF_8);

    setResourceValue(val, bytes);
  }

  @Override
  public void setResourceValue(final BwResource val,
                               final byte[] content) throws CalFacadeException {
    BwResourceContent resContent = val.getContent();

    if (resContent == null) {
      resContent = new BwResourceContent();
      val.setContent(resContent);
    }

    final Blob blob = svci.getBlob(content);
    resContent.setValue(blob);
    val.setContentLength(content.length);
  }

  /* ------------------------------------------------------------
   *                     Scheduling
   * ------------------------------------------------------------ */

  @Override
  public BwEvent getFreeBusy(final Collection<BwCalendar> fbset,
                             final BwPrincipal who,
                             final BwDateTime start,
                             final BwDateTime end,
                             final BwOrganizer org,
                             final String uid,
                             final String exceptUid)
          throws CalFacadeException {
    return svci.getScheduler().getFreeBusy(fbset, who, start, end,
                                           org, uid, exceptUid);
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
                                 final String recipient,
                                 final String fromAttUri,
                                 final boolean iSchedule)
          throws CalFacadeException {
    return update(svci.getScheduler().schedule(ei,
                                               recipient,
                                               fromAttUri,
                                               iSchedule));
  }

  @Override
  public EventInfo getStoredMeeting(final BwEvent ev)
          throws CalFacadeException {
    checkUpdate();
    return svci.getScheduler().getStoredMeeting(ev);
  }

  @Override
  public ScheduleResult requestRefresh(final EventInfo ei,
                                       final String comment)
          throws CalFacadeException {
    return svci.getScheduler().requestRefresh(ei, comment);
  }

  /* ------------------------------------------------------------
   *                     Sharing
   * ------------------------------------------------------------ */

  @Override
  public ShareResultType share(final String principalHref,
                               final BwCalendar col,
                               final ShareType share)
          throws CalFacadeException {
    return update(svci.getSharingHandler().share(principalHref, col,
                                                 share));
  }

  @Override
  public void publish(final BwCalendar col) throws CalFacadeException {
    svci.getSharingHandler().publish(col);
    updated();
  }

  @Override
  public void unpublish(final BwCalendar col)
          throws CalFacadeException {
    svci.getSharingHandler().unpublish(col);
    updated();
  }

  @Override
  public SharingReplyResult sharingReply(final InviteReplyType reply)
          throws CalFacadeException {
    return update(svci.getSharingHandler().reply(getHome(), reply));
  }

  @Override
  public SubscribeResult subscribe(final String colPath,
                                   final String subscribedName)
          throws CalFacadeException {
    return update(svci.getSharingHandler().subscribe(colPath,
                                                     subscribedName));
  }

  @Override
  public SubscribeResult subscribeExternal(final String extUrl,
                                           final String subscribedName,
                                           final int refresh,
                                           final String remoteId,
                                           final String remotePw)
          throws CalFacadeException {
    return update(svci.getSharingHandler().subscribeExternal(extUrl,
                                                      subscribedName,
                                                      refresh,
                                                      remoteId,
                                                      remotePw));
  }

  /* ------------------------------------------------------------
   *                     Synch
   * ------------------------------------------------------------ */

  @Override
  public BwSynchInfo getSynchInfo() throws CalFacadeException {
    return svci.getSynch().getSynchInfo();
  }

  /* ------------------------------------------------------------
   *                     Views
   * ------------------------------------------------------------ */

  @Override
  public boolean addView(final BwView val,
                         final boolean makeDefault)
          throws CalFacadeException {
    return update(svci.getViewsHandler().add(val, makeDefault));
  }

  @Override
  public boolean addViewCollection(final String name,
                                   final String path)
          throws CalFacadeException {
    return update(svci.getViewsHandler().addCollection(name, path));
  }

  @Override
  public boolean removeViewCollection(final String name,
                                      final String path)
          throws CalFacadeException {
    return update(svci.getViewsHandler().removeCollection(name,
                                                          path));
  }

  @Override
  public boolean removeView(final BwView val)
          throws CalFacadeException {
    return update(svci.getViewsHandler().remove(val));
  }

  /* ------------------------------------------------------------
   *                     Misc
   * ------------------------------------------------------------ */

  @Override
  public void moveContents(final BwCalendar cal,
                           final BwCalendar newCal)
          throws CalFacadeException {
    // TODO - getResource a set of keys then move each - or bulk mod?

    final Collection<EventInfo> eis = svci.getEventsHandler().
            getEvents(cal,
                      null,
                      null,
                      null,
                      null, // retrieveList
                      includeDeleted,
                      RecurringRetrievalMode.overrides);

    for (final EventInfo ei: eis) {
      final BwEvent ev = ei.getEvent();

      ev.setColPath(newCal.getPath());

      svci.getEventsHandler().update(ei, false, null,
                                     false); // autocreate
    }

    final Collection<BwCalendar> cals =
            svci.getCalendarsHandler().getChildren(cal);

    for (final BwCalendar c: cals) {
      svci.getCalendarsHandler().move(c, newCal);
    }

    updated();
  }

  @Override
  public void postMessage(final Message val)
          throws CalFacadeException {
    svci.getMailer().post(val);
  }

  /* ------------------------------------------------------------
   *                   Access
   * ------------------------------------------------------------ */

  @Override
  public void changeAccess(final BwShareableDbentity<?> ent,
                           final Collection<Ace> aces,
                           final boolean replaceAll)
          throws CalFacadeException {
    svci.changeAccess(ent, aces, replaceAll);
    updated();
  }

  /* ------------------------------------------------------------
   *                   Filters
   * ------------------------------------------------------------ */

  @Override
  public void validateFilter(final String val)
          throws CalFacadeException {
    svci.getFiltersHandler().validate(val);
  }

  @Override
  public void saveFilter(final BwFilterDef val)
          throws CalFacadeException {
    svci.getFiltersHandler().save(val);
    updated();
  }

  @Override
  public void deleteFilter(final String name)
          throws CalFacadeException {
    svci.getFiltersHandler().delete(name);
    updated();
  }

  protected FilterBase getDefaultFilterContext() throws CalFacadeException {
    if (defaultFilterContextSet) {
      return defaultFilterContext;
    }

    final BwView preferred = getView(null);

    if (preferred == null) {
      defaultFilterContextSet = true;
      return defaultFilterContext;
    }

    final String fexpr = "view=\"" + preferred.getName() +"\"";

    final BwFilterDef fd = new BwFilterDef();
    fd.setDefinition(fexpr);

    parseFilter(fd);

    defaultFilterContextSet = true;
    defaultFilterContext = fd.getFilters();

    return defaultFilterContext;
  }
}
