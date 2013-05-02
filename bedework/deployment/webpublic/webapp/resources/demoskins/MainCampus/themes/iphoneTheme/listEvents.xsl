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

  <!--==== LIST VIEW  (for day, week, and month) ====-->
  <xsl:template name="listView">
    <div id="eventList">
      <xsl:choose>
        <xsl:when test="not(/bedework/eventscalendar/year/month/week/day/event)">
          <p class="noEvents">
            <xsl:choose>
              <xsl:when test="/bedework/periodname='Year'">
                <xsl:value-of select="substring(/bedework/firstday/date,1,4)"/>
              </xsl:when>
              <xsl:when test="/bedework/periodname='Month'">
                <xsl:value-of select="/bedework/firstday/monthname"/>, <xsl:value-of select="substring(/bedework/firstday/date,1,4)"/>
              </xsl:when>
              <xsl:when test="/bedework/periodname='Week'">
                <xsl:copy-of select="$bwStr-Navi-WeekOf"/><xsl:text> </xsl:text><xsl:value-of select="substring-after(/bedework/firstday/longdate,', ')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="/bedework/firstday/longdate"/>
              </xsl:otherwise>
            </xsl:choose><br/>
            <xsl:copy-of select="$bwStr-LsEv-NoEventsToDisplay"/>
          </p>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="/bedework/eventscalendar/year/month/week/day[count(event)!=0]">

            <xsl:if test="/bedework/periodname='Week' or /bedework/periodname='Month' or /bedework/periodname=''">
              <h3>
                <xsl:attribute name="id">dayMarker-<xsl:value-of select="date"/></xsl:attribute>
                <xsl:attribute name="onclick">gotourl(this,'<xsl:value-of select="$setViewPeriod"/>&amp;viewType=dayView&amp;date=<xsl:value-of select="date"/>')</xsl:attribute>
                <xsl:value-of select="name"/>, <xsl:value-of select="longdate"/>
              </h3>
            </xsl:if>

            <ul>
              <xsl:for-each select="event">
                <li>
                  <xsl:attribute name="id"><xsl:value-of select="guid"/></xsl:attribute>
                  <xsl:attribute name="onclick">gotourl(this,'<xsl:value-of select="$eventView"/>&amp;calPath=<xsl:value-of select="calendar/encodedPath"/>&amp;guid=<xsl:value-of select="guid"/>&amp;recurrenceId=<xsl:value-of select="recurrenceId"/>')</xsl:attribute>
                  <xsl:choose>
                    <xsl:when test="position() mod 2 = 0">
                      <xsl:attribute name="class">even</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="class">odd</xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:value-of select="start/dayname"/>, <xsl:value-of select="start/longdate"/>
                  <xsl:choose>
                    <xsl:when test="start/allday = 'true'">
                      <xsl:text> </xsl:text>
                      <xsl:copy-of select="$bwStr-LsVw-AllDay"/>
                    </xsl:when>
                    <xsl:otherwise>
                       <xsl:text> </xsl:text>
                       <xsl:copy-of select="$bwStr-LsVw-At"/>
                       <xsl:text> </xsl:text>
                      <xsl:value-of select="start/time"/>
                    </xsl:otherwise>
                  </xsl:choose>
                  <br/>
                  <strong>
                    <xsl:if test="status='CANCELLED'"><xsl:copy-of select="$bwStr-LsVw-Canceled"/><xsl:text> </xsl:text></xsl:if>
                    <xsl:if test="status='TENTATIVE'"><xsl:copy-of select="$bwStr-LsEv-Tentative"/><xsl:text> </xsl:text></xsl:if>
                    <xsl:value-of select="summary"/>
                  </strong>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

</xsl:stylesheet>
