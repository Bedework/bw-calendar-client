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

  <xsl:template name="leftColumn">
    <div class="left_column">
      <xsl:call-template name="jsDateSelectionCal" />
      <div class="clear">&#160;</div>
      <xsl:if test="$useViewsNav = 'true'">
        <xsl:call-template name="viewList" />
      </xsl:if>
      <xsl:if test="$useSubscriptionsNav = 'true'">
        <xsl:call-template name="subscriptionsTree" />
      </xsl:if>
      <xsl:call-template name="displaySideBar" />
    </div>
  </xsl:template>

  <!-- Date Selection Calendar (javascript widget) -->
  <xsl:template name="jsDateSelectionCal">
    <div id="jsNavCal" class="calContainer">
      <xsl:call-template name="dateSelectForm" />
      <p>
        <xsl:copy-of select="$bwStr-LCol-JsMessage"/>
      </p>
    </div>
  </xsl:template>

  <!--============= Display Side Bar ======-->
  <xsl:template name="displaySideBar">
    <div class="sideBarContainer">
      <h4><xsl:copy-of select="$bwStr-LCol-FilterOnCalendars"/></h4>
      <ul class="sideLinks">
        <li>
          <a href="{$fetchPublicCalendars}">
            <xsl:copy-of select="$bwStr-LCol-ViewAllCalendars"/>
          </a>
        </li>
        <li>
		      <a>
		        <xsl:attribute name="href">
		          <xsl:choose>
		            <xsl:when test="$embedUrlBuilder = 'true'"><xsl:value-of select="$showPage"/>&amp;setappvar=page(urlbuilder)</xsl:when>
		            <xsl:otherwise><xsl:value-of select="$urlbuilder"/></xsl:otherwise>
		          </xsl:choose>
		        </xsl:attribute>
		        <xsl:attribute name="title"><xsl:value-of select="$bwStr-LCol-FeedsAndWidgets"/></xsl:attribute>
            <xsl:copy-of select="$bwStr-LCol-FeedsAndWidgets"/>
          </a>
        </li>
      </ul>
      <xsl:call-template name="leftColumnText"/>
    </div>
  </xsl:template>

</xsl:stylesheet>
