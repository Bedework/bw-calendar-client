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
/**
 *
 */
package org.bedework.portal.liferay;
//package com.liferay.portal.apache.bridges;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.portals.bridges.struts.StrutsPortlet;
import org.apache.struts.Globals;

/**
 * Added this to bedework for th etime being. It doesn't appear in any jar files
 * I could find and it was easier to add it here.
 *
 * LiferayStrutsPortlet: this portlet adds the additional functionality (over StrutsPortlet) to restore overwritten resources.
 * It works such that:
 * <ul>
 * <li>message resources that were in in the request BEFORE the call to processRequest are saved restored afterward such
 *   as to avoid conflicts with resources used for the portal itself (and other struts portlets)</li>
 * </ul>
 *
 * @author James Schopp
 *
 */
public class LiferayStrutsPortlet extends StrutsPortlet {
  protected void processRequest(PortletRequest request, PortletResponse response,
                                String defaultPage, String requestType)
          throws PortletException, IOException {
    HttpServletRequest req = getHttpServletRequest(this, request, response);
    Object objMessages = req.getAttribute(Globals.MESSAGES_KEY);

    super.processRequest(request, response, defaultPage, requestType);

    req.setAttribute(Globals.MESSAGES_KEY, objMessages);
  }
}
