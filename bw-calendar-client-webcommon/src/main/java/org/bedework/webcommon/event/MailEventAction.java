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
package org.bedework.webcommon.event;

import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.ClientMessage;
import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.mail.ObjectAttachment;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.icalendar.IcalTranslator;
import org.bedework.icalendar.Icalendar;
import org.bedework.util.misc.Util;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import net.fortuna.ical4j.model.Calendar;

/**
 * Action to mail an Event.
 * <p>No request parameters (other than updates to email and subject)
 * <p>Forwards to:<ul>
 *      <li>"retry"        email options still not valid.</li>
 *      <li>"noEvent"      no event was selected.</li>
 *      <li>"success"      mailed (or queued) ok.</li>
 * </ul>
 */
public class MailEventAction extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    EventInfo ei = form.getEventInfo();

    if (ei == null) {
      return forwardNoAction;
    }

    BwEvent ev = ei.getEvent();

    String recipient = request.getReqPar("lastEmail");
    if (recipient == null) {
      request.getErr().emit(ClientError.mailNoRecipient, 1);
      return forwardRetry;
    }

    String subject = request.getReqPar("subject");
    if (!Util.present(subject)) {
      subject = ev.getSummary();
    }

    Message emsg = new Message();
    String[] to = new String[]{recipient};

    emsg.setMailTo(to);
    emsg.setSubject(ev.getSummary());

    IcalTranslator trans = new IcalTranslator(new IcalCallbackcb(request.getClient()));

    Calendar cal = trans.toIcal(ei, Icalendar.methodTypePublish);
    mailMessage(emsg, cal.toString(),
                "event.ics", "text/calendar",
                request.getClient());

    form.getMsg().emit(ClientMessage.mailedEvent);

    return forwardSuccess;
  }

  /** Mail a message to somebody.
   *
   * <p>All required message fields are set. The message will be mailed via
   * the system dependent mailer. If the Object is non-null it will be
   * converted to the appropriate external form and sent as an attachment.
   *
   * @param val      Message to mail
   * @param att      String val to attach - e.g event, todo
   * @param name     name for attachment
   * @param type     mimetype for attachment
   * @param cl
   * @throws Throwable
   */
  private void mailMessage(final Message val,
                           final String att,
                           final String name,
                           final String type,
                           final Client cl) throws Throwable {
    ObjectAttachment oa = new ObjectAttachment();

    oa.setOriginalName(name);
    oa.setVal(att);
    oa.setMimeType(type);

    val.addAttachment(oa);

    if (val.getFrom() == null) {
      // This should be a property
      val.setFrom("donotreply-" + cl.getSystemProperties().getSystemid());
    }

    cl.postMessage(val);
  }
}
