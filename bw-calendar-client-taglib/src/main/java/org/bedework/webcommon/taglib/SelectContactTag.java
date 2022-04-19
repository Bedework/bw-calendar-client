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

import org.bedework.calfacade.BwContact;
import org.bedework.webcommon.tagcommon.BwFormTagUtils;

import java.util.Collection;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/** Create drop down list.
 *
 * @author Mike Douglass
 */
public class SelectContactTag extends NameScopePropertyTag {
  /** Optional attribute: for those who like tidy xml
   * If specified we add the value after a new line. */
  private String indent = null;

  private String contacts;

  /**
   * Constructor
   */
  public SelectContactTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final var val = getString(false);
      final var contacts = (Collection<BwContact>)getObject(
              getContacts(), getScope(), null, true);

      final JspWriter out = pageContext.getOut();

      BwFormTagUtils.outContactSelect(out,
                                      getIndent(),
                                      getProperty(),
                                      val,
                                      contacts);
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
      indent = null;
      property = null;
      contacts = null;
    }

    return EVAL_PAGE;
  }

  /**
   * @param val String indent
   */
  public void setIndent(final String val) {
    indent = val;
  }

  /**
   * @return  String indent
   */
  public String getIndent() {
    return indent;
  }

  public void setContacts(final String val) {
    contacts = val;
  }

  public String getContacts() {
    return contacts;
  }
}
