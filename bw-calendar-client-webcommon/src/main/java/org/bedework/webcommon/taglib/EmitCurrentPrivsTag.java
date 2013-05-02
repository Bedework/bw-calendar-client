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


import org.bedework.appcommon.AccessXmlUtil;

import edu.rpi.cmt.access.Acl.CurrentAccess;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspTagException;

/** A simple Tag to emit a tagged text object. This tag has the
 * attributes, <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>tagName<b> (Optional)defines the name of the tag. </li>
 * <li><b>indent<b> (Optional) string indent value</li>
 * </ul>
 *
 * We retrieve the value and emit xml.
 *
 * @author Mike Douglass
 */
public class EmitCurrentPrivsTag extends NameScopePropertyTag {
  /** Optional attribute: name of outer tag */
  private String tagName;

  /**
   * Constructor
   */
  public EmitCurrentPrivsTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      String val = getXmlAccess((CurrentAccess)getObject(false));

      JspWriter out = pageContext.getOut();

      if (tagName == null) {
        tagName = property;
      }

      // Assume we're indented for the first tag
      out.print('<');
      out.print(tagName);
      out.print('>');
      if (val != null) {
        out.print(val);
      }
      out.print("</");
      out.print(tagName);
      out.println('>');
    } catch(Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
      tagName = null; // reset for next time.
    }

    return EVAL_PAGE;
  }

  private String getXmlAccess(CurrentAccess ca) throws Throwable {
    if ((ca == null) || (ca.getPrivileges() == null)) {
      return null;
    }

    return AccessXmlUtil.getCurrentPrivSetString(ca.getPrivileges());
  }

  /**
   * @param val String name
   */
  public void setTagName(String val) {
    tagName = val;
  }

  /**
   * @return String name
   */
  public String getTagName() {
    return tagName;
  }
}
