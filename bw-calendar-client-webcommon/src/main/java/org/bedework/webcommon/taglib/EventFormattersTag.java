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

package org.bedework.webcommon.taglib;

import org.bedework.appcommon.EventFormatter;
import org.bedework.appcommon.TimeViewDailyInfo;

import org.apache.log4j.Logger;
import org.apache.struts.taglib.TagUtils;

import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/** Tag to get formatted events. Allows us to supply the client. <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Object proeprty containing CurrentAccess object. </li>
 * <li><b>id<b> Where we set the object. </li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class EventFormattersTag extends NameScopePropertyTag {
  /**
   * The name of the scripting variable that will be exposed as a page
   * scope attribute.
   */
  protected String id = null;

  /** Name of the form - needed to get the open state
   */
  protected String form = null;

  /**
   * Constructor
   */
  public EventFormattersTag() {
  }

  /* (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#setId(java.lang.String)
   */
  public void setId(String val) {
    id = val;
  }

  /* (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#getId()
   */
  public String getId() {
    return id;
  }

  /**
   * @param val
   */
  public void setForm(String val) {
    form = val;
  }

  /**
   * @return form name
   */
  public String getForm() {
    return form;
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      Object o = getObject(false);
      if (!(o instanceof TimeViewDailyInfo)) {
        JspException e =
          new JspException("Property is not instance of TimeViewDailyInfo");
        TagUtils.getInstance().saveException(pageContext, e);
        throw e;
      }

      TimeViewDailyInfo tvdi = (TimeViewDailyInfo)o;

      Collection<EventFormatter> efs = tvdi.getEventFormatters(getClient());

      int inScope = PageContext.PAGE_SCOPE;
      try {
        if (getScope() != null) {
          inScope = TagUtils.getInstance().getScope(getScope());
        }
      } catch (JspException e) {
        Logger.getLogger(getClass()).warn("toScope was invalid name " +
                                          "so we default to PAGE_SCOPE", e);
      }

      pageContext.setAttribute(id, efs, inScope);
    } catch(Throwable t) {
      Logger.getLogger(getClass()).debug(this, t);
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }
}
