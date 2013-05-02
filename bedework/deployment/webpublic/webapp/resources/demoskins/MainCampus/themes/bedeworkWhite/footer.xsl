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

  <xsl:template name="footer">
    <div id="footer">
      <xsl:if test="$showFootForm = 'true'">
        <div id="footForms">
          <form name="skinSelectForm" method="post" action="{$setup}">
            <input type="hidden" name="setappvar" value="summaryMode(details)" />
            <select name="skinPicker" onchange="window.location = this.value">
              <option value="{$setup}&amp;skinNameSticky=default">
                <xsl:copy-of select="$bwStr-Foot-ExampleSkins" />
              </option>
              <option value="{$setup}&amp;skinNameSticky=bedeworkWhite">
                <xsl:copy-of select="$bwStr-Foot-BwWhite" />
              </option>
              <option value="{$setup}&amp;skinNameSticky=bwclassic">
                <xsl:copy-of select="$bwStr-Foot-BwClassic" />
              </option>
              <option value="{$setup}&amp;skinNameSticky=default">
                <xsl:copy-of select="$bwStr-Foot-ResetToCalendarDefault" />
              </option>
              <option value="{$setup}&amp;browserTypeSticky=PDA">
                <xsl:copy-of select="$bwStr-Foot-ForMobileBrowsers" />
              </option>
              <option value="{$feeder}/main/listEvents.do?skinName=list-rss&amp;days=3">
                <xsl:copy-of select="$bwStr-Foot-RSSNext3Days" />
              </option>
              <option value="{$feeder}/main/listEvents.do?skinName=list-json&amp;days=3&amp;contentType=text/javascript&amp;contentName=bedework.js">
                <xsl:copy-of select="$bwStr-Foot-JavascriptNext3Days" />
              </option>
              <option value="{$setViewPeriod}&amp;viewType=todayView&amp;skinNameSticky=videocal">
                <xsl:copy-of select="$bwStr-Foot-VideoFeed" />
              </option>
            </select>
          </form>
        </div>
      </xsl:if>
      <xsl:call-template name="footerText"/>
    </div>
  </xsl:template>

</xsl:stylesheet>
