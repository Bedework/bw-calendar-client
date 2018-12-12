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

import org.apache.struts.taglib.html.LinkTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/** Supports the Struts html:link tag to be used within uPortlet context for
 * generating urls for the bedework portlet.
 *
 * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
 * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
 * @author Mike Douglass    douglm at rpi.edu
 * @author Dave Brondsema
 * @version $Id: RewriteTag.java 2005-10-25 12:31:13Z satish $
 */
public class CalLinkTag extends LinkTag {
  /** Indicates which type of a url must be generated: action, render or resource.
   * <p>If not specified, the type will be determined by
   * {@link PortletURLTypes#getType(String)}</p>.
   */
  //protected PortletURLTypes.URLType urlType = null;

  /** Generates a PortletURL or a ResourceURL for the link when in the context of a
   * {@link PortletServlet#isPortletRequest(ServletRequest) PortletRequest}, otherwise
   * the default behaviour is maintained.
   *
   * @return the link url
   * @exception JspException if a JSP exception has occurred
   * /
  protected String calculateURL() throws JspException {
    String urlStr = super.calculateURL();

    if (!PortletServlet.isPortletRequest(pageContext.getRequest())) {
      return urlStr;
    }

    try {
      // get just the path, ignoring query params and fragments
      urlStr = new URI(urlStr).getPath();

      urlType = TagsSupport.calculateURLType(urlStr);

      if (debug()) {
        debug("UrlStr = " + urlStr);
      }

      /* Drop the context
       * /
      int pos = urlStr.indexOf('/');
      if (pos > 0) {
        urlStr = urlStr.substring(pos);
      }

      if (debug()) {
        debug("UrlStr = " + urlStr);
      }

      urlStr = TagsSupport.getURL(pageContext, urlStr, urlType);

      if (debug()) {
        debug("UrlStr = " + urlStr);
      }

      /* remove embedded anchor because calendar xsl stylesheet
       * adds extra parameters later during transformation
       * /
      pos = urlStr.indexOf('#');
      if (pos > -1) {
        urlStr = urlStr.substring(0, pos);
      }

      if (urlType.equals(PortletURLTypes.URLType.RESOURCE)) {
        /* Add bedework dummy request parameter * /
        urlStr = urlStr + TagsSupport.bedeworkDummyParUnencoded;
      } else {
        /* Remove bedework dummy request parameter -
         * it's an encoded form of ?b=de * /
        urlStr = urlStr.replaceAll(TagsSupport.bedeworkDummyPar, "");
      }

      //Generate valid xml markup for transformation
      urlStr = urlStr.replaceAll("&", "&amp;");

      if (debug()) {
        debug("UrlStr = " + urlStr);
      }
    } catch (URISyntaxException use) {
      throw new JspException(use);
    }

    return urlStr;
  }

  public void release() {
    super.release();
    urlType = null;
  }
  */
}
