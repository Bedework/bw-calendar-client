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
package org.bedework.client.web.rw;

import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventProxy;

import java.io.Serializable;
import java.util.Collection;

/** Result of adding an event.
 *
 * @author Mike Douglass douglm - rpi.edu
 */
public class AddEventResult implements Serializable {
  private BwEvent event;

  private Collection<BwEventProxy> failedOverrides;

  /** Constructor
   *
   * @param event
   * @param failedOverrides
   */
  public AddEventResult(final BwEvent event,
                        final Collection<BwEventProxy> failedOverrides) {
    this.event = event;
    this.failedOverrides = failedOverrides;
  }

  /**
   * @param val
   */
  public void setEvent(final BwEvent val) {
    event = val;
  }

  /**
   * @return event
   */
  public BwEvent getEvent() {
    return event;
  }

  /**
   * @param val
   */
  public void setFailedOverrides(final Collection<BwEventProxy> val) {
    failedOverrides = val;
  }

  /**
   * @return failed overrides from add of event.
   */
  public Collection<BwEventProxy> getFailedOverrides() {
    return failedOverrides;
  }
}
