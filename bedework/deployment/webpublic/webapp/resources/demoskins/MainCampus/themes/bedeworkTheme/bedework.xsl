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
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">

  <xsl:output method="xml" indent="no" media-type="text/html"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    standalone="yes" omit-xml-declaration="yes" />

  <!-- =========================================================

    DEMONSTRATION CALENDAR STYLESHEET

    MainCampus Calendar Suite - based on the Duke/Yale Skin

    This stylesheet is devoid of school branding.  It is a good
    starting point for development of a customized theme.

    It is based on work by Duke University and Yale University with
    credit also to the University of Chicago.

    For detailed instructions on how to work with the XSLT
    stylesheets included with this distribution, please see the
    Bedework Manual at http://www.jasig.org/bedework/documentation

    ===============================================================  -->

  <!-- DEFINE INCLUDES -->
  <!-- Theme preferences -->
  <xsl:include href="themeSettings.xsl" />

  <!-- theme utility functions -->
  <xsl:include href="themeUtil.xsl" />

  <!-- Page subsections -->
  <xsl:include href="head.xsl" />
  <xsl:include href="header.xsl" />
  <xsl:include href="leftColumn.xsl" />
  <xsl:include href="views.xsl" />
  <xsl:include href="subscriptions.xsl" />
  <xsl:include href="featuredEvents.xsl"/>
  <xsl:include href="navigation.xsl" />
  <xsl:include href="eventList.xsl" />
  <xsl:include href="eventListDiscrete.xsl" />
  <xsl:include href="event.xsl" />
  <xsl:include href="year.xsl" />
  <xsl:include href="calendarList.xsl" />
  <xsl:include href="search.xsl"/>
  <xsl:include href="ongoing.xsl" />
  <xsl:include href="deadlines.xsl" />
  <xsl:include href="systemStats.xsl"/>
  <xsl:include href="showPage.xsl"/>
  <xsl:include href="footer.xsl" />

  <!-- MAIN TEMPLATE -->
  <xsl:template match="/">
    <html lang="en">
      <xsl:call-template name="head"/>
      <body>
        <div id="wrap">
          <!-- HEADER BAR and TABS -->
          <div id="header">
            <xsl:call-template name="titleBar" />
            <xsl:call-template name="tabs" />
          </div>
          <!-- ERROR MESSAGES -->
          <xsl:if test="/bedework/error">
            <div id="errors">
              <xsl:apply-templates select="/bedework/error" />
            </div>
          </xsl:if>

          <div id="content">
            <div id="contentSection">

              <!-- LEFT COLUMN: calendar widget, views, subscriptions, and links -->
              <xsl:call-template name="leftColumn" />

              <!-- FEATURED EVENTS, if enabled -->
              <xsl:if test="$featuredEventsEnabled = 'true'">
                <xsl:call-template name="featuredEvents"/>
              </xsl:if>

              <!-- MAIN CONTENT: event listings, single events, calendar lists, search results -->
              <div id="center_column">
                <xsl:attribute name="class">
                  <xsl:choose>
                    <xsl:when test="$ongoingEvents = 'true'">center_column</xsl:when>
                    <xsl:otherwise>double_center_column</xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>


                <!-- branch on content, as defined by /bedework/page -->
                <xsl:choose>
                  <!-- day, week, month, year event listings -->
                  <xsl:when test="/bedework/page='eventscalendar'">
                    <div class="secondaryColHeader">
                      <xsl:call-template name="navigation" />
                    </div>
                    <xsl:choose>
                      <xsl:when test="/bedework/periodname = 'Year'">
                        <xsl:call-template name="yearView" />
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:call-template name="eventList"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>

                  <!-- single event display -->
                  <xsl:when test="/bedework/page = 'event'">
                    <xsl:apply-templates select="/bedework/event" mode="singleEvent"/>
                  </xsl:when>

                  <!-- list of calendar suite's subscriptions -->
                  <xsl:when test="/bedework/page='calendarList'">
                    <xsl:apply-templates select="/bedework/calendars" />
                  </xsl:when>

                  <!-- export calendar form -->
                  <xsl:when test="/bedework/page='displayCalendarForExport'">
                    <xsl:apply-templates select="/bedework/currentCalendar" mode="export" />
                  </xsl:when>

                  <!-- search result -->
                  <xsl:when test="/bedework/page='searchResult'">
                    <xsl:call-template name="searchResult" />
                    <xsl:call-template name="advancedSearch" />
                  </xsl:when>

                  <!-- list of discrete events (normally used only for feeds
                       (see the /feeder app).  Included here for visualization. -->
                  <xsl:when test="/bedework/page = 'eventList'">
                    <xsl:apply-templates select="/bedework/events" mode="eventListDiscrete"/>
                  </xsl:when>

                  <!-- system statistics -->
                  <xsl:when test="/bedework/page='showSysStats'">
                    <xsl:call-template name="stats" />
                  </xsl:when>

                  <!-- branch to an arbitrary page (an xsl template) using the
                       "appvar" session variable on a link like so:
                       /misc/showPage.rdo?setappvar=page(mypage)
                       Page templates are defined in showPage.xsl -->
                  <xsl:when test="/bedework/page='showPage'">
                    <xsl:choose>
                      <xsl:when test="/bedework/appvar[key='page']">
                        <xsl:call-template name="showPage">
                          <xsl:with-param name="pageName"><xsl:value-of select="/bedework/appvar[key='page']/value"/></xsl:with-param>
                        </xsl:call-template>
                      </xsl:when>
                      <xsl:otherwise>
                        <div id="page">
                          <xsl:copy-of select="$bwStr-Error-NoPage"/>
                        </div>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>

                  <!-- otherwise, show us what page was requested
                       (if the stylesheet is thorough, you should never see this) -->
                  <xsl:otherwise>
                    <xsl:copy-of select="$bwStr-Error"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="/bedework/page" />
                  </xsl:otherwise>

                </xsl:choose>
              </div>
            </div>

            <!-- ONGOING EVENTS if enabled -->
            <xsl:if test="$ongoingEvents = 'true'">
              <div class="right_column" id="right_column">
                <xsl:if test="$ongoingEvents = 'true'">
                  <xsl:call-template name="ongoingEventList" />
                </xsl:if>
                <!--
                <xsl:if test="$deadlines = 'true'">
                  <xsl:call-template name="deadlines" />
                </xsl:if>
                -->
              </div>
            </xsl:if>

            <div class="clear">&#160;</div>
          </div>
          <!-- FOOTER -->
          <xsl:call-template name="footer" />
        </div>

      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
