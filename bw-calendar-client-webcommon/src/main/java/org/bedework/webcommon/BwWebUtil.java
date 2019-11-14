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
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.ical.IcalUtil;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.Util;

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
   * @param request
   * @param ei
   * @return boolean true for ok
   * @throws CalFacadeException
   */
  public static boolean validateEventDates(final BwRequest request,
                                           final EventInfo ei) throws CalFacadeException {
    BwEvent ev = ei.getEvent();
    boolean ok = true;

    /* ------------- Start, end and duration  ------------------ */

    BwDateTime evstart = ev.getDtstart();
    DtStart start = evstart.makeDtStart();
    DtEnd end = null;
    Duration dur = null;

    char endType = ev.getEndType();

    if (endType == StartEndComponent.endTypeNone) {
    } else if (endType == StartEndComponent.endTypeDate) {
      BwDateTime evend = ev.getDtend();

      if (evstart.after(evend)) {
        request.getErr().emit(ValidationError.startAfterEnd);
        ok = false;
      } else {
        end = evend.makeDtEnd();
      }
    } else if (endType == StartEndComponent.endTypeDuration) {
      dur = new Duration(new Dur(ev.getDuration()));
    } else {
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
   * @param publicAdmin
   * @param ev
   * @return  null for OK, validation errors otherwise.
   * @throws CalFacadeException
   */
  public static List<ValidationError> validateEvent(final Client cl,
                                                    final boolean prePublish,
                                                    final boolean publicAdmin,
                                                    final BwEvent ev)
                                  throws CalFacadeException {
    List<ValidationError> ves = null;

    /* ------------- Set zero length fields to null ------------------ */

    ev.setLink(checkNull(ev.getLink()));

    /* ------------- Check summary and description ------------------ */
    AuthProperties apars = cl.getAuthProperties();
    int maxDescLen;
    if (publicAdmin || prePublish) {
      maxDescLen = apars.getMaxPublicDescriptionLength();
    } else {
      maxDescLen = apars.getMaxUserDescriptionLength();
    }

    /* ------------------------- summary ------------------------------- */

    boolean nullOk = !publicAdmin && !prePublish;
    Collection<BwString> sums = ev.getSummaries();
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

    Collection<BwLongString> descs = ev.getDescriptions();
    if ((descs == null) || (descs.size() == 0)) {
      if (!nullOk) {
        ves = addError(ves, ValidationError.missingDescription);
      }
    } else {
      for (BwLongString s: descs) {
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
      for (String recip: ev.getRecipients()) {
        if (!validateUserHref(cl, recip)) {
          ves = addError(ves, ValidationError.invalidRecipient, recip);
        }
      }
    }

    if (ev.getNumAttendees() > 0) {
      for (BwAttendee att: ev.getAttendees()) {
        if (!validateUserHref(cl, att.getAttendeeUri())) {
          ves = addError(ves, ValidationError.invalidAttendee,
                         att.getAttendeeUri());
        }
      }
    }

    BwOrganizer org = ev.getOrganizer();
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
      ves = new ArrayList<ValidationError>();
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
   * @throws CalFacadeException
   */
  public static boolean validateUserHref(final Client cl,
                                         final String href) throws CalFacadeException {
    AccessPrincipal p = cl.calAddrToPrincipal(href);

    if (p == null) {
      return true; // External user.
    }

    return cl.validPrincipal(p.getPrincipalRef());
  }

  /** */
  public static class ValidateResult {
    /** */
    public boolean ok = true;
    /** */
    public boolean changed;
  }

  /**
   * @param form struts form
   * @return ValidateResult
   */
  public static ValidateResult validateLocation(final BwActionFormBase form) {
    final ValidateResult vr = new ValidateResult();

    final BwLocation loc = form.getLocation();

    /*
    BwString str = loc.getAddress();
    BwString frmstr = form.getLocationAddress();
    if (frmstr != null) {
      if (frmstr.checkNulls() && (frmstr.getValue() == null)) {
        frmstr = null;
      }
    }

    if (str == null) {
      if (frmstr != null) {
        vr.changed = true;
        loc.setAddress(frmstr);
      }
    } else if (frmstr == null) {
      vr.changed = true;
      loc.deleteAddress();
    } else if (str.update(frmstr)) {
      vr.changed = true;
    }

    final BwString str = loc.getSubaddress();
    BwString frmstr = form.getLocationSubaddress();
    if (frmstr != null) {
      if (frmstr.checkNulls() && (frmstr.getValue() == null)) {
        frmstr = null;
      }
    }

    if (str == null) {
      if (frmstr != null) {
        vr.changed = true;
        loc.setSubaddress(frmstr);
      }
    } else if (frmstr == null) {
      vr.changed = true;
      loc.deleteSubaddress();
    } else if (str.update(frmstr)) {
      vr.changed = true;
    }
    */

    final BwString addr = loc.getAddress();
    if (addr == null) {
      form.getErr().emit(ValidationError.missingAddress);
      vr.ok = false;
    } else {
      /* Put the status in the address lang */

      final String formSt = Util.checkNull(form.getCategoryStatus());

      if (formSt == null) {
        vr.changed = addr.getLang() != null;
        addr.setLang(null);
      } else if (!formSt.equals(addr.getLang())) {
        addr.setLang(formSt);
        vr.changed = true;
      }
    }
    
    // XXX - always true for the moment
    vr.changed = true;

    return vr;
  }

  /**
   *
   * @param form for data
   * @return ValidateResult
   */
  public static ValidateResult validateContact(final BwActionFormBase form) {
    final ValidateResult vr = new ValidateResult();

    final BwContact contact = form.getContact();

    final BwString str = contact.getCn();
    BwString frmstr = form.getContactName();
    if (frmstr != null) {
      if (frmstr.checkNulls() && (frmstr.getValue() == null)) {
        frmstr = null;
      }
    }

    if (str == null) {
      if (frmstr != null) {
        vr.changed = true;
        contact.setCn(frmstr);
      } else {
        form.getErr().emit(ValidationError.missingContactName);
        vr.ok = false;
      }
    } else if (frmstr == null) {
      vr.changed = true;
      contact.deleteName();
      form.getErr().emit(ValidationError.missingContactName);
      vr.ok = false;
    } else if (str.update(frmstr)) {
      vr.changed = true;
    }
    
    if (str != null) {
      /* Put the status in the cn lang */

      final String formSt = Util.checkNull(form.getCategoryStatus());

      if (formSt == null) {
        vr.changed = str.getLang() != null;
        str.setLang(null);
      } else if (!formSt.equals(str.getLang())) {
        str.setLang(formSt);
        vr.changed = true;
      }
    }

    contact.setPhone(Util.checkNull(contact.getPhone()));
    contact.setEmail(Util.checkNull(contact.getEmail()));
    contact.setLink(Util.checkNull(contact.getLink()));

    contact.setLink(fixLink(contact.getLink()));

    return vr;
  }

  /** Return either null (for null or all whitespace) or a url
   * prefixed with http://
   *
   * @param val  String urlk to fix up
   * @return String  fixed up
   */
  public static String fixLink(String val) {
    val = checkNull(val);

    if (val == null) {
      return val;
    }

    if (val.indexOf("://") > 0) {
      return val;
    }

    return "http://" + val;
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

