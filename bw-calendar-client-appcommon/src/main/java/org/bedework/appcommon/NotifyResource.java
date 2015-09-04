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
import org.bedework.calfacade.svc.EventInfo;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Class to hold info about a notification.
 *
 * @author Mike Douglass   douglm@rpi.edu
 *  @version 1.0
 */
public class NotifyResource implements Serializable {
  private final String name;

  private final String tag;

  @SuppressWarnings("FieldMayBeFinal")
  private String type;

  private final String content;

  private final NotificationType note;

  private String xmlFragment;

  private List<ResourceInfo> resourcesInfo;

  public static class ResourceInfo {
    private final String href;
    private boolean created;
    private boolean deleted;
    private boolean error;
    private String summary;
    private BwDateTime dtstart;

    private DateTimeFormatter formattedStart;

    ResourceInfo(final String href) {
      this.href = href;
    }

    public String getHref() {
      return href;
    }

    public boolean getCreated() {
      return created;
    }

    public boolean getDeleted() {
      return deleted;
    }

    /**
     *
     * @return true if any error accessing event
     */
    public boolean getError() {
      return error;
    }

    public String getSummary() {
      return summary;
    }

    public DateTimeFormatter getFormattedStart() {
      if ((formattedStart == null) && (dtstart != null)) {
        formattedStart = new DateTimeFormatter(dtstart);
      }
      return formattedStart;
    }
  }

  /**
   * @param cl the client
   * @param res the resource entity
   * @throws CalFacadeException
   */
  public NotifyResource(final Client cl,
                        final BwResource res) throws CalFacadeException {
    name = res.getName();
    tag = makeTag(res);

    try {
      content = res.getContent().getStringContent();

      note = Parser.fromXml(content);

      if ((note != null) && (note.getNotification() != null)) {
        type = note.getNotification().getElementName().getLocalPart();
      }

      initEventInfo(cl);
    } catch (final Throwable t) {
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
    if (note == null) {
      return null;
    }
    return note.getParsed();
  }

  public BaseNotificationType getNotification() {
    if (note == null) {
      return null;
    }
    return note.getNotification();
  }

  public List<ResourceInfo> getResourcesInfo() {
    return resourcesInfo;
  }

  private void initEventInfo(final Client cl) throws CalFacadeException {
    resourcesInfo = new ArrayList<>();

    final BaseNotificationType bnt = getNotification();

    if (bnt == null) {
      return;
    }

    final QName theType = bnt.getElementName();

    getHref:
    {
      if (bnt instanceof SuggestBaseNotificationType) {
        final SuggestBaseNotificationType sbnt = (SuggestBaseNotificationType)bnt;

        resourcesInfo.add(new ResourceInfo(sbnt.getHref()));
        break getHref;
      }

      if (bnt instanceof EventregBaseNotificationType) {
        final EventregBaseNotificationType ebnt = (EventregBaseNotificationType)bnt;

        resourcesInfo.add(new ResourceInfo(ebnt.getHref()));
        break getHref;
      }

      if (bnt instanceof ResourceChangeType) {
        final ResourceChangeType rct = (ResourceChangeType)bnt;

        if (rct.getCreated() != null) {
          final ResourceInfo ri = new ResourceInfo(rct.getCreated().getHref());
          ri.created = true;
          resourcesInfo.add(ri);
        } else if (rct.getDeleted() != null) {
          final ResourceInfo ri = new ResourceInfo(rct.getDeleted().getHref());
          ri.deleted = true;

          if (rct.getDeleted().getDeletedDetails() != null) {
            ri.summary = rct.getDeleted().getDeletedDetails()
                                   .getDeletedSummary();
          }
          resourcesInfo.add(ri);
        } else if (!Util.isEmpty(rct.getUpdated())) {
          for (final UpdatedType ut: rct.getUpdated()) {
            resourcesInfo.add(new ResourceInfo(ut.getHref()));
          }
        }
        break getHref;
      }
    }

    if (Util.isEmpty(resourcesInfo)) {
      // No event(s).
      return;
    }

    for (final ResourceInfo ri: resourcesInfo) {
      if (ri.getDeleted()) {
        continue;
      }

      try {
        final EventInfo ei = cl.getEvent(ri.getHref());

        if (ei != null) {
          final BwEvent ev = ei.getEvent();

          ri.dtstart = ev.getDtstart();
          ri.summary = ev.getSummary();
        }
      } catch (final Throwable ignored) {
        ri.error = true;
      }
    }
  }

  /** Output in jsp
   * @return formatted xml without the header
   */
  @SuppressWarnings("unused")
  public String getXmlFragment() {
    if (xmlFragment != null) {
      return xmlFragment;
    }

    try {
      final OutputFormat format = OutputFormat.createPrettyPrint();
      format.setTrimText(false);
      format.setSuppressDeclaration(true);

      final StringWriter sw = new StringWriter();
      final XMLWriter writer = new XMLWriter(sw, format);
      writer.write(DocumentHelper.parseText(content));

      xmlFragment = sw.toString();

      return xmlFragment;
    } catch (final Throwable t){
      return "<error>" + t.getLocalizedMessage() + "</error>";
    }
  }

  /**
   * @param rdr for content
   * @return parsed Document
   * @throws Throwable
   */
  private Document parseXml(final Reader rdr) throws Throwable {
    if (rdr == null) {
      return null;
    }

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    final DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(new InputSource(rdr));
  }

  /**
   * @param r resource
   * @return a tag value for comparison
   */
  public static String makeTag(final BwResource r) {
    return r.getLastmod() + "-" + r.getSequence();
  }
}
