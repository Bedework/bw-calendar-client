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

package org.bedework.webcommon;

import org.bedework.webcommon.config.ClientConfigurations;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/** A listener to load and unload the jmx configs.
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class ServletListener implements ServletContextListener {
  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    try {
      ClientConfigurations.startConfigs();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    try {
      ClientConfigurations.stopConfigs();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}

