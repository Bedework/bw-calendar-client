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

import javax.servlet.jsp.JspTagException;

/** Base for simple Tags which have a name and scope attribute
 *
 * @author Mike Douglass
 */
public class NameScopeTag extends NameTag {
  /** scope attribute */
  String scope;

  /**
   * @param val
   */
  public void setScope(String val) {
    scope = val;
  }

  /**
   * @return String
   */
  public String getScope() {
    return scope;
  }

  /** Return an object from the property in the default name and scope
   *
   * @param property  String property name or null for whole object
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return Object   null if none found.
   * @throws JspTagException
   */
  protected Object getObject(String property, boolean required)
      throws JspTagException {
    return getObject(name, scope, property, required);
  }

  /** Return an int value from the property in the default name and scope
   *
   * @param property  String property name or null for whole object
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return int      0 if none found.
   * @throws JspTagException
   */
  protected int getInt(String property, boolean required)
      throws JspTagException {
    return getInt(name, scope, property, required);
  }
}
