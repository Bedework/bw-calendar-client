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
package org.bedework.appcommon;

import org.bedework.util.jmx.MBeanInfo;

import java.io.Serializable;

/** Common configuration properties for the web clients
 *
 * @author Mike Douglass
 * @version 1.0
 */
public interface ConfigCommon extends Serializable {
  /** Mbean class name
  *
  * @param val    String
  */
 void setMbeanClassName(final String val);

 /** Class name
  *
  * @return String
  */
 @MBeanInfo("The mbean class.")
 String getMbeanClassName();

 /**
   * @param val
   */
  void setLogPrefix(String val);

  /**
   * @return String
   */
  String getLogPrefix();

  /**
   * @param val
   */
  void setAppType(String val);

  /**
   * @return String
   */
  String getAppType();

  /** True for a public admin client.
   *
   * @param val
   */
  void setPublicAdmin(boolean val);

  /**
   * @return boolean
   */
  boolean getPublicAdmin();

  /** True for a guest mode (non-auth) client.
   *
   * @param val
   */
  void setGuestMode(boolean val);

  /**
   * @return boolean
   */
  boolean getGuestMode();

  /** publicAdminUri.
   *
   * @param val
   */
  void setPublicAdminUri(String val);

  /**
   * @return String
   */
  String getPublicAdminUri();

  /** publicAdminUri.
   *
   * @param val
   */
  void setPublicCalendarUri(String val);

  /**
   * @return String
   */
  String getPublicCalendarUri();

  /** publicAdminUri.
   *
   * @param val
   */
  void setPersonalCalendarUri(String val);

  /**
   * @return String
   */
  String getPersonalCalendarUri();

  /** True if we should auto-create contacts. Some sites may wish to control
   * the creation of contacts to enforce consistency in their use. If this
   * is true we create a contact as we create events. If false the contact
   * must already exist.
   *
   * @param val
   */
  void setAutoCreateContacts(boolean val);

  /**
   * @return boolean
   */
  boolean getAutoCreateContacts();

  /** True if we should auto-create locations. Some sites may wish to control
   * the creation of locations to enforce consistency in their use. If this
   * is true we create a location as we create events. If false the location
   * must already exist.
   *
   * @param val
   */
  void setAutoCreateLocations(boolean val);

  /**
   * @return boolean
   */
  boolean getAutoCreateLocations();

  /** True if we should auto-delete contacts. Some sites may wish to control
   * the deletion of contacts to enforce consistency in their use. If this
   * is true we delete a contact when it becomes unused.
   *
   * @param val
   */
  void setAutoDeleteContacts(boolean val);

  /**
   * @return boolean
   */
  boolean getAutoDeleteContacts();

  /** True if we should auto-delete locations. Some sites may wish to control
   * the deletion of locations to enforce consistency in their use. If this
   * is true we delete a location when it becomes unused.
   *
   * @param val
   */
  void setAutoDeleteLocations(boolean val);

  /**
   * @return boolean
   */
  boolean getAutoDeleteLocations();

  /**
   * @param val
   */
  void setHour24(boolean val);

  /**
   * @return bool
   */
  boolean getHour24();

  /**
   * @param val
   */
  void setMinIncrement(int val);

  /**
   * @return int
   */
  int getMinIncrement();

  /** True if we show data on year viewws.
   *
   * @param val
   */
  void setShowYearData(boolean val);

  /**
   * @return boolean
   */
  boolean getShowYearData();

  /** Where the browser finds css and other resources.
   *
   * @param val
   */
  void setBrowserResourceRoot(String val);

  /**
   * @return String
   */
  String getBrowserResourceRoot();

  /** Where the xslt and resources are based.
   *
   * @param val
   */
  void setAppRoot(String val);

  /**
   * @return String
   */
  String getAppRoot();

  /**
   * @param val
   */
  void setRefreshAction(String val);

  /**
   * @return String
   */
  String getRefreshAction();

  /**
   * @param val
   */
  void setRefreshInterval(int val);

  /**
   * @return int
   */
  int getRefreshInterval();

  /**
   * @param val
   */
  void setCalSuite(String val);

  /**
   * @return String
   */
  String getCalSuite();

  /** The root of the calendars used for submission of public events by users.
   *
   * @param val
   */
  void setSubmissionRoot(String val);

  /**
   * @return String
   */
  String getSubmissionRoot();

  /** portalPlatform.
   *
   * @param val
   */
  void setPortalPlatform(String val);

  /**
   * @return String
   */
  String getPortalPlatform();
}
