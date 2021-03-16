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

import org.bedework.access.AccessPrincipal;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.ical.IcalUtil;
import org.bedework.util.calendar.IcalDefs;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Useful shared web utility routines
 *
 * @author  Mike Douglass  douglm    rpi.edu
 */
public class BwWebUtil {
  /** Name of the session attribute holding our session state
   */
  public static final String sessStateAttr = "org.bedework.sessstate";

  /** Name of the session attribute holding our calendar interface
   */
  public static final String sessCalSvcIAttr = "org.bedework.calsvci";

  /** Try to get the session state object  embedded in
   *  the current session.
   *
   * @param request  Needed to locate session
   * @return BwSession null on failure
   */
  public static BwSession getState(final HttpServletRequest request) {
    HttpSession sess = request.getSession(false);

    if (sess != null) {
      Object o = sess.getAttribute(sessStateAttr);
      if ((o != null) && (o instanceof BwSession)) {
        return (BwSession)o;
      }
    } else {
      noSession();
    }

    return null;
  }

  /** Drop the session state object embedded in
   *  the current session.
   *
   * @param request  Needed to locate session
   */
  public static void dropState(final HttpServletRequest request) {
    HttpSession sess = request.getSession(false);

    if (sess == null) {
      return;
    }

    sess.removeAttribute(sessStateAttr);
  }

  /** Set the session state object into the current session.
   *
   * @param request  HttpServletRequest Needed to locate session
   * @param s        BwSession session object
   */
  public static void setState(final HttpServletRequest request,
                              final BwSession s) {
    HttpSession sess = request.getSession(false);

    if (sess != null) {
      sess.setAttribute(sessStateAttr, s);
    } else {
      noSession();
    }
  }

  /** Validate the date properties of the event.
   *
   * @param request bw request
   * @param ei event info
   * @return boolean true for ok
   */
  public static boolean validateEventDates(final BwRequest request,
                                           final EventInfo ei) {
    final BwEvent ev = ei.getEvent();
    boolean ok = true;

    /* ------------- Start, end and duration  ------------------ */

    final BwDateTime evstart = ev.getDtstart();
    final DtStart start = evstart.makeDtStart();
    DtEnd end = null;
    Duration dur = null;

    final char endType = ev.getEndType();

    if (endType == StartEndComponent.endTypeDate) {
      final BwDateTime evend = ev.getDtend();

      if (evstart.after(evend)) {
        request.getErr().emit(ValidationError.startAfterEnd);
        ok = false;
      } else {
        end = IcalUtil.makeDtEnd(evend);
      }
    } else if (endType == StartEndComponent.endTypeDuration) {
      dur = new Duration(new Dur(ev.getDuration()));
    } else if (endType != StartEndComponent.endTypeNone) {
      request.getErr().emit(ValidationError.invalidEndtype);
      ok = false;
    }

    if (ok) {
      /* This calculates the duration etc. We need to merge this in with the
       * EventDates stuff as we are setting things twice
       */
      IcalUtil.setDates(request.getClient().getCurrentPrincipalHref(),
                        ei, start, end, dur);
    }

    return ok;
  }

