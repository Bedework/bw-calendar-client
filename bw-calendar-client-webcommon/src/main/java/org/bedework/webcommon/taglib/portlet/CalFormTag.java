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

import org.apache.log4j.Logger;
import org.apache.portals.bridges.struts.PortletServlet;
import org.apache.portals.bridges.struts.config.PortletURLTypes;
import org.apache.struts.taglib.html.FormTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/** Supports the Struts html:form tag to be used within uPortlet context for
 * generating urls for the bedework portlet.
 *
 * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
 * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
 * @author Mike Douglass    douglm at bedework.edu
 * @author Dave Brondsema
 * @version $Id: RewriteTag.java 2005-10-25 12:31:13Z satish $
 */
public class CalFormTag extends FormTag {
  private transient Logger log;

  /**
   * Modifies the default generated form action url to be a valid Portlet ActionURL
   * when in the context of a {@link PortletServlet#isPortletRequest(ServletRequest) PortletRequest}.
   * @return the formStartElement
   */
  @Override
  protected String renderFormStartElement() {
    boolean debug = getLogger().isDebugEnabled();

    String formStartElement;
    try {
      formStartElement = super.renderFormStartElement();
    } catch (JspException e) {
      error(e);
      return e.getMessage();
    }

    if (!PortletServlet.isPortletRequest(pageContext.getRequest())) {
      return formStartElement;
    }

    int actionURLStart = formStartElement.indexOf("action=") + 8;
    int actionURLEnd = formStartElement.indexOf('"', actionURLStart);

    String urlStr = formStartElement.substring(actionURLStart,
                                               actionURLEnd);

    if (debug) {
      trace("UrlStr = " + urlStr);
    }

    /* Drop the context
     */
    int pos = urlStr.indexOf('/');
    if (pos > 0) {
      urlStr = urlStr.substring(pos);
    }

    if (debug) {
      trace("UrlStr = " + urlStr);
    }

    urlStr = TagsSupport.getURL(pageContext, urlStr,
                                PortletURLTypes.URLType.ACTION);

    if (debug) {
      trace("UrlStr = " + urlStr);
    }

    /* remove embedded anchor because calendar xsl stylesheet
     * adds extra parameters later during transformation
     */
    pos = urlStr.indexOf('#');
    if (pos > -1) {
      urlStr = urlStr.substring(0, pos);
    }

    /* Remove bedework dummy request parameter -
     * it's an encoded form of ?b=de */
    urlStr = urlStr.replaceAll(TagsSupport.bedeworkDummyPar, "");

    //Generate valid xml markup for transformation
    urlStr = urlStr.replaceAll("&", "&amp;");

    if (debug) {
      trace("UrlStr = " + urlStr);
    }

    return formStartElement.substring(0, actionURLStart) +
           urlStr + formStartElement.substring(actionURLEnd);
  }

  private void trace(final String msg) {
    getLogger().debug(msg);
  }

  private void error(final Throwable t) {
    getLogger().error(this, t);
  }

  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
