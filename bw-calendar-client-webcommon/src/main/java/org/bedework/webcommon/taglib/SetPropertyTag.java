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

import org.bedework.calfacade.BwProperty;
import org.bedework.calfacade.base.PropertiesEntity;
import org.bedework.util.logging.BwLogger;

import org.apache.struts.taglib.TagUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/** Tag to set a named property for a PropertiesEntity object. <ul>
 * <li><b>name<b> (Required) defines the name of an object embedded somewhere
 *               in the page context.</li>
 * <li><b>property<b> if defined proeprty of name containing PropertiesEntity object. </li>
 * <li><b>pname<b> Name of property to set. </li>
 * <li><b>pval<b> Value of property to set. </li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class SetPropertyTag extends NameScopePropertyTag {
  /**
   * The name of the property to set
   */
  protected String pname = null;

  /** Value to set
   */
  protected String pval = null;

  /**
   * Constructor
   */
  public SetPropertyTag() {
  }

  /**
   * @param val property name
   */
  public void setPname(final String val) {
    pname = val;
  }

  /**
   * @return String name
   */
  public String getPname() {
    return pname;
  }

  /**
   * @param val property value
   */
  public void setPval(final String val) {
    pval = val;
  }

  /**
   * @return String val
   */
  public String getPval() {
    return pval;
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final Object o = getObject(false);
      if (!(o instanceof PropertiesEntity)) {
        final JspException e =
          new JspException("Property is not instance of PropertiesEntity");
        TagUtils.getInstance().saveException(pageContext, e);
        throw e;
      }

      final PropertiesEntity pe = (PropertiesEntity)o;

      final BwProperty prop = pe.findProperty(pname);

      if (prop == null) {
        pe.addProperty(new BwProperty(pname, pval));
      } else {
        prop.setValue(pval);
      }
    } catch(final Throwable t) {
      getLog().error(t);
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }

  private final static BwLogger logger =
          new BwLogger().setLoggedClass(SetPropertyTag.class);

  private static BwLogger getLog() {
    return logger;
  }
}
