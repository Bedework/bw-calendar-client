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

import org.bedework.util.webaction.Request;

/** This is a no-op action
 *
 * @author Mike Douglass  douglm - rpi.edu
 */
public class RenderAction extends BwAbstractAction {
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {
    if (form.getNewSession()) {
      request.refresh();
//      return forwardGotomain;
    }

    return forwardSuccess;
  }

  @Override
  public String getContentName(final Request req) {
    String contentName = req.getContentName();

    if (contentName != null) {
      return contentName;
    }

    contentName = req.getReqPar("contentName");
    req.setContentName(null);  // It's a one shot and we're about to render

    return contentName;
  }

  @Override
  public boolean clearMessagesOnEntry() {
    return false;
  }
}

