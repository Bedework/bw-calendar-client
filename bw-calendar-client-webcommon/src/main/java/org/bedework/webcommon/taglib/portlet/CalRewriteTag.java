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

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import org.apache.struts.taglib.html.RewriteTag;

import javax.servlet.jsp.JspException;

//import org.apache.portals.bridges.struts.config.PortletURLTypes;

//import org.apache.portals.bridges.struts.PortletServlet;
//import org.apache.portals.bridges.struts.config.PortletURLTypes;

/** Supports the Struts html:rewrite tag to be used within uPortlet context for
 * generating urls for the bedework portlet.
 *
 * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
 * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
 * @author Mike Douglass    douglm at rpi.edu
 * @author Dave Brondsema
 * @version $Id: RewriteTag.java 2005-10-25 12:31:13Z satish $
 */
public class CalRewriteTag extends RewriteTag implements Logged {
  /** Specifically flagged as action url
   */
  protected boolean actionURL = false;

  /** Specifically flagged as render url
   */
  protected boolean renderURL = false;

  /** Specifically flagged as resource url
   */
  protected boolean resourceURL = false;

  /* Indicates which type of a url must be generated: action, render or resource.
   * <p>If not specified, the type will be determined by
   * {@link PortletURLTypes#getType(String)}</p>.
   */
  //protected PortletURLTypes.URLType urlType = null;

  /* Generates a PortletURL or a ResourceURL for the link when in the context of a
   * {@link PortletServlet#isPortletRequest(ServletRequest) PortletRequest}, otherwise
   * the default behaviour is maintained.
   *
   * @return the link url
   * @exception JspException if a JSP exception has occurred
   */
  public int doStartTag() throws JspException {
    return super.doStartTag();

    /* Disable portal support for the time being - class loader errors in wildfly
    if (!TagsSupport.isPortletRequest(pageContext.getRequest())) {
      return super.doStartTag();
    }

    BodyContent bodyContent = pageContext.pushBody();

    super.doStartTag();
    String urlStr = bodyContent.getString();
    if (debug()) {
      trace(bodyContent.getString());
    }

    if (actionURL) {
      urlType = PortletURLTypes.URLType.ACTION;
    } else if (renderURL) {
        urlType = PortletURLTypes.URLType.RENDER;
    } else if (resourceURL) {
      urlType = PortletURLTypes.URLType.RESOURCE;
    } else {
      urlType = TagsSupport.calculateURLType(urlStr);
    }

    if (debug()) {
      String type = "unknown";
      if (urlType == null) {
        type = "null";
      } else if (urlType.equals(PortletURLTypes.URLType.ACTION)) {
        type = "ACTION";
      } else if (urlType.equals(PortletURLTypes.URLType.RENDER)) {
        type = "RENDER";
      } else if (urlType.equals(PortletURLTypes.URLType.RESOURCE)) {
        type = "RESOURCE";
      }
      debug("urlType = " + type + " UrlStr = " + urlStr);
    }

    urlStr = commonStartTag(urlStr);

    pageContext.popBody();

    TagUtils.getInstance().write(pageContext, urlStr);

    return (SKIP_BODY);
    */
  }

  /** Sets the resourceURL value.
   *
   * @param val
   */
  public void setResourceURL(boolean val) {
      resourceURL = val;
  }

  /** Returns the resourceURL value.
   *
   * @return boolean
   */
  public boolean getResourceURL() {
      return resourceURL;
  }

  /** Sets the renderURL value.
   *
   * @param val
   */
  public void setRenderURL(boolean val) {
    renderURL = val;
  }

  /** Returns the renderURL value.
   *
   * @return boolean
   */
  public boolean getRenderURL() {
      return renderURL;
  }

  /** Sets the actionURL value.
   *
   * @param val
   */
  public void setActionURL(boolean val) {
    actionURL = val;
  }

  /** Returns the actionURL value.
   *
   * @return boolean
   */
  public boolean getActionURL() {
      return actionURL;
  }

  /* Generates a URL for the link once urlType is set when in the context of a
   * {@link PortletServlet#isPortletRequest(ServletRequest) PortletRequest}, otherwise
   * the default behaviour is maintained.
   *
   * @return the link url
   * @exception JspException if a JSP exception has occurred
   * /
  protected String commonStartTag(String urlStr) throws JspException {
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
    int pos = urlStr.indexOf('#');
    if (pos > -1) {
      urlStr = urlStr.substring(0, pos);
    }

    /* Remove bedework dummy request parameter -
     * it's an encoded form of ?b=de * /
    urlStr = urlStr.replaceAll(TagsSupport.bedeworkDummyPar, "");

    //Generate valid xml markup for transformation
    urlStr = urlStr.replaceAll("&", "&amp;");

    if (debug()) {
      debug("UrlStr = " + urlStr);
    }

    return urlStr;
  }

  public void release() {
    super.release();
    urlType = null;
  }

  */

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
