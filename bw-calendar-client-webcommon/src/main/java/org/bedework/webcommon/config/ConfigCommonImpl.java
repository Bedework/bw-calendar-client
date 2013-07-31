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
package org.bedework.webcommon.config;

import org.bedework.appcommon.ConfigCommon;

import edu.rpi.cmt.config.ConfInfo;
import edu.rpi.cmt.config.ConfigBase;

/** Common configuration properties for the web clients
 *
 * @author Mike Douglass
 * @version 1.0
 */
@ConfInfo(elementName = "client-config")
public class ConfigCommonImpl extends ConfigBase<ConfigCommonImpl>
        implements ConfigCommon {
  private String mbeanClassName;

  private String logPrefix;

  private String appType;

  private boolean publicAdmin;

  private boolean guestMode;

  private String publicAdminUri;
  private String publicCalendarUri;
  private String personalCalendarUri;

  private boolean autoCreateContacts = true;

  private boolean autoCreateLocations = true;

  private boolean autoDeleteContacts = true;

  private boolean autoDeleteLocations = true;

  private boolean hour24;

  private int minIncrement;

  private boolean showYearData;

  private String browserResourceRoot;

  private String appRoot;

  private String refreshAction;

  private int refreshInterval;

  private String calSuite;

  private String submissionRoot;

  private String portalPlatform;

  @Override
  public void setMbeanClassName(final String val) {
    mbeanClassName = val;
  }

  @Override
  public String getMbeanClassName() {
    return mbeanClassName;
  }

  @Override
  public void setLogPrefix(final String val) {
    logPrefix = val;
  }

  @Override
  public String getLogPrefix() {
    return logPrefix;
  }

  @Override
  public void setAppType(final String val) {
    appType = val;
  }

  @Override
  public String getAppType() {
    return appType;
  }

  @Override
  public void setPublicAdmin(final boolean val) {
    publicAdmin = val;
  }

  @Override
  public boolean getPublicAdmin() {
    return publicAdmin;
  }

  @Override
  public void setGuestMode(final boolean val) {
    guestMode = val;
  }

  @Override
  public boolean getGuestMode() {
    return guestMode;
  }

  @Override
  public void setPublicAdminUri(final String val) {
    publicAdminUri = val;
  }

  @Override
  public String getPublicAdminUri() {
    return publicAdminUri;
  }

  @Override
  public void setPublicCalendarUri(final String val) {
    publicCalendarUri = val;
  }

  @Override
  public String getPublicCalendarUri() {
    return publicCalendarUri;
  }

  @Override
  public void setPersonalCalendarUri(final String val) {
    personalCalendarUri = val;
  }

  @Override
  public String getPersonalCalendarUri() {
    return personalCalendarUri;
  }

  @Override
  public void setAutoCreateContacts(final boolean val) {
    autoCreateContacts = val;
  }

  @Override
  public boolean getAutoCreateContacts() {
    return autoCreateContacts;
  }

  @Override
  public void setAutoCreateLocations(final boolean val) {
    autoCreateLocations = val;
  }

  @Override
  public boolean getAutoCreateLocations() {
    return autoCreateLocations;
  }

  @Override
  public void setAutoDeleteContacts(final boolean val) {
    autoDeleteContacts = val;
  }

  @Override
  public boolean getAutoDeleteContacts() {
    return autoDeleteContacts;
  }

  @Override
  public void setAutoDeleteLocations(final boolean val) {
    autoDeleteLocations = val;
  }

  @Override
  public boolean getAutoDeleteLocations() {
    return autoDeleteLocations;
  }

  @Override
  public void setHour24(final boolean val) {
    hour24 = val;
  }

  @Override
  public boolean getHour24() {
    return hour24;
  }

  @Override
  public void setMinIncrement(final int val) {
    minIncrement = val;
  }

  @Override
  public int getMinIncrement() {
    return minIncrement;
  }

  @Override
  public void setShowYearData(final boolean val) {
    showYearData = val;
  }

  @Override
  public boolean getShowYearData() {
    return showYearData;
  }

  @Override
  public void setBrowserResourceRoot(final String val) {
    browserResourceRoot = val;
  }

  @Override
  public String getBrowserResourceRoot() {
    return browserResourceRoot;
  }

  @Override
  public void setAppRoot(final String val) {
    appRoot = val;
  }

  @Override
  public String getAppRoot() {
    return appRoot;
  }

  @Override
  public void setRefreshAction(final String val) {
    refreshAction = val;
  }

  @Override
  public String getRefreshAction() {
    return refreshAction;
  }

  @Override
  public void setRefreshInterval(final int val) {
    refreshInterval = val;
  }

  @Override
  public int getRefreshInterval() {
    return refreshInterval;
  }

  @Override
  public void setCalSuite(final String val) {
    calSuite = val;
  }

  @Override
  public String getCalSuite() {
    return calSuite;
  }

  @Override
  public void setSubmissionRoot(final String val) {
    submissionRoot = val;
  }

  @Override
  public String getSubmissionRoot() {
    return submissionRoot;
  }

  @Override
  public void setPortalPlatform(final String val) {
    portalPlatform = val;
  }

  @Override
  public String getPortalPlatform() {
    return portalPlatform;
  }

  /** Copy this object to val.
   *
   * @param val
   */
  public void copyTo(final ConfigCommonImpl val) {
    val.setLogPrefix(getLogPrefix());
    val.setAppType(getAppType());
    val.setPublicAdmin(getPublicAdmin());
    val.setGuestMode(getGuestMode());
    val.setPublicAdminUri(getPublicAdminUri());
    val.setPublicCalendarUri(getPublicCalendarUri());
    val.setPersonalCalendarUri(getPersonalCalendarUri());
    val.setAutoCreateContacts(getAutoCreateContacts());
    val.setAutoCreateLocations(getAutoCreateLocations());
    val.setAutoDeleteContacts(getAutoDeleteContacts());
    val.setAutoDeleteLocations(getAutoDeleteLocations());
    val.setHour24(getHour24());
    val.setMinIncrement(getMinIncrement());
    val.setShowYearData(getShowYearData());
    val.setBrowserResourceRoot(getBrowserResourceRoot());
    val.setAppRoot(getAppRoot());
    val.setRefreshAction(getRefreshAction());
    val.setRefreshInterval(getRefreshInterval());
    val.setCalSuite(getCalSuite());
    val.setSubmissionRoot(getSubmissionRoot());
    val.setPortalPlatform(getPortalPlatform());
  }

  @Override
  public Object clone() {
    ConfigCommonImpl conf = new ConfigCommonImpl();

    copyTo(conf);

    return conf;
  }
}
