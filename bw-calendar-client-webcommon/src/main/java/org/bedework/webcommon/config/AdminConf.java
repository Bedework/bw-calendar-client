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

/**
 * @author douglm
 *
 */
public class AdminConf extends ClientConf<AdminConfigImpl>
    implements AdminConfMBean {
  public AdminConf(final String serviceName,
                   final ConfigurationStore store,
                   final String configName) {
    super(serviceName, store, configName);
  }

  /* ========================================================================
   * Conf properties
   * ======================================================================== */

  @Override
  public void setRegistrationsExternal(final boolean val) {
    getConfig().setRegistrationsExternal(val);
  }

  @Override
  public boolean getRegistrationsExternal() {
    return getConfig().getRegistrationsExternal();
  }

  @Override
  public void setDefaultClearFormsOnSubmit(final boolean val) {
    getConfig().setDefaultClearFormsOnSubmit(val);
  }

  @Override
  public boolean getDefaultClearFormsOnSubmit() {
    return getConfig().getDefaultClearFormsOnSubmit();
  }

  @Override
  public void setCategoryOptional(final boolean val) {
    getConfig().setCategoryOptional(val);
  }

  @Override
  public boolean getCategoryOptional() {
    return getConfig().getCategoryOptional();
  }

  @Override
  public void setAllowEditAllCategories(final boolean val) {
    getConfig().setAllowEditAllCategories(val);
  }

  @Override
  public boolean getAllowEditAllCategories() {
    return getConfig().getAllowEditAllCategories();
  }

  @Override
  public void setAllowEditAllLocations(final boolean val) {
    getConfig().setAllowEditAllLocations(val);
  }

  @Override
  public boolean getAllowEditAllLocations() {
    return getConfig().getAllowEditAllLocations();
  }

  @Override
  public void setAllowEditAllContacts(final boolean val) {
    getConfig().setAllowEditAllContacts(val);
  }

  @Override
  public boolean getAllowEditAllContacts() {
    return getConfig().getAllowEditAllContacts();
  }

  @Override
  public void setNoGroupAllowed(final boolean val) {
    getConfig().setNoGroupAllowed(val);
  }

  @Override
  public boolean getNoGroupAllowed() {
    return getConfig().getNoGroupAllowed();
  }

  @Override
  public void setAdminGroupApprovers(final boolean val) {
    getConfig().setAdminGroupApprovers(val);
  }

  @Override
  public boolean getAdminGroupApprovers() {
    return getConfig().getAdminGroupApprovers();
  }

  @Override
  public void setAdminGroupsIdPrefix(final String val) {
    getConfig().setAdminGroupsIdPrefix(val);
  }

  @Override
  public String getAdminGroupsIdPrefix() {
    return getConfig().getAdminGroupsIdPrefix();
  }
}
