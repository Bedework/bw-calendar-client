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
package org.bedework.webcommon.config;

import org.bedework.appcommon.ConfigCommon;
import org.bedework.calfacade.exc.CalFacadeException;

import org.bedework.util.jmx.ConfBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

/** All the configuration objects used by CalSvc and its callers.
 *
 */
@SuppressWarnings("rawtypes")
public final class ClientConfigurations extends ConfBase {
  /* Name of the property holding the location of the config data */
  private static final String confuriPname = "org.bedework.clients.confuri";

  private static volatile Object lock = new Object();

  private Map<String, ConfigCommon> clientConfigs =
      new HashMap<String, ConfigCommon>();

  private static ClientConfigurations configs;

  /**
   * @return a configs object
   * @throws CalFacadeException
   */
  public static ClientConfigurations getConfigs() throws CalFacadeException {
    if (configs != null) {
      return configs;
    }

    synchronized (lock) {
      if (configs != null) {
        return configs;
      }

      configs = new ClientConfigurations();
      return configs;
    }
  }

  /** Start up (load) the config mbeans.
   *
   * @throws CalFacadeException
   */
  public static void startConfigs() throws CalFacadeException {
    getConfigs();
  }

  /** Shut down the config jmx bean(s)
   *
   * @throws CalFacadeException
   */
  public static void stopConfigs() throws CalFacadeException {
    synchronized (lock) {
      if (configs == null) {
        return;
      }

      configs.stop();

      configs = null;
    }
  }

  @Override
  public String loadConfig() {
    return null;
  }

  /**
   * @throws CalFacadeException
   */
  private ClientConfigurations() throws CalFacadeException {
    super("org.bedework.clients:service=System");

    try {
      setConfigPname(confuriPname);

      loadClientConfigs();
    } catch (CalFacadeException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  /**
   * @param name
   * @return client config identified by name - or null.
   * @throws CalFacadeException
   */
  public ConfigCommon getClientConfig(final String name) throws CalFacadeException {
    return clientConfigs.get(name);
  }

  private void loadClientConfigs() throws Throwable {
    List<String> names = getStore().getConfigs();

    for (String cn: names) {
      ObjectName objectName = createObjectName("client-config", cn);

      /* Read the config so we can get the mbean class name. */

      ConfigCommonImpl cCfg = (ConfigCommonImpl)getStore().getConfig(cn);

      if (cCfg == null) {
        error("Unable to read client configuration " + cn);
        continue;
      }

      String mbeanClassName = cCfg.getMbeanClassName();

      if (mbeanClassName == null) {
        error("Must set the mbean class name for config " + cn);
        error("Falling back to base class for " + cn);

        mbeanClassName = ClientConf.class.getCanonicalName();
      }

      @SuppressWarnings("unchecked")
      ClientConf<ConfigCommonImpl> cc = (ClientConf<ConfigCommonImpl>)makeObject(mbeanClassName);
      cc.init(getStore(), objectName.toString(), cCfg);

      cc.saveConfig();
      clientConfigs.put(cn, cc);

      register(new ObjectName(cc.getServiceName()), cc);
    }
  }

  void stop() {
    try {
      getManagementContext().stop();
    } catch (Throwable t){
      t.printStackTrace();
    }
  }
}