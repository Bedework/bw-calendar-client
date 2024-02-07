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
package org.bedework.webcommon.monitor;

import org.bedework.calfacade.MonitorStat;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwSysMonitorMBean;
import org.bedework.util.jmx.MBeanUtil;
import org.bedework.util.xml.XmlEmit;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Just display monitor statistics in xml.
 *
 * <p>No request parameters.
 *
 * <p>No forwards.
 *
 * @author Mike Douglass
 */
public class MonitorAction extends BwAbstractAction {
  private static BwSysMonitorMBean monitor;

  /** */
  public static QName monitorTag = new QName("monitor");

  /** */
  public static QName statTag = new QName("stat");

  /** */
  public static QName nameTag = new QName("name");

  /** */
  public static QName valueTag = new QName("value");

  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) {
    final List<MonitorStat> stats = getMonitor().getStats();

    request.getResponse().setContentType("text/xml; charset=UTF-8");

    try (final Writer wtr = request.getWriter()) {
      final XmlEmit xml = new XmlEmit();

      xml.startEmit(wtr);

      xml.openTag(monitorTag);

      for (final MonitorStat stat: stats) {
        xml.openTag(statTag);

        xml.property(nameTag, stat.getName());
        xml.property(valueTag, stat.getValue());

        xml.closeTag(statTag);
      }

      xml.closeTag(monitorTag);
    } catch (final IOException e) {
      throw new CalFacadeException(e);
    }

    return forwardNull;
  }

  private synchronized BwSysMonitorMBean getMonitor() {
    if (monitor == null) {
      try {
        monitor = (BwSysMonitorMBean)MBeanUtil.getMBean(
                BwSysMonitorMBean.class,
                "org.bedework:service=BwSysMonitor");
      } catch (final Throwable e) {
        throw new CalFacadeException(e);
      }
    }

    return monitor;
  }
}
