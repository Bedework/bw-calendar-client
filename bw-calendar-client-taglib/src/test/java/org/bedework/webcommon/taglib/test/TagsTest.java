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
package org.bedework.webcommon.taglib.test;

import org.bedework.calfacade.BwXproperty;
import org.bedework.webcommon.tagcommon.BwTagUtils;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bedework.calfacade.BwXproperty.bedeworkCalsuite;
import static org.bedework.calfacade.BwXproperty.makeBwAlias;

/** Test the access classes
 *
 * @author Mike Douglass       douglm@bedework.edu
   @version 1.0
 */
public class TagsTest extends TestCase {
  boolean debug = true;

  public void testOutXprops() {
    final MockJspWriter wtr = new MockJspWriter();

    final List<BwXproperty> xprops = new ArrayList<>(5);

    xprops.add(makeBwAlias(
            "Holidays",
            "/public/aliases/Academic Calendar/Holidays",
            "/public/cals/MainCal",
            "/user/agrp_calsuite-MainCampus/Academic Calendar/Holidays"));

    xprops.add(makeBwAlias(
            "Lectures",
            "/public/aliases/Lectures and Seminars/Lectures",
            "/public/cals/MainCal",
            "/user/agrp_calsuite-MainCampus/Lectures and Seminars/Lectures"));

    xprops.add(new BwXproperty("X-BEDEWORK-SUBMITTEDBY",
                               null,
                               "admin for calsuite-MainCampus (agrp_calsuite-MainCampus)"));

    xprops.add(new BwXproperty(bedeworkCalsuite,
                               null,
                               "MainCampus"));

    try {
      BwTagUtils.OutXprops(wtr, "  ", true, xprops);
    } catch (final IOException ioe) {
      ioe.printStackTrace();
      fail("Exception " + ioe.getMessage());
    }

    log(wtr.getContent());
  }

  private void log(final String msg) {
    System.out.println(this.getClass().getName() + ": " + msg);
  }
}

