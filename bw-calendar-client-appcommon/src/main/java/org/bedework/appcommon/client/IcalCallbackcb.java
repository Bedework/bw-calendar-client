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
package org.bedework.appcommon.client;

import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.ifs.IcalCallback;
import org.bedework.util.misc.response.GetEntitiesResponse;
import org.bedework.util.misc.response.GetEntityResponse;
import org.bedework.calfacade.svc.EventInfo;

/**
 * User: douglm Date: 6/28/13 Time: 3:51 PM
 */
public class IcalCallbackcb implements IcalCallback {
  private int strictness = conformanceRelaxed;
  private final Client cl;

  /**
   * @param cl the client
   */
  public IcalCallbackcb(final Client cl) {
    this.cl = cl;
  }

  @Override
  public void setStrictness(final int val) {
    strictness = val;
  }

  @Override
  public int getStrictness() {
    return strictness;
  }

  @Override
  public BwPrincipal getPrincipal() {
    return cl.getCurrentPrincipal();
  }

  @Override
  public BwPrincipal getOwner() {
    return cl.getOwner();
  }

  @Override
  public String getCaladdr(final String val) {
    return cl.uriToCaladdr(val);
  }

  @Override
  public GetEntityResponse<BwCategory> findCategory(final BwString val) {
    return cl.getCategoryByName(val);
  }

  @Override
  public void addCategory(final BwCategory val) {
    cl.addCategory(val);
  }

  @Override
  public GetEntityResponse<BwContact> getContact(final String uid) {
    return cl.getContactByUid(uid);
  }

  @Override
  public GetEntityResponse<BwContact> findContact(final BwString val) {
    return cl.findContact(val);
  }

  @Override
  public void addContact(final BwContact val) {
    cl.addContact(val);
  }

  @Override
  public GetEntityResponse<BwLocation> getLocation(final String uid) {
    return cl.getLocationByUid(uid);
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByKey(
          final String name,
          final String val) {
    return null;
  }

  @Override
  public GetEntityResponse<BwLocation> findLocation(final BwString address) {
    return cl.findLocation(address);
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByCombined(
          final String val, final boolean persisted) {
    return cl.fetchLocationByCombined(val, persisted);
  }

  @Override
  public void addLocation(final BwLocation val) {
    cl.addLocation(val);
  }

  @Override
  public GetEntitiesResponse<EventInfo> getEvent(final String colPath,
                                                 final String guid) {
    return cl.getEventByUid(colPath, guid,
                            null, null);
  }

  @Override
  public boolean getTimezonesByReference() {
    //return cl.getSystemProperties().getTimezonesByReference();
    // We use this for export - should always contain the timezone
    return false;
  }
}

