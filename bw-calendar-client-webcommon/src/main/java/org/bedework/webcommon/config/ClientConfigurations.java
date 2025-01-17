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
import org.bedework.base.exc.BedeworkException;
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
  /* Name of the directory holding the config data */
  private static final String confDirName = "clients";

  private static final Object lock = new Object();

  private final Map<String, ConfigCommon> clientConfigs = new HashMap<>();

  private static ClientConfigurations configs;

  /**
   * @return a configs object
) {   */
  public static ClientConfigurations getConfigs() {
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
) {   */
  public static void startConfigs() {
    getConfigs();
  }

  /** Shut down the config jmx bean(s)
   *
) {   */
  public static void stopConfigs() {
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
   */
  private ClientConfigurations() {
    super("org.bedework.clients:service=System",
          confDirName,
          null); // No overall config

    loadClientConfigs();
  }

  /**
   * @param name of config
   * @return client config identified by name - or null.
   */
  public ConfigCommon getClientConfig(final String name) {
    return clientConfigs.get(name);
  }

  private void loadClientConfigs() {
    try {
      final List<String> names = getStore().getConfigs();

      for (final String cn: names) {
        final ObjectName objectName = createObjectName(
                "client-config", cn);

        /* Read the config so we can get the mbean class name. */

        final ConfigCommonImpl cCfg =
                (ConfigCommonImpl)getStore().getConfig(cn);

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

        @SuppressWarnings("unchecked") final ClientConf<ConfigCommonImpl> cc =
                (ClientConf<ConfigCommonImpl>)makeObject(
                        mbeanClassName,
                        objectName.toString(),
                        getStore(),
                        cn);

        if (cc == null) {
          throw new BedeworkException(
                  "Unable to create mbean class: " + mbeanClassName);
        }

        cc.setConfig(cCfg);

        clientConfigs.put(cn, cc);

        register(new ObjectName(cc.getServiceName()), cc);
      }
    } catch (final BedeworkException be) {
      throw be;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  @Override
  public void stop() {
    try {
      getManagementContext().stop();
    } catch (final Throwable t){
      error(t);
    }
  }
}