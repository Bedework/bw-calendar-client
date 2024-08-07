/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.webaction;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.servlet.HttpServletUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * User: mike Date: 6/26/24 Time: 23:04
 */
public class WebGlobals implements Logged, Serializable {
  // Stored in session
  public final static String webGlobalsAttrName = "web_globals";

  /**
   * The current authenticated user. May be null
   */
  protected String currentUser;

  protected Locale currentLocale;

  public void reset(final Request req) {
    if (getCurrentUser() == null) {
      currentUser = HttpServletUtils.remoteUser(req.getRequest());
    } // Otherwise we check it later in checklogout.
  }

  /**
   *
   * @return current authenticated user or null
   */
  public String getCurrentUser() {
    return currentUser;
  }

  public Locale getCurrentLocale() {
    if (currentLocale == null) {
      return Locale.getDefault();
    }
    return currentLocale;
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
