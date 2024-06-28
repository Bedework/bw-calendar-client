/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.webcommon;

import org.bedework.calfacade.locale.BwLocale;
import org.bedework.util.misc.Util;
import org.bedework.util.webaction.WebGlobals;

import java.util.Collection;
import java.util.Locale;

/**
 * User: mike Date: 6/26/24 Time: 22:53
 */
public class BwWebGlobals extends WebGlobals {
  private Locale requestedLocale;

  public void reset(final BwRequest req) {
    super.reset(req);
    setLocale(req);
  }

  private void setLocale(final BwRequest request) {
    final BwModuleState mstate = request.getModule().getState();
    final Collection<Locale> reqLocales = request.getLocales();
    final String reqLoc = request.getReqPar("locale");

    if (reqLoc != null) {
      if ("default".equals(reqLoc)) {
        requestedLocale = null;
      } else {
        try {
          requestedLocale = Util.makeLocale(reqLoc); // Make it stick
        } catch (final Throwable t) {
          // Ignore bad parameter?
        }
      }
    }

    final Locale loc =
            request.getClient().getUserLocale(reqLocales,
                                              getRequestedLocale());

    if (loc != null) {
      BwLocale.setLocale(loc);
      final Locale cloc = getCurrentLocale();
      if ((cloc == null) || (!cloc.equals(loc))) {
        mstate.setRefresh(true);
      }
      currentLocale = loc;
    }
  }

  /**
   * @return locale or null
   */
  public Locale getRequestedLocale() {
    return requestedLocale;
  }
}
