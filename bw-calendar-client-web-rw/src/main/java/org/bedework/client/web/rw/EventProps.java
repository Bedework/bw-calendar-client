/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.webcommon.BwRequest;

import static org.bedework.util.misc.Util.checkNull;

/**
 * User: mike Date: 3/13/21 Time: 23:58
 */
public class EventProps {
  /** */
  public static class ValidateResult {
    /** */
    public boolean ok = true;
    /** */
    public boolean changed;
  }

  /**
   * @param form action form
   * @return ValidateResult
   */
  public static ValidateResult validateLocation(final BwRequest request,
                                                final BwRWActionForm form) {
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
      request.error(ValidationError.missingAddress);
      vr.ok = false;
    } else {
      /* Put the status in the address lang */

      final String formSt = request.getReqPar("categoryStatus");

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
  public static ValidateResult validateContact(final BwRequest request,
                                               final BwRWActionForm form) {
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
        request.error(ValidationError.missingContactName);
        vr.ok = false;
      }
    } else if (frmstr == null) {
      vr.changed = true;
      contact.deleteName();
      request.error(ValidationError.missingContactName);
      vr.ok = false;
    } else if (str.update(frmstr)) {
      vr.changed = true;
    }

    if (str != null) {
      /* Put the status in the cn lang */

      final String formSt = request.getReqPar("categoryStatus");

      if (formSt == null) {
        vr.changed = str.getLang() != null;
        str.setLang(null);
      } else if (!formSt.equals(str.getLang())) {
        str.setLang(formSt);
        vr.changed = true;
      }
    }

    contact.setPhone(checkNull(contact.getPhone()));
    contact.setEmail(checkNull(contact.getEmail()));
    contact.setLink(checkNull(contact.getLink()));

    contact.setLink(fixLink(contact.getLink()));

    return vr;
  }

  /** Return either null (for null or all whitespace) or a url
   * prefixed with http://
   *
   * @param val  String urlk to fix up
   * @return String  fixed up
   */
  private static String fixLink(String val) {
    val = checkNull(val);

    if (val == null) {
      return val;
    }

    if (val.indexOf("://") > 0) {
      return val;
    }

    return "http://" + val;
  }
}
