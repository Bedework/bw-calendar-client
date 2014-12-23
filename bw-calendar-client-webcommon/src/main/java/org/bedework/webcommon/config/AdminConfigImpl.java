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

import org.bedework.appcommon.AdminConfig;
import org.bedework.util.misc.ToString;

/**
 * @author douglm
 *
 */
public class AdminConfigImpl extends ConfigCommonImpl implements
        AdminConfig {
  /* True if categories are optional.
   */
  private boolean categoryOptional = true;

  private boolean allowEditAllCategories;
  private boolean allowEditAllLocations;
  private boolean allowEditAllContacts;

  private boolean noGroupAllowed;

  private String adminGroupsIdPrefix;

  @Override
  public void setCategoryOptional(final boolean val) {
    categoryOptional = val;
  }

  @Override
  public boolean getCategoryOptional() {
    return categoryOptional;
  }

  @Override
  public void setAllowEditAllCategories(final boolean val) {
    allowEditAllCategories = val;
  }

  @Override
  public boolean getAllowEditAllCategories() {
    return allowEditAllCategories;
  }

  @Override
  public void setAllowEditAllLocations(final boolean val) {
    allowEditAllLocations = val;
  }

  @Override
  public boolean getAllowEditAllLocations() {
    return allowEditAllLocations;
  }

  @Override
  public void setAllowEditAllContacts(final boolean val) {
    allowEditAllContacts = val;
  }

  @Override
  public boolean getAllowEditAllContacts() {
    return allowEditAllContacts;
  }

  @Override
  public void setNoGroupAllowed(final boolean val) {
    noGroupAllowed = val;
  }

  @Override
  public boolean getNoGroupAllowed() {
    return noGroupAllowed;
  }

  @Override
  public void setAdminGroupsIdPrefix(final String val) {
    adminGroupsIdPrefix = val;
  }

  @Override
  public String getAdminGroupsIdPrefix() {
    return adminGroupsIdPrefix;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("adminGroupsIdPrefix", getAdminGroupsIdPrefix());

    return ts.toString();
  }

  @Override
  public Object clone() {
    final AdminConfigImpl conf = new AdminConfigImpl();

    copyTo(conf);

    conf.setCategoryOptional(getCategoryOptional());
    conf.setAllowEditAllCategories(getAllowEditAllCategories());
    conf.setAllowEditAllLocations(getAllowEditAllLocations());
    conf.setAllowEditAllContacts(getAllowEditAllContacts());

    conf.setNoGroupAllowed(getNoGroupAllowed());
    conf.setAdminGroupsIdPrefix(getAdminGroupsIdPrefix());

    return conf;
  }
}
