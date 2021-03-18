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
package org.bedework.client.web.rw.sharing;

import org.bedework.calfacade.BwCalendar;
import org.bedework.client.rw.RWClient;
import org.bedework.client.web.rw.BwRWActionForm;
import org.bedework.client.web.rw.RWActionBase;
import org.bedework.webcommon.BwRequest;

/** This action published a collection making it world readable and providing an
 * external url for access by outside users.
 *
 * <p>It also becomes subscribable and will be indexed as a public collection.
 *
 * <p>Parameters to publish are:</p>
 * <ul>
 *      <li>"colHref"             Collection to publish</li>
 *      <li>"unpublish"           to make collection unpublished</li>
 * </ul>
 *
 * <p>Forwards to:</p>
 * <ul>
 *      <li>"noAccess"     user not authorized.</li>
 *      <li>"error"        for problems.</li>
 *      <li>"notFound"     no such calendar.</li>
 *      <li>"continue"     continue on to update page.</li>
 * </ul>
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class PublishCollectionAction extends RWActionBase {
  @Override
  public int doAction(final BwRequest request,
                      final RWClient cl,
                      final BwRWActionForm form) throws Throwable {
    final BwCalendar col = request.getCollection(false);
    if (col == null) {
      return forwardNotFound;
    }

    final boolean remove = request.present("remove");

    if (remove) {
      cl.unpublish(col);
    } else {
      cl.publish(col);
    }

    cl.flushState();

    return forwardContinue;
  }
}

