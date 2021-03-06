/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.client.web.rw;

import org.bedework.client.rw.InOutBoxInfo;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.filter.BwCollectionFilter;
import org.bedework.client.rw.NotificationInfo;
import org.bedework.client.rw.RWClient;
import org.bedework.webcommon.BwModule;
import org.bedework.webcommon.BwRequest;

/**
 * User: mike Date: 3/19/21 Time: 21:35
 */
public class RwBwModule extends BwModule {
  public RwBwModule(final String moduleName) {
    super(moduleName);
  }

  @Override
  protected void checkMessaging(final BwRequest req) throws Throwable {
    final RWClient cl = (RWClient)req.getClient();
    final BwRWActionForm form = (BwRWActionForm)req.getBwForm();

    if (!cl.getPublicAdmin()) {
      InOutBoxInfo ib = form.getInBoxInfo();
      if (ib == null) {
        ib = new InOutBoxInfo(cl, true);
        form.setInBoxInfo(ib);
      } else {
        ib.refresh(cl, false);
      }
    }

    if (shouldCheckNotifications(req)) {
      NotificationInfo ni = form.getNotificationInfo();
      if (ni == null) {
        ni = new NotificationInfo();
        form.setNotificationInfo(ni);
      }

      ni.refresh(cl, false);
    }
  }

  protected boolean shouldCheckNotifications(final BwRequest req) {
    return true;
  }

  @Override
  protected FilterBase defaultSearchFilter(final BwRequest req) {
    final RWClient cl = (RWClient)req.getClient();
    final BwRWActionForm form = (BwRWActionForm)req.getBwForm();

    FilterBase filter = null;

    if (cl.getWebUser()) {
      final var inBoxInfo = form.getInBoxInfo();
      if ((inBoxInfo != null) && (inBoxInfo.getColPath() != null)) {
        filter = new BwCollectionFilter(null,
                                        inBoxInfo.getColPath());
        filter.setNot(true);
      }
    }

    return filter;
  }
}
