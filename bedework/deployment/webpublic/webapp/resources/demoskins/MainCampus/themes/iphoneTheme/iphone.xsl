<?xml version="1.0" encoding="UTF-8"?>
<!-- 
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
-->
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">
<xsl:output method="xml"
    indent="yes"
    media-type="text/html"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    standalone="yes"
    omit-xml-declaration="yes"/>

  <!-- ===============================================
             BEDEWORK iPHONE/MOBILE STYLESHEET

        Renders Bedework public client for mobile
        devices, particularly those with javascript and
        touchscreens.  Call this stylesheet using:

        http://localhost:8080/cal/setup.do?browserTypeSticky=PDA
        to revert to the default browserType, use
        http://localhost:8080/cal/setup.do?browserTypeSticky=default

       ==============================================  -->

  <!-- DEFINE INCLUDES -->
  <!-- Theme preferences -->
  <xsl:include href="themeSettings.xsl" />

  <!-- Page subsections -->
  <xsl:include href="navigation.xsl" />
  <xsl:include href="listEvents.xsl" />
  <xsl:include href="event.xsl" />
  <xsl:include href="calendarList.xsl" />
  <xsl:include href="dateSelect.xsl" />

  <!-- MAIN TEMPLATE -->
  <xsl:template match="/">
    <html xml:lang="en">
      <head>
        <title><xsl:copy-of select="$bwStr-Root-PageTitle"/></title>
        <meta name="viewport" id="viewport" content="width=device-width, user-scalable=yes,initial-scale=1.0,maximum-scale=1.0" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta http-equiv="Pragma" content="no-cache"/>
        <meta http-equiv="Expires" content="-1"/>
        <link rel="stylesheet" href="{$resourcesRoot}/css/jsphone.css"/>
        <script type="text/javascript" src="{$resourcesRoot}/javascript/jsphone.js"><xsl:text> </xsl:text></script>
      </head>
      <body>
        <h2 id="title" class="title">
          <xsl:if test="/bedework/page!='eventscalendar'">
            <xsl:attribute name="onclick">gotourl(this, '<xsl:value-of select="$setup"/>')</xsl:attribute>
          </xsl:if>
          <xsl:copy-of select="$bwStr-HdBr-PageTitle"/>
        </h2>
        <!-- ERROR MESSAGES -->
        <xsl:if test="/bedework/error">
          <div id="errors">
            <xsl:apply-templates select="/bedework/error" />
          </div>
        </xsl:if>
        <xsl:choose>
          <xsl:when test="/bedework/page='event'">
            <!-- show an event -->
            <xsl:apply-templates select="/bedework/event" mode="singleEvent"/>
          </xsl:when>
          <xsl:when test="/bedework/page='calendarList'">
            <!-- show a list of all calendars -->
            <xsl:apply-templates select="/bedework/calendars"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- otherwise, show the eventsCalendar -->
            <!-- main eventCalendar content -->
            <xsl:call-template name="navigation"/>
            <xsl:call-template name="listView"/>
            <xsl:call-template name="dateSelect"/>
          </xsl:otherwise>
        </xsl:choose>
        <!-- footer -->
        <p class="footer">
          <a href="{$fetchPublicCalendars}"><xsl:copy-of select="$bwStr-Cals-AllCalendars"/></a> |
          <a href="{$setup}&amp;browserTypeSticky=default"><xsl:copy-of select="$bwStr-Foot-ResetSkin"/></a><br/>
        </p>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
