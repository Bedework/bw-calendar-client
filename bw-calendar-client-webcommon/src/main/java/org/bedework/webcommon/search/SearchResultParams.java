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
package org.bedework.webcommon.search;

import org.bedework.appcommon.client.Client;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

/**
 * Set search result retrieval params.
 *
 * <p>Request parameters<ul>
 *      <li>"pageNum"        page number 1-n</li>
 * </ul>
 *
 * <p>If neither are present will increment last page number by 1.
 * For no last page# will set it to 1.
 *
 * <p>Forwards to:<ul>
 *      <li>"noAction"     no query.</li>
 *      <li>"success"      ok.</li>
 * </ul>

 * @author Mike Douglass
 */
public class SearchResultParams extends BwAbstractAction {
  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  public int doAction(BwRequest request,
                      BwActionFormBase form) throws Throwable {
    Client cl =form.fetchClient();

    int pageNum = request.getIntReqPar("pageNum", -1);
    int start;
    int count;

    if (pageNum < 0) {
      pageNum = form.getCurPage() + 1;
    }

    if (pageNum == 0) {
      pageNum = 1;
    }

    count = cl.getPreferences().getPageSize();

    start = (pageNum - 1) * count;
    form.setResultCt(count);
    form.setPrevPage(Math.max(0, pageNum - 1));
    form.setCurPage(pageNum);

    if (pageNum == form.getNumPages()) {
      form.setNextPage(0);
    } else {
      form.setNextPage(pageNum + 1);
    }

    form.setResultStart(start);
    form.setResultCt(count);

    return forwardSuccess;
  }
}
