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

import org.bedework.appcommon.client.Client;
import org.bedework.appcommon.client.IcalCallbackcb;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.icalendar.IcalTranslator;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** Class to provide a Collection of formatted BwEvent.
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class FormattedEvents extends AbstractCollection<EventFormatter> {
  private Collection<EventInfo> events;
  private Client cl;
  private IcalTranslator trans;

  /** Constructor
   *
   * @param cl - client object
   * @param events
   */
  public FormattedEvents(final Client cl,
                         final Collection<EventInfo> events) {
    if (events == null) {
      this.events = new ArrayList<EventInfo>();
    } else {
      this.events = events;
    }
    this.cl = cl;
    trans = new IcalTranslator(new IcalCallbackcb(cl));
  }

  @Override
  public Iterator<EventFormatter> iterator() {
    return new FormattedEventsIterator(events.iterator());
  }

  @Override
  public int size() {
    return events.size();
  }

  private class FormattedEventsIterator implements Iterator<EventFormatter> {
    private Iterator<EventInfo> it;

    private FormattedEventsIterator(final Iterator<EventInfo> it) {
      this.it = it;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public EventFormatter next() {
      EventInfo ev = it.next();

      return new EventFormatter(cl, trans, ev);
    }

    @Override
    public void remove() {
      throw new RuntimeException("Iterator is read-only");
    }
  }
}

