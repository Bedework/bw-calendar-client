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

import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.icalendar.URIgen;


import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** NOTE this code has never really been used and may be removed.
 *
 *
 *
 * This class defines methods used to return uris which will provide access
 * to entities located somewhere in the implementing calendar system.
 *
 * <p>For example, a call to getLocationURI(loc) might return something like<br/>
 *     "http://cal.myplace.edu/ucal/locations.do?id=1234"
 *
 * <p>Implementing classes will be used by services like the synch process
 * which needs to embed usable urls in the generated Icalendar objects.
 *
 * The generated
 *
 * @author Mike Douglass   douglm@rpi.edu
 */
public class BwWebURIgen implements URIgen {
  private String urlPrefix;

  private Pattern locPattern = Pattern.compile("(.*location=)([^)]*)");
  private Pattern spPattern = Pattern.compile("(.*sponsor=)([^)]*)");

  /** Constructor - the prefix must point to a server application which
   * implements the requests built below. This application should run
   * unauthenticated or authenticated but in unauthenticated mode it will
   * only return public entities.
   *
   * @param urlPrefix   String value which will prefix all the urls.
   *                    Does NOT have trailing "/"
   */
  public BwWebURIgen(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

  public URI getLocationURI(BwLocation val) throws CalFacadeException {
    try {
      return new URI(urlPrefix + "/location.do?locationId=" + val.getId());
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.uwcal.common.URIgen#getLocation(java.net.URI)
   */
  public BwLocation getLocation(URI val) throws CalFacadeException {
    try {
      String query = val.getQuery();

      if (query == null) {
        throw new CalFacadeException("Not a location URI");
      }

      Matcher m = locPattern.matcher(query);

      if (!m.matches()) {
        throw new CalFacadeException("Not a location URI");
      }

      String uid = m.group(2);

      BwLocation loc = new BwLocation();

      loc.setUid(uid);

      return loc;
    } catch (CalFacadeException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  public URI getSponsorURI(BwContact val) throws CalFacadeException {
    try {
      return new URI(urlPrefix + "/contact.do?sponsorId=" + val.getId());
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  public BwContact getSponsor(URI val) throws CalFacadeException {
    try {
      String query = val.getQuery();

      if (query == null) {
        throw new CalFacadeException("Not a sponsor URI");
      }

      Matcher m = spPattern.matcher(query);

      if (!m.matches()) {
        throw new CalFacadeException("Not a sponsor URI");
      }

      //String uid = m.group(2);

      BwContact sp = new BwContact();

      //sp.setId(id.intValue());

      return sp;
    } catch (CalFacadeException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }
}

