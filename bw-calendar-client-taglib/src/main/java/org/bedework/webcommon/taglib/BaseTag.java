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

import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.webcommon.BwRequest;

import org.apache.commons.beanutils.PropertyUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;

/** Base for simple Tags which provides some commonly used methods
 *
 * @author Mike Douglass
 */
public class BaseTag extends TagSupport {
  /** Return a value from the property of the named object in the given
   * (or null for default) scope.
   *
   * @param name      String object name
   * @param scope     String scope name or null for default
   * @param property  String property name or null for whole object
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return Object   null if none found.
   * @throws JspTagException
   */
  protected Object getObject(final String name, 
                             final String scope,
                             final String property,
                             final boolean required)
      throws JspTagException {
    try {
      final Object o = lookup(pageContext, name, property, scope);
      if (o == null) {
        if (required) {
          throw new JspTagException("Unable to find " + name +
                                    " in " + scope + " for property " +
                                    property);
        }

        return null;
      }

      return o;
    } catch (final JspTagException jte) {
      throw jte;
    } catch (final Throwable t) {
      throw new JspTagException(t.getMessage());
    }
  }

  protected Object lookup(final PageContext pageContext,
                          final String name,
                          final String scopeName)
          throws JspException {
    if (scopeName == null) {
      return pageContext.findAttribute(name);
    }

    return pageContext.getAttribute(name, getScope(scopeName));
  }

  protected Object lookup(final PageContext pageContext,
                          final String name,
                          final String property,
                          final String scope) throws JspException {
    // Look up the requested bean, and return if requested
    final Object bean = lookup(pageContext, name, scope);

    if (bean == null) {
      throw new JspException("missing.bean " + name);
    }

    if (property == null) {
      return bean;
    }

    // Locate and return the specified property
    try {
      return PropertyUtils.getProperty(bean, property);
    } catch (final Throwable t) {
      throw new JspException(t);
    }
  }

  protected Client getClient() {
    return (Client)pageContext.getRequest().getAttribute(BwRequest.embeddedClientName);
  }

  protected BwPreferences getPreferences() {
    final HttpSession sess = pageContext.getSession();

    return (BwPreferences)sess.getAttribute(BwRequest.bwPreferencesName);
  }

  /** Return an int value from the property of the named object in the given
   * (or null for default) scope.
   *
   * @param name      String object name
   * @param scope     String scope name or null for default
   * @param property  String property name or null for whole object
   * @param required  boolean true if we should throw an exception if not
   *                  found.
   * @return int      0 if none found.
   * @throws JspTagException
   */
  protected int getInt(final String name, final String scope,
                       final String property, final boolean required)
      throws JspTagException {
    Object o = getObject(name, scope, property, required);

    if (!(o instanceof Integer)) {
      throw new JspTagException("Object " + name +
                                " in " + scope + " for property " +
                                property +
                                " is not an Integer");
    }

    return ((Integer)o).intValue();
  }

  /** Return the int scopeindex given a scope name
   *
   * @param scopeName   String - null, "page", "request", "session", "application"
   * @return int        value defined in PageContext
   * @throws JspTagException for invalid scope
   */
  protected int getScope(final String scopeName) throws JspTagException {
    if (scopeName == null) {
      return PageContext.PAGE_SCOPE;
    }

    if (scopeName.equals("page")) {
      return PageContext.PAGE_SCOPE;
    }

    if (scopeName.equals("request")) {
      return PageContext.REQUEST_SCOPE;
    }

    if (scopeName.equals("session")) {
      return PageContext.SESSION_SCOPE;
    }

    if (scopeName.equals("application")) {
      return PageContext.APPLICATION_SCOPE;
    }

    throw new JspTagException("Invalid scope name " + scopeName);
  }
}
