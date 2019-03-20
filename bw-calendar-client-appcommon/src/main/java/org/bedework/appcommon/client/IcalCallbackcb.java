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
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.responses.GetEntityResponse;
import org.bedework.icalendar.IcalCallback;

import java.util.Collection;

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
  public void setStrictness(final int val) throws CalFacadeException {
    strictness = val;
  }

  @Override
  public int getStrictness() throws CalFacadeException {
    return strictness;
  }

  @Override
  public BwPrincipal getPrincipal() throws CalFacadeException {
    return cl.getCurrentPrincipal();
  }

  @Override
  public BwPrincipal getOwner() throws CalFacadeException {
    return cl.getOwner();
  }

  @Override
  public String getCaladdr(final String val) throws CalFacadeException {
    return cl.uriToCaladdr(val);
  }

  @Override
  public BwCategory findCategory(final BwString val) throws CalFacadeException {
    return cl.getCategoryByName(val);
  }

  @Override
  public void addCategory(final BwCategory val) throws CalFacadeException {
    cl.addCategory(val);
  }

  @Override
  public BwContact getContact(final String uid) throws CalFacadeException {
    return cl.getContactByUid(uid);
  }

  @Override
  public BwContact findContact(final BwString val) throws CalFacadeException {
    return cl.findContact(val);
  }

  @Override
  public void addContact(final BwContact val) throws CalFacadeException {
    cl.addContact(val);
  }

  @Override
  public BwLocation getLocation(final String uid) throws CalFacadeException {
    return cl.getLocationByUid(uid);
  }

  @Override
  public BwLocation getLocation(final BwString address) throws CalFacadeException {
    return cl.findLocation(address);
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByKey(
          final String name,
          final String val) {
    return null;
  }

  @Override
  public BwLocation findLocation(final BwString address) throws CalFacadeException {
    return cl.findLocation(address);
  }

  @Override
  public GetEntityResponse<BwLocation> fetchLocationByCombined(
          final String val, final boolean persisted) {
    return cl.fetchLocationByCombined(val, persisted);
  }

  @Override
  public void addLocation(final BwLocation val) throws CalFacadeException {
    cl.addLocation(val);
  }

  @Override
  public Collection getEvent(final String colPath,
                             final String guid)
          throws CalFacadeException {
    return cl.getEventByUid(colPath, guid,
                            null, null);
  }

  @Override
  public boolean getTimezonesByReference() throws CalFacadeException {
    //return cl.getSystemProperties().getTimezonesByReference();
    // We use this for export - should always contain the timezone
    return false;
  }
}

