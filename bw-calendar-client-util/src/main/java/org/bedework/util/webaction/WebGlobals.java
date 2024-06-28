/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.webaction;

import java.util.Locale;

/**
 * User: mike Date: 6/26/24 Time: 23:04
 */
public class WebGlobals {
  // Stored in session
  public final static String webGlobalsAttrName = "web_globals";

  protected Locale currentLocale;

  public void reset(final Request req) {
  }

  public Locale getCurrentLocale() {
    if (currentLocale == null) {
      return Locale.getDefault();
    }
    return currentLocale;
  }
}
