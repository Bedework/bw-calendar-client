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
package org.bedework.portal.liferay;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Implementation of <b>HttpServletResponseWrapper</b> that works with
 * uPortal 2.5.
 *
 * @author Satish Sekharan
 * @version 1.0
 */
public class PortletResponseWrapper extends HttpServletResponseWrapper {

  /** Constructor
   *
   * @param response HttpServletResponse
   */
  public PortletResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return getResponse().getOutputStream();
  }

  public PrintWriter getWriter() throws IOException {
    return (new PrintWriter(new OutputStreamWriter(getOutputStream(),
        getCharacterEncoding()), true));
  }

  public String encodeUrl(String path) {
    return super.encodeUrl(path);
  }
}
