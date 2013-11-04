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
package org.bedework.webcommon.misc;

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.base.BwTimeRange;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.util.BwDateTimeUtil;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.Icalendar;
import org.bedework.icalendar.VFreeUtil;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwModuleState;
import org.bedework.webcommon.BwRequest;

import net.fortuna.ical4j.model.component.VFreeBusy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

/**
 * Action to publish free busy information as rfc formatted information.
 *
 * <p>The incoming request is formatted as in the current CalConnect freebusy URL
 * draft.
 *
 * <p>Request parameters - all optional:<ul>
 *      <li>  user:     whose free busy we want - default to current user</li>
 *      <li>  cua:      alternate to user - a calendar user address</li>
 *      <li>  start:    start of period - default to beginning of this week</li>
 *      <li>  end:      end of period - default to end of this week</li>
 *      <li>  calPath:  optional path to calendar</li>.
 * </ul>
 *
 * <p>This action forwards to null. HTTP response codes are set.
 *
 * <p>If no period is given return this week.
 *
 * @author Mike Douglass douglm - rpi.edu
 */
public class FreeBusyPublishAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    BwPrincipal principal = null;
    Client cl = request.getClient();
    BwModuleState mstate = request.getModule().getState();

    gotoDateView(request, mstate.getDate(), mstate.getViewTypeI());

    String userId = request.getReqPar("user");
    String cua = null;

    if (userId == null) {
      cua = request.getReqPar("cua");
    }

    if (cua != null) {
      principal = cl.calAddrToPrincipal(cua);
    } else if (userId != null) {
      principal = cl.getUser(userId);
    } else if (!form.getGuest()) {
      principal = cl.getCurrentPrincipal();
    }

    if (principal == null) {
      request.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
      return forwardNull;
    }

    BwCalendar cal = null;

    String calPath = request.getReqPar("calPath");
    if (calPath != null) {
      cal = cl.getCollection(calPath);
      if (cal == null) {
        request.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND,
                                        calPath);
        return forwardNull;
      }
    }

    AuthProperties authp = request.getSess().getAuthpars();

    int max = 0;

    if (!cl.isSuperUser()) {
      max = authp.getMaxFBPeriod();
    }

    BwTimeRange tr = BwDateTimeUtil.getPeriod(request.getReqPar("start"),
                                            request.getReqPar("end"),
                                            Calendar.DATE,
                                            authp.getDefaultFBPeriod(),
                                            java.util.Calendar.DATE,
                                            max);

    if (tr == null) {
      request.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST,
                                      "dates");
      return forwardNull;
    }

    try {
      if (debug) {
        debugMsg("getFreeBusy for start =  " + tr.getStart()+
                 " end = " + tr.getEnd());
      }

      Collection<BwCalendar> cals = null;
      if (cal != null) {
        cals = new ArrayList<BwCalendar>();
        cals.add(cal);
      }

      String orgUri = cl.getCurrentCalendarAddress();
      BwOrganizer org = new BwOrganizer();
      org.setOrganizerUri(orgUri);

      BwEvent fb = cl.getFreeBusy(cals,
                                  principal,
                                  tr.getStart(),
                                  tr.getEnd(),
                                  org,
                                  null, // uid
                                  null);

      VFreeBusy vfreeBusy = VFreeUtil.toVFreeBusy(fb);
      net.fortuna.ical4j.model.Calendar ical = null;
      if (vfreeBusy != null) {
        ical = IcalTranslator.newIcal(Icalendar.methodTypePublish);
        ical.getComponents().add(vfreeBusy);
      }

      form.setContentName("freebusy.ics");
      request.getResponse().setHeader("Content-Disposition",
                                      "Attachment; Filename=\"freebusy.ics\"");
      request.getResponse().setContentType("text/calendar; charset=UTF-8");

      IcalTranslator.writeCalendar(ical, request.getResponse().getWriter());
    } catch (CalFacadeAccessException cfae) {
      request.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    return forwardNull;
  }
}
