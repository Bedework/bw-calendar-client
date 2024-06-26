/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: mike Date: 6/25/24 Time: 14:10
 */
public class BwModules implements Serializable {
  public static final String modulesAttrName =
          "org.bedework.client,modules";

  private final Map<String, BwModule> modules = new ConcurrentHashMap<>();

  public BwModule newModule(final String name) {
    return new BwModule(name);
  }

  public void setModule(final String name, final BwModule module) {
    modules.put(name, module);
  }

  public synchronized  BwModule fetchModule(final String name) {
    String n = name;

    if (n == null) {
      n = BwModule.defaultModuleName;
    }

    BwModule m = modules.get(n);

    if (m == null) {
      m = newModule(n);

      /* clone the client from any active module */
      for (final BwModule from: modules.values()) {
        if (from.getClient() != null) {
          m.setClient(from.getClient().copy(m.getModuleName()));
        }
      }
      modules.put(n, m);
    }

    return m;
  }

  /** Called when we change the default client state enough to need
   * to ditch the other clients.
   */
  public void flushModules(final String name) {
    String n = name;

    if (n == null) {
      n = BwModule.defaultModuleName;
    }

    final ArrayList<String> mnames = new ArrayList<>(modules.keySet());

    for (String s: mnames) {
      if (s.equals(n)) {
        continue;
      }

      modules.remove(s);
    }
  }
}
