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
package org.bedework.webcommon;

import org.bedework.calfacade.base.CollatableEntity;
import org.bedework.calfacade.locale.BwLocale;

import java.text.Collator;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;

/**
 * @author douglm
 *
 * @param <T>
 */
public class CollectionCollator <T extends CollatableEntity> {
  private Collator listCollator;
  private Locale loc;

  /**
   * @param vals
   * @return sorted collection
   */
  public Collection<T> getCollatedCollection(final Collection<T> vals) {
    TreeMap<String, T> tm = new TreeMap<String, T>(getListCollator());

    if (vals != null) {
      for (T val: vals) {
        tm.put(val.getCollateValue(), val);
      }
    }

    return tm.values();
  }

  private Collator getListCollator() {
    // Toss it away if locale changed.
    Locale curloc = BwLocale.getLocale();

    if (loc != null) {
      if (!loc.equals(curloc)) {
        listCollator = null;
      }
    }

    if (listCollator == null) {
      listCollator = Collator.getInstance(curloc);
      loc = curloc;
    }

    return listCollator;
  }

}
