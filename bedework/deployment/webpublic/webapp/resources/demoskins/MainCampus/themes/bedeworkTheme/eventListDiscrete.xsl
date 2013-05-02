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

  <!--==== LIST EVENTS - for listing discrete events ====-->

  <!-- The listEvents.do action is used for generating data feeds of events and
       should be used with the /feeder application.  The feeder application provides
       a common place to pull events regardless of calendar suite.

       The listing presented here allows developers to call listEvent
       requests against the system and see the results.  It is not currently
       linked to the bedeworkTheme web pages directly.  -->

  <xsl:template match="events" mode="eventListDiscrete">
    <div class="secondaryColHeader">
      <h3>
        <xsl:copy-of select="$bwStr-LsEv-Upcoming"/>
        <xsl:choose>
          <xsl:when test="/bedework/selectionState/selectionType = 'view'">
            <xsl:text> - </xsl:text>
            <xsl:value-of select="/bedework/selectionState/view/name"/>
          </xsl:when>
          <xsl:when test="/bedework/selectionState/selectionType = 'collections'">
            <xsl:text> - </xsl:text>
            <xsl:value-of select="/bedework/appvar[key='curCollection']/value"/>
          </xsl:when>
        </xsl:choose>
      </h3>
    </div>
    <div id="listEvents">
      <!--h1><xsl:copy-of select="$bwStr-LsEv-Upcoming"/></h1-->
      <ul>
        <xsl:choose>
          <xsl:when test="not(event)">
            <li><xsl:copy-of select="$bwStr-LsEv-NoEventsToDisplay"/></li>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="event[not(categories/category[uid = $ongoingEventsCatUid])]">
              <xsl:variable name="id" select="id"/>
              <xsl:variable name="calPath" select="calendar/encodedPath"/>
              <xsl:variable name="guid" select="guid"/>
              <xsl:variable name="guidEsc" select="translate(guid, '.', '_')"/>
              <xsl:variable name="recurrenceId" select="recurrenceId"/>
              <li>
                <xsl:attribute name="class">
                  <xsl:choose>
                    <xsl:when test="status='CANCELLED'">bwStatusCancelled</xsl:when>
                    <xsl:when test="status='TENTATIVE'">bwStatusTentative</xsl:when>
                    <xsl:when test="position() mod 2 = 0">even</xsl:when>
                    <xsl:otherwise>odd</xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
                
                <!-- event icons -->
                <span class="icons">
                  <xsl:variable name="gStartdate" select="start/utcdate"/>
                  <xsl:variable name="gLocation" select="location/address"/>
                  <xsl:variable name="gEnddate" select="end/utcdate"/>
                  <xsl:variable name="gText" select="summary"/>
                  <xsl:variable name="gDetails" select="summary"/>
      
                  <xsl:if test="$eventIconDownloadIcs = 'true'">
                    <xsl:variable name="eventIcalName" select="concat($guid,'.ics')"/>
                    <a href="{$export}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}&amp;nocache=no&amp;contentName={$eventIcalName}" title="{$bwStr-SgEv-Download}">
                      <img src="{$resourcesRoot}/images/std-ical_icon_small.gif" alt="{$bwStr-SgEv-Download}"/>
                    </a>
                  </xsl:if>
                  <xsl:if test="$eventIconAddToMyCal = 'true'">
                    <a href="{$privateCal}/event/addEventRef.do?calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}" title="{$bwStr-LsVw-AddEventToMyCalendar}" target="myCalendar">
                      <img class="addref" src="{$resourcesRoot}/images/add2mycal-icon-small.gif" width="12" height="16" border="0" alt="{$bwStr-LsVw-AddEventToMyCalendar}"/>
                    </a>
                  </xsl:if>
                  <xsl:if test="$eventIconFacebook = 'true'">
                    <xsl:choose>
                      <xsl:when test="string-length($recurrenceId)">
                        <a href="http://www.facebook.com/share.php?u={$eventView}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}&amp;t={$gText}" title="{$bwStr-SgEv-AddToFacebook}">
                          <img title="{$bwStr-SgEv-AddToFacebook}" src="{$resourcesRoot}/images/Facebook_Badge_small.gif" alt="{$bwStr-SgEv-AddToFacebook}"/>
                        </a>
                      </xsl:when>
                      <xsl:otherwise>
                        <a href="http://www.facebook.com/share.php?u={$eventView}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}&amp;t={$gText}" title="{$bwStr-SgEv-AddToFacebook}">
                          <img title="{$bwStr-SgEv-AddToFacebook}" src="{$resourcesRoot}/images/Facebook_Badge_small.gif" alt="{$bwStr-SgEv-AddToFacebook}"/>
                        </a>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                  <xsl:if test="$eventIconGoogleCal = 'true'">
                    <a href="http://www.google.com/calendar/event?action=TEMPLATE&amp;dates={$gStartdate}/{$gEnddate}&amp;text={$gText}&amp;details={$gDetails}&amp;location={$gLocation}">
                      <img title="{$bwStr-SgEv-AddToGoogleCalendar}" src="{$resourcesRoot}/images/gcal_small.gif" alt="{$bwStr-SgEv-AddToGoogleCalendar}"/>
                    </a>
                  </xsl:if>
                  <xsl:if test="$eventIconShareThis = 'true'">
                    <a href="https://wiki.jasig.org/pages/viewpage.action?pageId=50860497">
                      <img title="{$bwStr-SgEv-ShareThis}" src="{$resourcesRoot}/images/share-icon-16x16.png" alt="{$bwStr-SgEv-ShareThis}"/>
                    </a>
                  </xsl:if>
                </span>
                
                <!-- event thumbnail -->
                <xsl:if test="xproperties/X-BEDEWORK-IMAGE or $usePlaceholderThumb = 'true'">
                  <xsl:variable name="imgPrefix">
                    <xsl:choose>
                      <xsl:when test="starts-with(xproperties/X-BEDEWORK-IMAGE/values/text,'http')"></xsl:when>
                      <xsl:otherwise><xsl:value-of select="$bwEventImagePrefix"/></xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="imgThumbPrefix">
                    <xsl:choose>
                      <xsl:when test="starts-with(xproperties/X-BEDEWORK-THUMB-IMAGE/values/text,'http')"></xsl:when>
                      <xsl:otherwise><xsl:value-of select="$bwEventImagePrefix"/></xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
	                <a href="{$eventView}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
	                  <img class="eventThumb">
	                    <xsl:attribute name="width"><xsl:value-of select="$thumbWidth"/></xsl:attribute>
	                    <xsl:attribute name="src">
	                      <xsl:choose>
	                        <xsl:when test="xproperties/X-BEDEWORK-THUMB-IMAGE"><xsl:value-of select="$imgThumbPrefix"/><xsl:value-of select="xproperties/X-BEDEWORK-THUMB-IMAGE/values/text"/></xsl:when>
	                        <xsl:when test="xproperties/X-BEDEWORK-IMAGE and $useFullImageThumbs = 'true'"><xsl:value-of select="$imgPrefix"/><xsl:value-of select="xproperties/X-BEDEWORK-IMAGE/values/text"/></xsl:when>
	                        <xsl:otherwise><xsl:value-of select="$resourcesRoot"/>/images/placeholder.png</xsl:otherwise>
	                      </xsl:choose>
	                    </xsl:attribute>
	                    <xsl:attribute name="alt"><xsl:value-of select="summary"/></xsl:attribute>
	                  </img>
	                </a>
	              </xsl:if>
                
                <div class="eventListContent">
                  <xsl:if test="xproperties/X-BEDEWORK-IMAGE or $usePlaceholderThumb = 'true'">
                    <xsl:attribute name="class">eventListContent withImage</xsl:attribute>
                  </xsl:if>
                  <!-- event title -->
                  <xsl:if test="status='CANCELLED'"><strong><xsl:copy-of select="$bwStr-LsVw-Canceled"/><xsl:text> </xsl:text></strong></xsl:if>
                  <xsl:if test="status='TENTATIVE'"><strong><xsl:copy-of select="$bwStr-LsEv-Tentative"/><xsl:text> </xsl:text></strong></xsl:if>
                  <h4>
                    <a href="{$eventView}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
                      <xsl:value-of select="summary"/>
                      <xsl:if test="summary = ''">
                        <xsl:copy-of select="$bwStr-SgEv-NoTitle" />
                      </xsl:if>
                    </a>
                  </h4>
                  
                  <xsl:value-of select="substring(start/dayname,1,3)"/>,
                  <xsl:value-of select="start/longdate"/>
                  <xsl:text> </xsl:text>
                  <xsl:if test="start/allday != 'true'">
                    <xsl:value-of select="start/time"/>
                  </xsl:if>
                  <xsl:choose>
                    <xsl:when test="start/shortdate != end/shortdate">
                      -
                      <xsl:value-of select="substring(end/dayname,1,3)"/>,
                      <xsl:value-of select="end/longdate"/>
                      <xsl:text> </xsl:text>
                      <xsl:if test="start/allday != 'true'">
                        <xsl:value-of select="end/time"/>
                      </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:if test="start/time != end/time">
                        -
                        <xsl:value-of select="end/time"/>
                      </xsl:if>
                    </xsl:otherwise>
                  </xsl:choose>
                  
                  <br/>
                  <xsl:copy-of select="$bwStr-LsVw-Location"/><xsl:text> </xsl:text>
                  <xsl:value-of select="location/address"/>
                  <xsl:if test="location/subaddress != ''">
                    , <xsl:value-of select="location/subaddress"/>
                  </xsl:if>
  
                  <xsl:if test="/bedework/appvar[key='listEventsSummaryMode']/value='details'">
                    <br/>
                    <xsl:value-of select="description"/>
                    <xsl:if test="link != ''">
                      <br/>
                      <xsl:variable name="link" select="link"/>
                      <a href="{$link}" class="moreLink"><xsl:value-of select="link"/></a>
                    </xsl:if>
                    <xsl:if test="categories/category">
                      <br/>
                      <xsl:copy-of select="$bwStr-LsEv-Categories"/>
                      <xsl:for-each select="categories/category">
                        <xsl:value-of select="value"/><xsl:if test="position() != last()">, </xsl:if>
                      </xsl:for-each>
                    </xsl:if>
                    <br/>
                    <em>
                      <xsl:if test="cost!=''">
                        <xsl:value-of select="cost"/>.&#160;
                      </xsl:if>
                      <xsl:if test="contact/name!='none'">
                        <xsl:copy-of select="$bwStr-LsEv-Contact"/><xsl:text> </xsl:text><xsl:value-of select="contact/name"/>
                      </xsl:if>
                    </em>
                  </xsl:if>
                  
                  <xsl:if test="xproperties/X-BEDEWORK-ALIAS">
                    <br/>
                    <xsl:copy-of select="$bwStr-LsVw-TopicalArea"/><xsl:text> </xsl:text>
                    <span class="eventSubscription">
                      <xsl:for-each select="xproperties/X-BEDEWORK-ALIAS">
                        <xsl:choose>
                          <xsl:when test="parameters/X-BEDEWORK-PARAM-DISPLAYNAME">
                            <xsl:value-of select="parameters/X-BEDEWORK-PARAM-DISPLAYNAME"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="substring-afterLastInstanceOf">
                              <xsl:with-param name="string" select="values/text"/>
                              <xsl:with-param name="char">/</xsl:with-param>
                            </xsl:call-template>
                          </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="position()!=last()">, </xsl:if>
                      </xsl:for-each>
                    </span>
                  </xsl:if>
                </div>
                <br class="clear"/>

              </li>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </ul>
    
	    <xsl:if test="paged = 'true' and numPages &gt; 1">
	      <span class="resultPages" id="resultsBottom">
	        <xsl:copy-of select="$bwStr-Srch-Pages" />
	        <xsl:if test="number(curPage) != 1">
	          <xsl:variable name="prevPage" select="number(curPage) - 1" />
	          <a href="{$listEvents}&amp;p={$prevPage}">
	            &#171; <!-- left double arrow -->
	          </a>
	        </xsl:if>
	        <xsl:text> </xsl:text>
	        <xsl:call-template name="listEventsPageNav"/>
	        <xsl:text> </xsl:text>
	        <xsl:choose>
	          <xsl:when test="curPage != numPages">
	            <xsl:variable name="nextPage" select="number(curPage) + 1" />
	            <a href="{$listEvents}&amp;p={$nextPage}">
	              &#187; <!-- right double arrow -->
	            </a>
	          </xsl:when>
	        </xsl:choose>
	      </span>
	    </xsl:if>
	    
    </div>
    
  </xsl:template>

  <xsl:template name="listEventsPageNav">
    <xsl:param name="page">1</xsl:param>
    <xsl:choose>
      <xsl:when test="$page = /bedework/events/curPage">
        <span class="current">
          <xsl:value-of select="$page" />
        </span>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$listEvents}&amp;p={$page}">
          <xsl:value-of select="$page" />
        </a>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:if test="number($page) &lt; number(/bedework/events/numPages)">
      <xsl:call-template name="listEventsPageNav">
        <xsl:with-param name="page" select="number($page)+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
