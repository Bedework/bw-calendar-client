/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.admin;

import org.bedework.webcommon.BwModule;
import org.bedework.webcommon.BwModules;

/**
 * User: mike Date: 6/26/24 Time: 00:08
 */
public class BwAdminModules extends BwModules {
  public BwModule newModule(final String name) {
    return new AdminBwModule(name);
  }
}
