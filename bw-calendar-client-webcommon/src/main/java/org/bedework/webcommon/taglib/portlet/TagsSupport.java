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
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bedework.webcommon.taglib.portlet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.apache.portals.bridges.struts.PortletServlet;
import org.apache.portals.bridges.struts.StrutsPortlet;
import org.apache.portals.bridges.struts.StrutsPortletURL;
import org.apache.portals.bridges.struts.config.StrutsPortletConfig;
import org.apache.portals.bridges.struts.config.PortletURLTypes; // javadoc

/** Utility class providing common Struts Bridge Tags functionality.
 *
 * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
 * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
 * @author Mike Douglass    douglm at bedework.edu
 * @author Dave Brondsema
 * @version $Id: TagsSupport.java 306958 2005-10-06 23:23:37Z ate $
 */
class TagsSupport {
  /* bedework dummy request parameter - it's an encoded form of ?b=de */
  static final String bedeworkDummyPar = "%3Fb%3Dde";
  static final String bedeworkDummyParUnencoded = "?b=de";

  /**
   * Private constructor as this class isn't supposed to be instantiated.
   *
   */
  private TagsSupport(){}

  /**
   * Determine a url type based on file extension in a URL
   * @param urlStr a URL string without query params or a fragment
   * @return the url type
   */
  public static PortletURLTypes.URLType calculateURLType(String urlStr) {
    if (urlStr.endsWith(".rdo")) {
      return PortletURLTypes.URLType.RENDER;
    } else if (urlStr.endsWith(".do")) {
      return PortletURLTypes.URLType.ACTION;
    } else if (urlStr.endsWith(".gdo")) {
      return PortletURLTypes.URLType.RESOURCE;
    }
    return null;
  }


  /** Return true if this is a portlet request
   *
   * @param request
   * @return boolean true for portlet
   */
  public static boolean isPortletRequest(ServletRequest request) {
    return request.getAttribute("javax.portlet.request") != null;
  }

  /** Resolves a, possibly relative, url to a context relative one for use
   * within a Portlet context.
   *
   * <p>The url parameter may contain relative (../) elements.
   *
   * @param pageContext the JSP pageContext
   * @param url the url to resolve
   * @param addContextPath
   * @return the resolved url
   */
  public static String getContextRelativeURL(PageContext pageContext,
                                             String url,
                                             boolean addContextPath) {
    if ( !url.startsWith("/")) {
      String newUrl = url;
      String currentPath =
        (String)pageContext.getRequest().getAttribute(StrutsPortlet.PAGE_URL);

      if (addContextPath) {
        currentPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath() +
                      currentPath;
      }

      if (addContextPath || currentPath.length() > 1 /* keep "/" */) {
        currentPath = currentPath.substring(0,currentPath.lastIndexOf('/'));
      }

      if (currentPath.length() == 0) {
        currentPath = "/";
      }

      while (currentPath.length() > 0) {
        if (!newUrl.startsWith("../")) {
          break;
        }

        currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
        newUrl = newUrl.substring(3);
      }

      if (currentPath.length() > 1) {
        url = currentPath + "/" + newUrl;
      } else {
        url = "/" + newUrl;
      }
    }

    return url;
  }

  /**
   * Creates an action or render PortletURL, or a ResourceURL.
   *
   * <p>The url parameter is first
   * {@link #getContextRelativeURL(PageContext, String, boolean) resolved}
   * to an context relative url.<br/>
   * Then, a prefixed contextPath is removed from the resulting url.<br/>
   * If the type parameter is specified (not null), the type of url created is based on its value.<br/>
   * Otherwise, {@link PortletURLTypes#getType(String)} is used to determine which
   * type of url must be created.
   *
   * @param pageContext the JSP pageContext
   * @param url the url to resolve
   * @param type indicated which type of url must be created
   * @return an action or render PortletURL, or a ResourceURL
   */
  public static String getURL(PageContext pageContext,
                              String url,
                              PortletURLTypes.URLType type) {
    Logger log = Logger.getLogger(TagsSupport.class);
    boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug("url = " + url);
    }

    url = getContextRelativeURL(pageContext, url, false);

    if (debug) {
      log.debug("url = " + url);
    }

    if (type == null) {
      String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
      String urlStr;

      if (url.startsWith(contextPath + "/")) {
        urlStr = url.substring(contextPath.length() + 1);
      } else {
        urlStr = url;
      }

      StrutsPortletConfig strutsPortletConfig = (StrutsPortletConfig)pageContext.getAttribute(StrutsPortlet.STRUTS_PORTLET_CONFIG,PageContext.APPLICATION_SCOPE);
      type = strutsPortletConfig.getPortletURLTypes().getType(urlStr);
    }
    if (debug) {
      log.debug("urlType = " + type + " url = " + url);
    }

    if (type.equals(PortletURLTypes.URLType.ACTION)) {
      return StrutsPortletURL.createActionURL(pageContext.getRequest(),url).toString();
    }

    if (type.equals(PortletURLTypes.URLType.RENDER)) {
      return StrutsPortletURL.createRenderURL(pageContext.getRequest(),url).toString();
    }

    if (type.equals(PortletURLTypes.URLType.RESOURCE)) {
    //if (url.startsWith("/")) {
      return url;
    }

    //return contextPath + "/" + url;
    return url;
  }

  /** Replaces the action url as generated by the struts:form tag with an action PortletURL.
   *
   * @param pageContext the JSP pageContext
   * @param formStartElement the formStartElement as generated by the struts:form tag
   * @return the formStartElement containing an action PortletURL
   */
  public static String getFormTagRenderFormStartElement(PageContext pageContext,
                                                        String formStartElement) {
    if (PortletServlet.isPortletRequest(pageContext.getRequest())) {
      int actionURLStart = formStartElement.indexOf("action=") + 8;
      int actionURLEnd = formStartElement.indexOf('"', actionURLStart);
      String actionURL = formStartElement.substring(actionURLStart,
                                                    actionURLEnd);

      formStartElement = formStartElement.substring(0, actionURLStart) +
                         StrutsPortletURL.createActionURL(pageContext.getRequest(),
                                                          actionURL).toString() +
                                     formStartElement.substring(actionURLEnd);
    }

    return formStartElement;
  }
}
