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
import org.bedework.calfacade.base.BwShareableContainedDbentity;
import org.bedework.util.logging.BwLogger;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.JspWriter;

/** Emit information about the container for the named object. Parameters, <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Optional property</li>
 * <li><b>tagname<b> Optional property</li>
 * </ul>
 *
 * We retrieve the value and emit xml.
 *
 * @author Mike Douglass
 */
public class EmitContainerTag extends EmitCollectionTag {
  /**
   * Constructor
   */
  public EmitContainerTag() {
  }

  /** Called at end of Tag
   *
   * @return int      either EVAL_PAGE or SKIP_PAGE
   */
  @Override
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the object */
      BwCollection col = null;
      BwShareableContainedDbentity entity = (BwShareableContainedDbentity)getObject(false);
      if (entity != null) {
        col = getClient().getCollection(entity.getColPath());

        if (col != null) {
          getClient().resolveAlias(col, true, false);
        }
      }

      String outerTag = null;

      if (!getNoTag()) {
        outerTag = getTagName();
        if (outerTag == null) {
          outerTag = property;
        }

        if (outerTag == null) {
          outerTag = "container";
        }
      }

      final JspWriter out = pageContext.getOut();

      String indent = getIndent();
      if (indent == null) {
        indent = "";
      }

      outerTagStart(out, outerTag);

      emitCollection(col, out, indent);

      outerTagEnd(out, outerTag, indent);
    } catch(final Throwable t) {
      if (getLog().isDebugEnabled()) {
        getLog().error("Error", t);
      }

      throw new JspTagException("Error: " + t.getMessage());
    } finally {
    }

    return EVAL_PAGE;
  }

  private final static BwLogger logger =
          new BwLogger().setLoggedClass(EmitContainerTag.class);

  private static BwLogger getLog() {
    return logger;
  }
}
