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

import org.bedework.calfacade.BwDateTime;

import java.util.Collection;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.JspWriter;

/** A simple Tag to emit rdates or exdates. This tag has the attributes, <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Object property containing rdates </li>
 * </ul>
 *
 * We retrieve the value and emit xml.
 *
 * @author Mike Douglass
 */
public class EmitRExDatesTag extends EmitTextTag {
  private boolean exdates;

  /**
   * Constructor
   */
  public EmitRExDatesTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      Collection<BwDateTime> dates = (Collection<BwDateTime>)getObject(false);
      if (dates == null) {
        return EVAL_PAGE;
      }

      String outerTag = getTagName();
      if (outerTag == null) {
        outerTag = property;
      }

      if (outerTag == null) {
        outerTag = "name";
      }

      String innerTag;

      if (exdates) {
        innerTag = "exdate";
      } else {
        innerTag = "rdate";
      }

      JspWriter out = pageContext.getOut();

      String indent = getIndent();
      if (indent == null) {
        indent = "";
      }

      out.print('<');
      out.print(outerTag);
      out.println('>');

      for (BwDateTime dt: dates) {
        out.print(indent);
        out.print("  ");
        out.print('<');
        out.print(innerTag);
        out.print('>');

        String dtval = dt.getDtval();

        out.print("<date>");
        out.print(dtval.substring(0, 8));
        out.print("</date>");

        out.print("<time>");
        if (dtval.length() > 8) {
          out.print(dtval.substring(9, 13));
        }
        out.print("</time>");

        out.print("<tzid>");
        if (dt.getTzid() != null) {
          out.print(dt.getTzid());
        }
        out.print("</tzid>");

        out.print("</");
        out.print(innerTag);
        out.println('>');
      }

      out.print(indent);
      out.print("</");
      out.print(outerTag);
      out.println('>');
    } catch(Throwable t) {
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }

  /** Sets the exdates value.
   *
   * @param val
   */
  public void setExdates(boolean val) {
    exdates = val;
  }

  /** Returns the exdates value.
   *
   * @return boolean
   */
  public boolean getExdates() {
      return exdates;
  }
}
