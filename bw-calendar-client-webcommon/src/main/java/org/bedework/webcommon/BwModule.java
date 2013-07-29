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

import org.bedework.appcommon.client.Client;

import java.io.Serializable;

/** A module represents a client and its associated state. A module
 * subclass may have fields visible to jsp.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class BwModule implements Serializable {
  private String moduleName;
  private Client cl;

  public BwModule(String moduleName,
                  Client cl) {
    this.moduleName = moduleName;
    this.cl = cl;
  }

  /**
   * @param val
   */
  public void assignModuleName(final String val) {
    moduleName = val;
  }

  /**
   * @return String
   */
  public String fetchModuleName() {
    return moduleName;
  }

  /** Avoid any access via requests
   *
   * @param val
   */
  public void assignClient(Client val) {
    cl = val;
  }

  /**
   *
   * @return the client object
   */
  public Client fetchClient() {
    return cl;
  }


}

