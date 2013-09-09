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

import org.bedework.util.servlet.filters.PresentationState;

import java.io.Serializable;

/** This interface represents a session for the Bedework web interface.
 * Some user state will be retained here.
 * We also provide a number of methods which act as the interface between
 * the web world and the calendar world.
 *
 * @author Mike Douglass   douglm@bedework.edu
 */
public interface BwSession extends Serializable {
  /** ===================================================================
   *                     Property methods
   *  =================================================================== */

  /** This may not be entirely correct so should be used with care.
   * Really just provides some measure of use.
   *
   * @return long session number
   */
  public long getSessionNum();

  /** The current user
   *
   * @param val   String user
   */
  public void setUser(String val);

  /**
   * @return String
   */
  public String getUser();

  /** Is this a guest user?
   *
   * @return boolean true for a guest
   */
  public boolean isGuest();

  /** The PresentationState object defines how the external information is
      presented, usually through some sort of XML/XSLT filtering
   *
   * @param val
   */
  public void setPresentationState(PresentationState val);

  /**
   * @return PresentationState
   */
  public PresentationState getPresentationState();
}

