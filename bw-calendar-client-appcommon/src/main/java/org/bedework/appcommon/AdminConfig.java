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

/**
 * @author douglm
 *
 */
public interface AdminConfig extends ConfigCommon {
  /**
   *
   * @param val true if external registrations allowed
   */
  void setRegistrationsExternal(boolean val);

  /**
   * @return boolean
   */
  boolean getRegistrationsExternal();

  /**
   *
   * @param val true if we clear (some of) the admin form on submit
   */
  void setDefaultClearFormsOnSubmit(boolean val);

  /**
   * @return boolean
   */
  boolean getDefaultClearFormsOnSubmit();

  /**
   *
   * @param val True if categories are optional.
   */
  void setCategoryOptional(boolean val);

  /**
   * @return boolean
   */
  boolean getCategoryOptional();

  /**
   *  @param val True if all categories can be edited.
   */
  void setAllowEditAllCategories(boolean val);

  /**
   * @return boolean
   */
  boolean getAllowEditAllCategories();

  /**
   *  @param val True if all locations can be edited.
   */
  void setAllowEditAllLocations(boolean val);

  /**
   * @return boolean
   */
  boolean getAllowEditAllLocations();

  /**
   *  @param val True if all contacts can be edited.
   */
  void setAllowEditAllContacts(boolean val);

  /**
   * @return boolean
   */
  boolean getAllowEditAllContacts();

  /**
   *  @param val false if we require that all users be in a group
   */
  void setNoGroupAllowed(boolean val);

  /**
   * @return boolean
   */
  boolean getNoGroupAllowed();

  /**
   *  @param val true if all in admin group are approvers
   */
  void setAdminGroupApprovers(boolean val);

  /**
   * @return true if all in admin group are approvers
   */
  boolean getAdminGroupApprovers();

  /**
   *  @param val a prefix to distinguish admin groups from others.
   */
  void setAdminGroupsIdPrefix(String val);

  /**
   * @return boolean
   */
  @MBeanInfo("Prefix for the admin group id")
  String getAdminGroupsIdPrefix();
}
