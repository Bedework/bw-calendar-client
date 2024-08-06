/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.filter.BwCollectionFilter;
import org.bedework.client.rw.InOutBoxInfo;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwModule;
import org.bedework.webcommon.BwRequest;

import static org.bedework.caldav.util.filter.FilterBase.addAndChild;

/**
 * User: mike Date: 3/19/21 Time: 21:35
 */
public class RwBwModule extends BwModule {
  public RwBwModule(final String moduleName) {
    super(moduleName);
  }

  @Override
  protected void checkMessaging(final BwRequest req) {
    final RWClient cl = (RWClient)req.getClient();
    final var globals = (BwRWWebGlobals)req.getGlobals();

    if (!cl.getPublicAdmin()) {
      InOutBoxInfo ib = globals.getInBoxInfo();
      if (ib == null) {
        ib = new InOutBoxInfo(cl, true);
        globals.setInBoxInfo(ib);
      } else {
        ib.refresh(cl, false);
      }
    }

    if (shouldCheckNotifications(req)) {
      NotificationInfo ni = globals.getNotificationInfo();
      if (ni == null) {
        ni = new NotificationInfo();
        globals.setNotificationInfo(ni);
      }

      ni.refresh(cl, false);
    }
  }

  protected boolean shouldCheckNotifications(final BwRequest req) {
    return true;
  }

  @Override
  public FilterBase defaultSearchFilter(final BwRequest req) {
    final RWClient cl = (RWClient)req.getClient();
    final var globals = (BwRWWebGlobals)req.getGlobals();

    FilterBase filter = null;

    if (cl.getWebUser()) {
      final var inBoxInfo = globals.getInBoxInfo();
      if ((inBoxInfo != null) && (inBoxInfo.getColPath() != null)) {
        filter = new BwCollectionFilter(null,
                                        inBoxInfo.getColPath());
        filter.setNot(true);
      }

      final BwCalendar home;
      try {
        home = cl.getHome();
      } catch (final CalFacadeException cfe) {
        throw new RuntimeException(cfe);
      }

      final BwFilterDef fd = new BwFilterDef();
      fd.setDefinition("vpath='" + home.getPath() + "')");
      final var pr = cl.parseFilter(fd);

      filter = addAndChild(filter, pr.filter);
    }

    return filter;
  }
}