  /** Validate the properties of the event.
   *
   * @param cl - for system properties
   * @param prePublish - a public event being submitted or updated before publish
   * @param publicAdmin true fior public admin
   * @param ev event
   * @return  null for OK, validation errors otherwise.
   */
  public static List<ValidationError> validateEvent(final Client cl,
                                                    final boolean prePublish,
                                                    final boolean publicAdmin,
                                                    final BwEvent ev) {
    List<ValidationError> ves = null;

    /* ------------- Set zero length fields to null ------------------ */

    ev.setLink(checkNull(ev.getLink()));

    /* ------------- Check summary and description ------------------ */
    final AuthProperties apars = cl.getAuthProperties();
    final int maxDescLen;
    if (publicAdmin || prePublish) {
      maxDescLen = apars.getMaxPublicDescriptionLength();
    } else {
      maxDescLen = apars.getMaxUserDescriptionLength();
    }

    /* ------------------------- summary ------------------------------- */

    final boolean nullOk = !publicAdmin && !prePublish;
    final Collection<BwString> sums = ev.getSummaries();
    if ((sums == null) || (sums.size() == 0)) {
      if (!nullOk) {
        ves = addError(ves, ValidationError.missingTitle);
      }
    } else {
      for (BwString s: sums) {
        if (s.getValue().length() > maxDescLen) {
          ves = addError(ves, ValidationError.tooLongSummary,
                         String.valueOf(maxDescLen));
          break;
        }
      }
    }

    /* ------------------------- description ------------------------------- */

    final Collection<BwLongString> descs = ev.getDescriptions();
    if ((descs == null) || (descs.size() == 0)) {
      if (!nullOk) {
        ves = addError(ves, ValidationError.missingDescription);
      }
    } else {
      for (final BwLongString s: descs) {
        if (s.getValue().length() > maxDescLen) {
          ves = addError(ves, ValidationError.tooLongDescription,
                   String.valueOf(maxDescLen));
          break;
        }
      }
    }

    if (publicAdmin && !prePublish) {
      /* -------------------------- Location ------------------------------ */

      if (ev.getLocation() == null) {
        ves = addError(ves, ValidationError.missingLocation);
      }

      /* -------------------------- Contact ------------------------------ */

      if (ev.getContact() == null) {
        ves = addError(ves, ValidationError.missingContact);
      }
    }

    /* ------------- Transparency and status ------------------ */

    if (!checkTransparency(ev.getTransparency())) {
      ves = addError(ves, ValidationError.invalidTransparency,
                     ev.getTransparency());
    }

    if (!checkStatus(ev.getStatus())) {
      ves = addError(ves, ValidationError.invalidStatus,
                     ev.getStatus());
    }

    /* ------------- All referenced users valid? ------------------ */

    if (ev.getNumRecipients() > 0) {
      for (final String recip: ev.getRecipients()) {
        if (!validateUserHref(cl, recip)) {
          ves = addError(ves, ValidationError.invalidRecipient, recip);
        }
      }
    }

    if (ev.getNumAttendees() > 0) {
      for (final BwAttendee att: ev.getAttendees()) {
        if (!validateUserHref(cl, att.getAttendeeUri())) {
          ves = addError(ves, ValidationError.invalidAttendee,
                         att.getAttendeeUri());
        }
      }
    }

    final BwOrganizer org = ev.getOrganizer();
    if (org != null) {
      if (!validateUserHref(cl, org.getOrganizerUri())) {
        ves = addError(ves, ValidationError.invalidOrganizer,
                 org.getOrganizerUri());
      }
    }

    return ves;
  }

  private static List<ValidationError> addError(final List<ValidationError> ves,
                                                      final String errorCode) {
    return addError(ves, errorCode, null);
  }

  private static List<ValidationError> addError(List<ValidationError> ves,
                                                      final String errorCode,
                                                      final String extra) {
    if (ves == null) {
      ves = new ArrayList<>();
    }

    ves.add(new ValidationError(errorCode, extra));

    return ves;
  }

  /** Given a calendar user href check for validity.
   *
   * <p>If it's external to the system we just accept it. If it's internal we
   * require it be a valid user account.
   *
   * @param cl - client
   * @param href of possible principal
   * @return boolean true for ok
   */
  public static boolean validateUserHref(final Client cl,
                                         final String href) {
    final AccessPrincipal p = cl.calAddrToPrincipal(href);

    if (p == null) {
      return true; // External user.
    }

    return cl.validPrincipal(p.getPrincipalRef());
  }

  /** Check for valid transparency setting
   *
   * @param val possible transparency value
   * @return boolean true for ok
   */
  public static boolean checkTransparency(final String val) {
    return (val == null) ||  // Defaulted
            IcalDefs.transparencyOpaque.equals(val) ||
            IcalDefs.transparencyTransparent.equals(val);
  }

  /** Check for valid status setting
   *
   * @param val possible status value
   * @return boolean true for ok
   */
  public static boolean checkStatus(final String val) {
    return (val == null) ||  // Defaulted
            BwEvent.statusConfirmed.equals(val) ||
            BwEvent.statusTentative.equals(val) ||
            BwEvent.statusCancelled.equals(val);
  }

  /** We get a lot of zero length strings in the web world. This will return
   * null for a zero length.
   *
   * @param  val    String request parameter value
   * @return String null for null or zero lengt val, val otherwise.
   */
  public static String checkNull(final String val) {
    if (val == null) {
      return null;
    }

    if (val.length() == 0) {
      return null;
    }

    return val;
  }

  private static void noSession() {
    Logger.getLogger("org.bedework.webcommon.BwWebUtil").warn(
            "No session!!!!!!!");
  }
}

