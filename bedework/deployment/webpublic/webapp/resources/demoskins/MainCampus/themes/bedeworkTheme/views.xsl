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

  <!-- View List -->
  <xsl:template name="viewList">
    <div class="secondaryColHeader">
      <h3><xsl:copy-of select="$bwStr-LCol-CalendarViews"/></h3>
    </div>
    <ul class="viewList">
      <xsl:for-each select="/bedework/views/view">
        <xsl:variable name="viewName" select="name/text()"/>
        <li>
          <a href="{$setSelection}&amp;viewName={$viewName}">
            <xsl:attribute name="href">
              <xsl:choose>
                <xsl:when test="/bedework/page = 'eventList'"><xsl:value-of select="$setSelectionList"/>&amp;viewName=<xsl:value-of select="$viewName"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$setSelection"/>&amp;viewName=<xsl:value-of select="$viewName"/></xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
            <xsl:if test="$viewName = (/bedework/selectionState/view/name)">
              <xsl:attribute name="class">current</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$viewName"/>
          </a>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

</xsl:stylesheet>
