/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.webcommon.BwModule;
import org.bedework.webcommon.BwModules;

/**
 * User: mike Date: 6/26/24 Time: 00:06
 */
public class BwRWModules extends BwModules {
  public BwModule newModule(final String name) {
    return new RwBwModule(name);
  }
}
