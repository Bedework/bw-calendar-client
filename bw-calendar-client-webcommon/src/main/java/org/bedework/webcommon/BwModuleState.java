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

import edu.rpi.sss.util.servlets.PresentationState;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** This class will be exposed to JSP via the request. Do not expose the
 * client indirectly through this.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class BwModuleState implements Serializable {
  private String moduleName;

  private PresentationState ps;

  private static final int maxAppVars = 50; // Stop screwing around.

  Map<String, String> vars = new HashMap<>();

  public BwModuleState(String moduleName) {
    this.moduleName = moduleName;
  }

  /**
   * @param val PresentationState
   */
  public void setPresentationState(PresentationState val) {
    ps = val;
  }

  /**
   * @return PresentationState
   */
  public PresentationState getPresentationState() {
    return ps;
  }

  /**
   * @return String
   */
  public String getModuleName() {
    return moduleName;
  }

  /** Called to set a variable to a value
   *
   * @param   name     name of variable
   * @param   val      new value of variable - null means remove.
   * @return  boolean  True if ok - false for too many vars
   */
  public boolean setVar(final String name, final String val) {
    if (val == null) {
      vars.remove(name);
      return true;
    }

    if (vars.size() > maxAppVars) {
      return false;
    }

    vars.put(name, val);
    return true;
  }

  /** Called to return a variable
   *
   * @param   name     name of variable
   * @return  value or null
   */
  public String getVar(final String name) {
    return vars.get(name);
  }
}

