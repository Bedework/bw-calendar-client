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
package org.bedework.appcommon;

import org.bedework.access.AccessException;
import org.bedework.access.AccessPrincipal;
import org.bedework.access.Acl;
import org.bedework.access.PrivilegeSet;
import org.bedework.appcommon.client.Client;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.xml.XmlEmit;

import org.apache.struts.taglib.TagUtils;

import java.io.Serializable;
import java.io.StringWriter;

import javax.xml.namespace.QName;

/** Class to generate xml from an access specification. The resulting xml follows
 * the webdav acl spec rfc3744
 *
 *  @author Mike Douglass   douglm @ rpi.edu
 */
public class AccessXmlUtil extends org.bedework.access.AccessXmlUtil {
  /**
   */
  public static class Cb implements AccessXmlCb, Serializable {
    private Client cl;
    private TagUtils tagUtil = TagUtils.getInstance();

    QName errorTag;
    String errorMsg;

    Cb(final Client cl) {
      this.cl = cl;
    }

    public String makeHref(final String id, final int whoType) throws AccessException {
      try {
        return tagUtil.filter(cl.makePrincipalUri(id, whoType));
      } catch (Throwable t) {
        throw new AccessException(t);
      }
    }

    public AccessPrincipal getPrincipal() {
      return cl.getCurrentPrincipal();
    }

    public AccessPrincipal getPrincipal(final String href) throws AccessException {
      try {
        return cl.getPrincipal(href);
      } catch (CalFacadeException cfe) {
        throw new AccessException(cfe);
      }
    }

    @Override
    public void setErrorTag(final QName tag) throws AccessException {
      errorTag = tag;
    }

    @Override
    public QName getErrorTag() throws AccessException {
      return errorTag;
    }

    @Override
    public void setErrorMsg(final String val) throws AccessException {
      errorMsg = val;
    }

    @Override
    public String getErrorMsg() throws AccessException {
      return errorMsg;
    }
  }

  /** Acls use tags in the webdav and caldav namespace.
   *
   * @param xml to emit xml
   * @param cl fo callbacks
   */
  public AccessXmlUtil(final XmlEmit xml,
                       final Client cl) {
    super(caldavPrivTags, xml, new Cb(cl));
  }

  /** Represent the acl as an xml string
   *
   * @param acl
   * @param cl fo callbacks
   * @return String xml representation
   * @throws AccessException
   */
  public static String getXmlAclString(final Acl acl,
                                       final Client cl) throws AccessException {
    return getXmlAclString(acl, false, caldavPrivTags, new Cb(cl));
  }

  /** Produce an xml representation of current user privileges from an array
   * of allowed/disallowed/unspecified flags indexed by a privilege index.
   *
   * @param xml
   * @param privileges    char[] of allowed/disallowed
   * @throws AccessException
   */
  public static void emitCurrentPrivSet(final XmlEmit xml,
                                        final char[] privileges) throws AccessException {
    emitCurrentPrivSet(xml, caldavPrivTags, privileges);
  }

  /** Produce an xml representation of current user privileges from an array
   * of allowed/disallowed/unspecified flags indexed by a privilege index,
   * returning the representation a a String
   *
   * @param ps    PrivilegeSet allowed/disallowed
   * @return String xml
   * @throws AccessException
   */
  public static String getCurrentPrivSetString(final PrivilegeSet ps)
          throws AccessException {
    try {
      char[] privileges = ps.getPrivileges();

      XmlEmit xml = new XmlEmit(true);  // no headers
      StringWriter su = new StringWriter();
      xml.startEmit(su);
      emitCurrentPrivSet(xml, caldavPrivTags, privileges);

      su.close();

      return su.toString();
    } catch (AccessException ae) {
      throw ae;
    } catch (Throwable t) {
      throw new AccessException(t);
    }
  }
}
