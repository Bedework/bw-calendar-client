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

  <xsl:template name="featuredEvents">
    <xsl:if test="$featuredEventsAlwaysOn = 'true' or
                 ($featuredEventsForEventDisplay = 'true' and /bedework/page = 'event') or
                 ($featuredEventsForCalList = 'true' and /bedework/page = 'calendarList') or
                 (/bedework/page = 'eventscalendar' and (
                   ($featuredEventsForDay = 'true' and /bedework/periodname = 'Day') or
                   ($featuredEventsForWeek = 'true' and /bedework/periodname = 'Week') or
                   ($featuredEventsForMonth = 'true' and /bedework/periodname = 'Month') or
                   ($featuredEventsForYear = 'true' and /bedework/periodname = 'Year')))">
      <div id="feature">
        <!-- grab the root of the FeaturedEvent.xml document (/image[position() &lt; 4])-->
        <xsl:apply-templates select="document('../../themes/bedeworkWhite/featured/FeaturedEvent.xml')/featuredEvents"/>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="featuredEvents">
    <xsl:choose>
      <xsl:when test="featuresOn = 'true'">
        <xsl:choose>
          <xsl:when test="singleMode = 'false'"><!-- triptych -->
            <xsl:apply-templates select="features/group/image"/>
          </xsl:when>
          <xsl:otherwise><!-- single pane -->
            <xsl:apply-templates select="features/single/image">
              <xsl:with-param name="singleMode">true</xsl:with-param>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise><!-- use generic defaults -->
        <xsl:apply-templates select="generics/group/image">
          <xsl:with-param name="isGeneric">true</xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="image">
    <xsl:param name="singleMode">false</xsl:param>
    <xsl:param name="isGeneric">false</xsl:param>
    <xsl:variable name="imgPrefix"><xsl:choose><xsl:when test="not(starts-with(name,'http') or starts-with(name,'/'))"><xsl:value-of select="$resourcesRoot"/>/featured/</xsl:when></xsl:choose></xsl:variable>
    <xsl:choose>
      <xsl:when test="link = '' or $isGeneric = 'true'">
        <img width="240" height="189">
          <xsl:attribute name="src"><xsl:value-of select="$imgPrefix"/><xsl:value-of select="name"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="toolTip"/></xsl:attribute>
          <xsl:if test="$singleMode = 'true'">
            <xsl:attribute name="width">725</xsl:attribute>
          </xsl:if> 
        </img>
      </xsl:when>
      <xsl:otherwise>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
          <img width="240" height="189">
            <xsl:attribute name="src"><xsl:value-of select="$imgPrefix"/><xsl:value-of select="name"/></xsl:attribute>
            <xsl:attribute name="alt"><xsl:value-of select="toolTip"/></xsl:attribute>
            <xsl:if test="$singleMode = 'true'">
	            <xsl:attribute name="width">725</xsl:attribute>
	          </xsl:if> 
          </img>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
