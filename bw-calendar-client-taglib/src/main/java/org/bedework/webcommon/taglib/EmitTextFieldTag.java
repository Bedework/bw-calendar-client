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

import org.bedework.webcommon.tagcommon.BwTagUtilCommon.Attribute;
import org.bedework.webcommon.tagcommon.BwTagUtils;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.JspWriter;

import static org.bedework.webcommon.tagcommon.BwFormTagUtils.outTextField;

/** A Tag to emit a tagged text field object. This tag has the
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
public class EmitTextFieldTag extends NameScopePropertyTag {
  /** Optional attribute: name of outer tag */
  private String tagName;

  /** Optional attribute: for those who like tidy xml
   * If specified we add the value after a new line. */
  private String indent = null;

  private String maxlength = null;

  private String size = null;

  private String styleId = null;

  private String styleClass = null;

  private boolean filter = true;

  /**
   * Constructor
   */
  public EmitTextFieldTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      if (getName() == null) {
        setName("calForm");
      }
      final String val = getString(false);

      final JspWriter out = pageContext.getOut();
      final Collection<Attribute> attrs = new ArrayList<>();

      addAttr(attrs, "size", getSize());
      addAttr(attrs, "maxlength", getMaxlength());
      addAttr(attrs, "id", getStyleId());
      addAttr(attrs, "class", getStyleClass());

      outTextField(out, indent, tagName,
                   formatted(val),
                   getProperty(),
                   attrs);
    } catch(final Throwable t) {
      t.printStackTrace();
      throw new JspTagException("Error: " + t.getMessage());
    } finally {
      tagName = null; // reset for next time.
      size = null;
      filter = true; // reset for next time.
    }

    return EVAL_PAGE;
  }

  private void addAttr(final Collection<Attribute> attrs,
                       final String name,
                       final String val) {
    if (val == null) {
      return;
    }

    attrs.add(new Attribute(name, val));
  }

  /**
   * @param val String name
   */
  public void setTagName(final String val) {
    tagName = val;
  }

  /**
   * @return String name
   */
  public String getTagName() {
    return tagName;
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

  public void setMaxlength(final String val) {
    maxlength = val;
  }

  public String getMaxlength() {
    return maxlength;
  }

  public void setSize(final String val) {
    size = val;
  }

  public String getSize() {
    return size;
  }

  public void setStyleId(final String val) {
    styleId = val;
  }

  public String getStyleId() {
    return styleId;
  }

  public void setStyleClass(final String val) {
    styleClass = val;
  }

  public String getStyleClass() {
    return styleClass;
  }

  /** Should we filter output for html?
   *
   * @param val boolean
   */
  public void setFilter(final boolean val) {
    filter = val;
  }

  /** Should we filter output for html?
   *
   * @return boolean
   */
  public boolean getFilter() {
    return filter;
  }

  private String formatted(final String val) {
    if (filter) {
      return BwTagUtils.filter(val);
    }

    return val;
  }
}
