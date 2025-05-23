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
import org.bedework.util.logging.BwLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;

/** Tag to define a bean which is a Collection of accessible children of the
 * named calendar object. <ul>
 * <li><b>name<b> (Required) defines the name of object embedded somewhere
 *               in the page context containing the path object.</li>
 * <li><b>property<b> Object proeprty containing CurrentAccess object. </li>
 * <li><b>id<b> Where we set the object. </li>
 * </ul>
 *
 * @author Mike Douglass
 */
public class GetChildrenTag extends NameScopePropertyTag {
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
  public GetChildrenTag() {
  }

  @Override
  public void setId(final String val) {
    id = val;
  }

  @Override
  public String getId() {
    return id;
  }

  /**
   * @param val name of form
   */
  public void setForm(final String val) {
    form = val;
  }

  /**
   * @return form name
   */
  public String getForm() {
    return form;
  }

  @Override
  public int doEndTag() throws JspTagException {
    try {
      /* Try to retrieve the value */
      final Object o = getObject(false);
      Collection<BwCollection> cs;

      if (o == null) {
        cs = new ArrayList<>();
      } else {
        if (!(o instanceof BwCollection)) {
          throw new JspException("Property is not instance of BwCollection");
        }

        final BwCollection col = (BwCollection)o;

        cs = col.getChildren();
      }

      if (cs == null) {
        //Logger.getLogger(getClass()).warn("Children == null for " + o);
        cs = new ArrayList<>();
      } else {
        if (getForm() == null) {
          // Assume always open
          for (final BwCollection c: cs) {
            c.setOpen(true);
          }
        } else {
          //noinspection unchecked
          final Set<String> cos =
                  (Set<String>)getObject(getForm(), null,
                                         "calendarsOpenState",
                                         false);

          if (cos != null) {
            for (final BwCollection c: cs) {
              c.setOpen(cos.contains(c.getPath()));
            }
          }
        }
      }

      int inScope = PageContext.PAGE_SCOPE;
      try {
        if (getScope() != null) {
          inScope = getScope(getScope());
        }
      } catch (final JspException e) {
        getLog().warn("toScope was invalid name " +
                              "so we default to PAGE_SCOPE. " + e);
      }

      pageContext.setAttribute(id, cs, inScope);
    } catch(final Throwable t) {
      getLog().error(t);
      throw new JspTagException("Error: " + t.getMessage());
    }

    return EVAL_PAGE;
  }

  private final static BwLogger logger =
          new BwLogger().setLoggedClass(GetChildrenTag.class);

  private static BwLogger getLog() {
    return logger;
  }
}
