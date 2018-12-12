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

import org.bedework.access.CurrentAccess;
import org.bedework.appcommon.AccessXmlUtil;
import org.bedework.appcommon.client.Client;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/** A simple Tag to emit acls. This tag has the attributes, <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Object proeprty containing CurrentAccess object. </li>
 * </ul>
 *
 * We retrieve the value and emit xml.
 *
 * @author Mike Douglass
 */
public class EmitAclTag extends NameScopePropertyTag {
  /**
   * Constructor
   */
  public EmitAclTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  @Override
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      String val = getXmlAcl((CurrentAccess)getObject(false));

      JspWriter out = pageContext.getOut();

      if (val != null) {
        out.print(val);
      }
    } catch(Throwable t) {
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }

  private String getXmlAcl(final CurrentAccess ca) throws Throwable {
    if (ca == null) {
      return null;
    }

    Client cl = getClient();
    return AccessXmlUtil.getXmlAclString(ca.getAcl(), cl);
  }
}
