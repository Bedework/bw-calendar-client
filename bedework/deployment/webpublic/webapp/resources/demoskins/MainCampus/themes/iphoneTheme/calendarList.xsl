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

  <!--==== CALENDAR LIST ====-->
  <xsl:template match="calendars">
    <h1><xsl:copy-of select="$bwStr-Cals-AllCalendars"/></h1>
    <p><xsl:copy-of select="$bwStr-Cals-SelectCalendar"/></p>

    <div id="navlink-back" class="navlink backlink" onclick="gotourl(this,'javascript:history.back()')">
      <xsl:copy-of select="$bwStr-HdBr-Back"/>
    </div>

    <ul class="calendarTree">
      <xsl:apply-templates select="calendar/calendar[calType != 5 and calType != 6 and name != 'calendar']" mode="calTree"/>
    </ul>
  </xsl:template>

  <xsl:template match="calendar" mode="calTree">
    <xsl:variable name="virtualPath"><xsl:call-template name="url-encode"><xsl:with-param name="str">/user<xsl:for-each select="ancestor-or-self::calendar/name">/<xsl:value-of select="."/></xsl:for-each></xsl:with-param></xsl:call-template></xsl:variable>
    <li>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="calType = '0'">folder</xsl:when>
          <xsl:otherwise>calendar</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:variable name="calPath" select="path"/>
      <a href="{$setSelection}&amp;virtualPath={$virtualPath}&amp;setappvar=curCollection({$calPath})" title="view calendar">
        <xsl:value-of select="summary"/>
      </a>
      <xsl:if test="calendar">
        <ul>
          <xsl:apply-templates select="calendar" mode="calTree"/>
        </ul>
      </xsl:if>
    </li>
  </xsl:template>

</xsl:stylesheet>
