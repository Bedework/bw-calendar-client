/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw.event;

import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.exc.ValidationError;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.webcommon.BwRequest;

import java.util.List;

import static org.bedework.client.web.rw.EventCommon.validateEvent;

/**
 * User: mike Date: 11/21/22 Time: 22:44
 */
public class UpdatePars {
  public final BwRequest request;
  public final RWClient cl;
  public final BwRWActionForm form;
  public final EventInfo ei;
  public final BwEvent ev;

  final boolean submitApp;

  public String preserveColPath;

  public final ChangeTable changes;

  public final boolean adding;

  public final boolean sendInvitations;

  public String unindexLocation;

  protected UpdatePars(final BwRequest request,
                       final RWClient cl,
                       final BwRWActionForm form) {
    this.request = request;
    this.cl = cl;
    this.form = form;
    submitApp = cl.getWebSubmit();

    ei = form.getEventInfo();
    ev = ei.getEvent();

    /* This should be done by a wrapper */
    changes = ei.getChangeset(cl.getCurrentPrincipalHref());
    adding = form.getAddingEvent();
    sendInvitations = request.present("submitAndSend");

    if (ev != null) {
      /* We have a problem with roll back in the case of errors.
       * Hibernate will actually update the event as we change fields and
       * really we should do a roll back on any failure.
       *
       * However, this causes a problem with the UI. All this should be
       * resolved with the newer client approach which doesn't update
       * this way. For the moment preserve some important value(s).
       */

      preserveColPath = ev.getColPath();
    }
  }

  public List<ValidationError> validate() {
    final int maxDescLen;

    if (submitApp) {
      maxDescLen = cl.getAuthProperties()
                     .getMaxPublicDescriptionLength();
    } else {
      maxDescLen = cl.getAuthProperties()
                     .getMaxUserDescriptionLength();
    }

    return validateEvent(cl, maxDescLen, !submitApp, ev);
  }
}
