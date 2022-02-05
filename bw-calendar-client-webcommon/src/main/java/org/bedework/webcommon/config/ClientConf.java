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

import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;

/**
 * @author douglm
 *
 * @param <X>
 */
public class ClientConf<X extends ConfigCommonImpl> extends ConfBase<X>
    implements ClientConfMBean {
  /**
   * @param configStore
   * @param serviceName
   * @param cfg - the configuration
   */
  public void init(final ConfigurationStore configStore,
                   final String serviceName,
                   final X cfg) {
    setServiceName(serviceName);
    setStore(configStore);
    setConfigName(cfg.getName());

    this.cfg = cfg;
  }

  /* ========================================================================
   * Conf properties
   * ======================================================================== */

  @Override
  public void setMbeanClassName(final String val) {
    getConfig().setMbeanClassName(val);
  }

  @Override
  public String getMbeanClassName() {
    if (getConfig().getMbeanClassName() == null) {
      return this.getClass().getCanonicalName();
    }

    return getConfig().getMbeanClassName();
  }

  @Override
  public void setLogPrefix(final String val) {
    getConfig().setLogPrefix(val);
  }

  @Override
  public String getLogPrefix() {
    return getConfig().getLogPrefix();
  }

  @Override
  public void setAppType(final String val) {
    getConfig().setAppType(val);
  }

  @Override
  public String getAppType() {
    return getConfig().getAppType();
  }

  @Override
  public void setPublicAdmin(final boolean val) {
    getConfig().setPublicAdmin(val);
  }

  @Override
  public boolean getPublicAdmin() {
    return getConfig().getPublicAdmin();
  }

  @Override
  public void setReadWrite(final boolean val) {
    getConfig().setReadWrite(val);
  }

  @Override
  public boolean getReadWrite() {
    return getConfig().getReadWrite();
  }

  @Override
  public void setGuestMode(final boolean val) {
    getConfig().setGuestMode(val);
  }

  @Override
  public boolean getGuestMode() {
    return getConfig().getGuestMode();
  }

  @Override
  public void setPublicAdminUri(final String val) {
    getConfig().setPublicAdminUri(val);
  }

  @Override
  public String getPublicAdminUri() {
    return getConfig().getPublicAdminUri();
  }

  @Override
  public void setPublicCalendarUri(final String val) {
    getConfig().setPublicCalendarUri(val);
  }

  @Override
  public String getPublicCalendarUri() {
    return getConfig().getPublicCalendarUri();
  }

  @Override
  public void setPersonalCalendarUri(final String val) {
    getConfig().setPersonalCalendarUri(val);
  }

  @Override
  public String getPersonalCalendarUri() {
    return getConfig().getPersonalCalendarUri();
  }

  @Override
  public void setAutoCreateContacts(final boolean val) {
    getConfig().setAutoCreateContacts(val);
  }

  @Override
  public boolean getAutoCreateContacts() {
    return getConfig().getAutoCreateContacts();
  }

  @Override
  public void setAutoCreateLocations(final boolean val) {
    getConfig().setAutoCreateLocations(val);
  }

  @Override
  public boolean getAutoCreateLocations() {
    return getConfig().getAutoCreateLocations();
  }

  @Override
  public void setAutoDeleteContacts(final boolean val) {
    getConfig().setAutoDeleteContacts(val);
  }

  @Override
  public boolean getAutoDeleteContacts() {
    return getConfig().getAutoDeleteContacts();
  }

  @Override
  public void setAutoDeleteLocations(final boolean val) {
    getConfig().setAutoDeleteLocations(val);
  }

  @Override
  public boolean getAutoDeleteLocations() {
    return getConfig().getAutoDeleteLocations();
  }

  @Override
  public void setHour24(final boolean val) {
    getConfig().setHour24(val);
  }

  @Override
  public boolean getHour24() {
    return getConfig().getHour24();
  }

  @Override
  public void setMinIncrement(final int val) {
    getConfig().setMinIncrement(val);
  }

  @Override
  public int getMinIncrement() {
    return getConfig().getMinIncrement();
  }

  @Override
  public void setShowYearData(final boolean val) {
    getConfig().setShowYearData(val);
  }

  @Override
  public boolean getShowYearData() {
    return getConfig().getShowYearData();
  }

  @Override
  public void setBrowserResourceRoot(final String val) {
    getConfig().setBrowserResourceRoot(val);
  }

  @Override
  public String getBrowserResourceRoot() {
    return getConfig().getBrowserResourceRoot();
  }

  @Override
  public void setAppRoot(final String val) {
    getConfig().setAppRoot(val);
  }

  @Override
  public String getAppRoot() {
    return getConfig().getAppRoot();
  }

  @Override
  public void setRefreshAction(final String val) {
    getConfig().setRefreshAction(val);
  }

  @Override
  public String getRefreshAction() {
    return getConfig().getRefreshAction();
  }

  @Override
  public void setRefreshInterval(final int val) {
    getConfig().setRefreshInterval(val);
  }

  @Override
  public int getRefreshInterval() {
    return getConfig().getRefreshInterval();
  }

  @Override
  public void setCalSuite(final String val) {
    getConfig().setCalSuite(val);
  }

  @Override
  public String getCalSuite() {
    return getConfig().getCalSuite();
  }

  @Override
  public void setSubmissionRoot(final String val) {
    getConfig().setSubmissionRoot(val);
  }

  @Override
  public String getSubmissionRoot() {
    return getConfig().getSubmissionRoot();
  }

  /*
  @Override
  public void setPortalPlatform(final String val) {
    getConfig().setPortalPlatform(val);
  }

  @Override
  public String getPortalPlatform() {
    return getConfig().getPortalPlatform();
  }*/

  @Override
  public String loadConfig() {
    return null;
  }
}
