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

import org.bedework.calfacade.BwLocation;
import org.bedework.util.misc.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** Parameters for fetching a bunch of events.
 *
 * @author Mike Douglass douglm - rpi.edu
 */
public class Locations implements Serializable {
  private Collection<BwLocation> locations;
  private List<String> preferred;

  /**
   *
   * @param val collection of locations
   */
  public void setLocations(final Collection<BwLocation> val) {
    locations = val;
  }

  /**
   * @return collection of locations
   */
  public Collection<BwLocation> getLocations() {
    return locations;
  }

  /**
   * @param val list of hrefs
   */
  public void setPreferred(final List<String> val) {
    preferred = val;
  }

  /**
   * @return list of hrefs
   */
  public List<String> getPreferred() {
    return preferred;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("locations", getLocations())
            .append("preferred", getPreferred());

    return ts.toString();
  }
}
