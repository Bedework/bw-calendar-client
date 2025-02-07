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

import jakarta.servlet.jsp.JspTagException;

/** Base for simple Tags which have a name and scope attribute.
 * The getXXX methods here are for those attributes which can be identified
 * the the "name", "scope" and "property" parameters.
 *
 * @author Mike Douglass
 */
public class NameScopePropertyTag extends NameScopeTag {
  /** property attribute */
  String property;

  /**
   * @param val property value
   */
  public void setProperty(final String val) {
    property = val;
  }

  /**
   * @return String
   */
  public String getProperty() {
    return property;
  }

  /** Return an object from the default name, scope and property.
   *
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return Object   null if none found.
   * @throws JspTagException
   */
  protected Object getObject(final boolean required) throws JspTagException {
    return getObject(name, scope, property, required);
  }

  /** Return a String from the default name, scope and property.
   *
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return String   null if none found.
   * @throws JspTagException if required and absent
   */
  protected String getString(final boolean required) throws JspTagException {
    final Object o = getObject(required);
    if (o == null) {
      return null;
    }
    return String.valueOf(o);
  }
}
