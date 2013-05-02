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

import org.bedework.sysevents.monitor.BwSysMonitorMBean;
import org.bedework.sysevents.monitor.MonitorStat;
import org.bedework.webcommon.BwAbstractAction;
import org.bedework.webcommon.BwActionFormBase;
import org.bedework.webcommon.BwRequest;

import edu.rpi.cmt.jmx.MBeanUtil;
import edu.rpi.sss.util.xml.XmlEmit;

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

  /* (non-Javadoc)
   * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
   */
  @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    List<MonitorStat> stats = getMonitor().getStats();

    request.getResponse().setContentType("text/xml; charset=UTF-8");

    Writer wtr = request.getResponse().getWriter();

    try {
      XmlEmit xml = new XmlEmit();

      xml.startEmit(wtr);

      xml.openTag(monitorTag);

      for (MonitorStat stat: stats) {
        xml.openTag(statTag);

        xml.property(nameTag, stat.getName());
        xml.property(valueTag, stat.getValue());

        xml.closeTag(statTag);
      }

      xml.closeTag(monitorTag);
    } finally {
      wtr.close();
    }

    return forwardNull;
  }

  private synchronized BwSysMonitorMBean getMonitor() throws Throwable {
    if (monitor == null) {
      monitor = (BwSysMonitorMBean)MBeanUtil.getMBean(BwSysMonitorMBean.class,
                                                     "org.bedework:service=BwSysMonitor");
    }

    return monitor;
  }
}
