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

  <!-- === Navigation == -->
  <xsl:template name="navigation">
    <div id="nav">
      <div class="navlink" id="navlink-prev" onclick="gotourl(this,'{$setViewPeriod}&amp;date={$prevdate}');">
        &lt;<xsl:copy-of select="$bwStr-Srch-Prev"/>
      </div>
      <div class="navlink" id="navlink-today" onclick="gotourl(this,'{$setViewPeriod}&amp;viewType=todayView&amp;date={$curdate}');">
        <xsl:copy-of select="$bwStr-Tabs-Today"/>
      </div>
      <div class="navlink" id="navlink-day" onclick="gotourl(this,'{$setViewPeriod}&amp;viewType=dayView&amp;date={$curdate}');">
        <xsl:if test="/bedework/periodname='Day'">
          <xsl:attribute name="class">navlink selected</xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="$bwStr-Tabs-Day"/>
      </div>
      <div class="navlink" id="navlink-week" onclick="gotourl(this,'{$setViewPeriod}&amp;viewType=weekView&amp;date={$curdate}');">
        <xsl:if test="/bedework/periodname='Week'">
          <xsl:attribute name="class">navlink selected</xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="$bwStr-Tabs-Week"/>
      </div>
      <div class="navlink" id="navlink-month" onclick="gotourl(this,'{$setViewPeriod}&amp;viewType=monthView&amp;date={$curdate}');">
        <xsl:if test="/bedework/periodname='Month'">
          <xsl:attribute name="class">navlink selected</xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="$bwStr-Tabs-Month"/>
      </div>
      <div class="navlink" id="navlink-next" onclick="gotourl(this,'{$setViewPeriod}&amp;date={$nextdate}');">
        <xsl:copy-of select="$bwStr-Srch-Next"/>&gt;
      </div>

      <!-- display information about the current selection state if not default -->
      <xsl:choose>
        <xsl:when test="/bedework/selectionState/selectionType = 'collections'">
          <div id="stateMsg">
            <xsl:copy-of select="$bwStr-LsVw-DispEventsForCal"/>
            <xsl:text> </xsl:text>
            <span class="displayFilterName">
              <xsl:call-template name="substring-afterLastInstanceOf">
                <xsl:with-param name="string" select="/bedework/selectionState/collection/virtualpath"/>
                <xsl:with-param name="char">/</xsl:with-param>
              </xsl:call-template>
            </span><xsl:text> </xsl:text>
            <span id="allView" class="navlink" onclick="gotourl(this,'{$setSelection}')"><xsl:copy-of select="$bwStr-LsVw-ShowAll"/></span>
          </div>
        </xsl:when>
        <xsl:when test="/bedework/selectionState/view/name != 'All'">
          <div id="stateMsg">
            <xsl:copy-of select="$bwStr-LsVw-DispEventsForView"/>
            <xsl:text> </xsl:text>
            <span class="displayFilterName">
              <xsl:value-of select="/bedework/selectionState/view/name"/>
            </span><xsl:text> </xsl:text>
            <span id="allView" class="navlink" onclick="gotourl(this,'{$setSelection}')"><xsl:copy-of select="$bwStr-LsVw-ShowAll"/></span>
          </div>
        </xsl:when>
      </xsl:choose>

    </div>
  </xsl:template>

</xsl:stylesheet>
