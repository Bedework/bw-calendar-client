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

package org.bedework.webcommon;

import org.bedework.util.servlet.filters.ConfiguredXSLTFilter;

import jakarta.servlet.http.HttpServletRequest;

/** This is an XSLT filter which overrides the global fetch/update to
 * obtain them from the request.
 *
 * <p>The application needs to create and implant them in the request</p>
 *
 * @author Mike Douglass douglm rpi.edu
 */
public class ModuleXsltFilter extends ConfiguredXSLTFilter {
  public static final String globalsName =
          "org.bedework.util.servlet.filter.AbstractFilter.FilterGlobals";

  @Override
  public FilterGlobals getGlobals(final HttpServletRequest req) {
    final Object o = req.getAttribute(globalsName);
    final FilterGlobals fg;

    if (o == null) {
      fg = newFilterGlobals();
      req.setAttribute(globalsName, fg);

      if (debug()) {
        debug("Created new FilterGlobals");
      }
    } else {
      fg = (FilterGlobals)o;
    }

    return fg;
  }
}


