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
package org.bedework.portal.uportal;


import javax.portlet.GenericPortlet;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.core.impl.PortletContextImpl;
import org.apache.portals.bridges.common.ServletContextProvider;

/**
 * ServletContextProviderImpl supplies access to the Servlet context of uPortal Portlet.
 *
 * @author Satish Sekharan
 */
public class PortalServletContextProvider implements ServletContextProvider {

  public ServletContext getServletContext(GenericPortlet portlet) {
    return ((PortletContextImpl)portlet.getPortletContext())
        .getServletContext();
  }

  public HttpServletRequest getHttpServletRequest(GenericPortlet portlet,
                                                  PortletRequest request)  {
    return (HttpServletRequest)((HttpServletRequestWrapper)request).getRequest();
  }


  public HttpServletResponse getHttpServletResponse(GenericPortlet portlet,
                                                    PortletResponse response) {
    PortletResponseWrapper wrapper = new PortletResponseWrapper((HttpServletResponse)response);

    return (HttpServletResponse)wrapper.getResponse();
  }

}
