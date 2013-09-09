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

import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.AppleServerTags;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Class to hold info about a notification.
 *
 * @author Mike Douglass   douglm@bedework.edu
 *  @version 1.0
 */
public class NotifyResource implements Serializable {
  private String name;

  private String tag;

  private String type;

  private String content;

  private Document doc;

  /**
   * @param res
   * @throws CalFacadeException
   */
  public NotifyResource(final BwResource res) throws CalFacadeException {
    name = res.getName();
    tag = makeTag(res);

    try {
      content = res.getContent().getStringContent();

      if (content != null) {
        doc = parseXml(new StringReader(content));
      }

      if (doc != null) {
        for (Element el: XmlUtil.getElements(doc.getDocumentElement())) {
          if (XmlUtil.nodeMatches(el, AppleServerTags.dtstamp)) {
            continue;
          }

          type = el.getLocalName();
          break;
        }
      }
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  /** Get the name.
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /** Get the tag.
   *
   * @return type
   */
  public String getTag() {
    return tag;
  }

  /** Get the type - this is the name of the root element in the
   * notification message.
   *
   * @return type
   */
  public String getType() {
    return type;
  }

  /** Get the parsed XML.
   *
   * @return parsed xml
   */
  public Document getDoc() {
    return doc;
  }

  /**
   * @return formatted xml without the header
   */
  public String getXmlFragment() {
    try {
      OutputFormat format = OutputFormat.createPrettyPrint();
      format.setTrimText(false);
      format.setSuppressDeclaration(true);

      StringWriter sw = new StringWriter();
      XMLWriter writer = new XMLWriter(sw, format);
      writer.write(DocumentHelper.parseText(content));

      return sw.toString();
    } catch (Throwable t){
      return "<error>" + t.getLocalizedMessage() + "</error>";
    }
  }

  /**
   * @param rdr
   * @return parsed Document
   * @throws Throwable
   */
  private Document parseXml(final Reader rdr) throws Throwable {
    if (rdr == null) {
      return null;
    }

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(new InputSource(rdr));
  }

  /**
   * @param r
   * @return a tag value for comparison
   */
  public static String makeTag(final BwResource r) {
    return r.getLastmod() + "-" + r.getSequence();
  }
}
