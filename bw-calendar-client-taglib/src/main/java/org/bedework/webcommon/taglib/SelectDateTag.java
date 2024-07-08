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

import org.bedework.webcommon.TimeDateComponents;
import org.bedework.webcommon.tagcommon.BwFormTagUtils;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/** Create drop down list.
 *
 * @author Mike Douglass
 */
public class SelectDateTag extends NameScopePropertyTag {
  /** Optional attribute: for those who like tidy xml
   * If specified we add the value after a new line. */
  private String indent = null;

  private boolean notag;

  private boolean noyear;

  private String[] yearVals;

  /**
   * Constructor
   */
  public SelectDateTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final var val = (TimeDateComponents)getObject(true);

      final JspWriter out = pageContext.getOut();

      BwFormTagUtils.outDateSelect(out,
                                   getIndent(),
                                   getProperty(),
                                   val,
                                   yearVals,
                                   getNotag(),
                                   getNoyear());
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
      indent = null;
      property = null;
      notag = false;
      noyear = false;
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

  public void setNotag(final boolean val) {
    notag = val;
  }

  public boolean getNotag() {
    return notag;
  }

  public void setNoyear(final boolean val) {
    noyear = val;
  }

  public boolean getNoyear() {
    return noyear;
  }

  public void setYearVals(final String[] val) {
    yearVals = val;
  }

  public String[] getYearVals() {
    return yearVals;
  }
}
