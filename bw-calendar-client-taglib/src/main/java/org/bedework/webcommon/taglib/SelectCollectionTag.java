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

import org.bedework.calfacade.BwCollection;
import org.bedework.webcommon.tagcommon.BwFormTagUtils;

import java.util.Collection;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.JspWriter;

/** Create drop down list.
 *
 * @author Mike Douglass
 */
public class SelectCollectionTag extends NameScopePropertyTag {
  /** Optional attribute: for those who like tidy xml
   * If specified we add the value after a new line. */
  private String indent = null;

  private String cols;

  /**
   * Constructor
   */
  public SelectCollectionTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final var val = getString(false);
      final var cols = (Collection<BwCollection>)getObject(
              getCols(), getScope(), null, true);

      final JspWriter out = pageContext.getOut();

      BwFormTagUtils.outCollectionSelect(out,
                                         getIndent(),
                                         getProperty(),
                                         val,
                                         cols);
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
      indent = null;
      property = null;
      cols = null;
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

  public void setCols(final String val) {
    cols = val;
  }

  public String getCols() {
    return cols;
  }
}
