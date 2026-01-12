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
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwRequest;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

import static org.bedework.webcommon.DateViewUtil.gotoDateView;

/**
 * Action to go to a given date and/or set a day/week/month/year view.
 * <p>Request parameter<br>
 *      "date" date to move to.
 * <p>Optional request parameter<br>
 *      "viewType" type of view we want, day, week, etc.
 *
 */
public class GoToAction extends BwAbstractAction {
  private String date;

  @Override
  public String doAction(final BwRequest request) {
    final Client cl = request.getClient();

    cl.setViewMode(Client.gridViewMode);

    gotoDateView(request,
                 getDate(),
                 request.getViewType());

    return forwardSuccess;
  }

  @StrutsParameter
  public void setDate(final String val) {
    date = val;
  }

  public String getDate() {
    return date;
  }
}
