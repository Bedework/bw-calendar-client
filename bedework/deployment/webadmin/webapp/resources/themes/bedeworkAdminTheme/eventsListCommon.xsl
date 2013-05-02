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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output
     method="html"
     indent="no"
     media-type="text/html"
     doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
     doctype-system="http://www.w3.org/TR/html4/loose.dtd"
     standalone="yes"
     omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  
  <!--++++++++++++++++++ Events List Common ++++++++++++++++++++-->
  <!--            included in most event listings               -->
  
  <xsl:template name="eventListCommon">
    <xsl:param name="pending">false</xsl:param>
    <table id="commonListTable">
      <tr>
        <th><xsl:copy-of select="$bwStr-EvLC-Title"/></th>
        <xsl:if test="$pending = 'true'">
          <th><xsl:copy-of select="$bwStr-EvLC-ClaimedBy"/></th>
        </xsl:if>
        <th><xsl:copy-of select="$bwStr-EvLC-Start"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-End"/></th>
        <th>
          <xsl:if test="$pending = 'true'"><xsl:copy-of select="$bwStr-EvLC-Suggested"/><xsl:text> </xsl:text></xsl:if>
          <xsl:copy-of select="$bwStr-EvLC-TopicalAreas"/>
        </th>
        <xsl:if test="$pending = 'false'">
          <th><xsl:copy-of select="$bwStr-EvLC-Categories"/></th>
        </xsl:if>
        <th><xsl:copy-of select="$bwStr-EvLC-Description"/></th>
      </tr>

      <xsl:choose>
        <xsl:when test="/bedework/appvar[key='catFilter'] and /bedework/appvar[key='catFilter']/value != 'none'">
          <xsl:apply-templates select="/bedework/events/event[categories//category/uid = /bedework/appvar[key='catFilter']/value]" mode="eventListCommon">
            <xsl:with-param name="pending"><xsl:value-of select="$pending"/></xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="/bedework/events/event" mode="eventListCommon">
            <xsl:with-param name="pending"><xsl:value-of select="$pending"/></xsl:with-param>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>

    </table>
  </xsl:template>

  <xsl:template match="event" mode="eventListCommon">
    <xsl:param name="pending">false</xsl:param>
    <xsl:variable name="calPath" select="calendar/encodedPath"/>
    <xsl:variable name="guid" select="guid"/>
    <xsl:variable name="recurrenceId" select="recurrenceId"/>
    <tr>
      <xsl:if test="position() mod 2 = 0"><xsl:attribute name="class">even</xsl:attribute></xsl:if>
      <xsl:if test="$pending = 'true' and not(xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT)">
        <xsl:attribute name="class">highlight</xsl:attribute>
      </xsl:if>
      <xsl:if test="status = 'TENTATIVE'">
        <xsl:attribute name="class">tentative</xsl:attribute>
      </xsl:if>
      <xsl:if test="status = 'CANCELLED'">
        <xsl:attribute name="class">cancelled</xsl:attribute>
      </xsl:if>
      <td>
        <xsl:choose>
          <xsl:when test="$pending = 'true'">
            <xsl:choose>
              <xsl:when test="xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT and not(xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT/values/text = /bedework/userInfo/group)">
                <xsl:choose>
                  <xsl:when test="summary != ''">
                    <xsl:value-of select="summary"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <em><xsl:copy-of select="$bwStr-EvLC-NoTitle"/></em>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <a>
                  <xsl:choose>
                    <xsl:when test="recurrenceId != ''">
                      <!-- recurrence instances should be updated like normal events - only master events should be published -->
                      <xsl:attribute name="href"><xsl:value-of select="$event-fetchForUpdate"/>&amp;calPath=<xsl:value-of select="$calPath"/>&amp;guid=<xsl:value-of select="$guid"/>&amp;recurrenceId=<xsl:value-of select="$recurrenceId"/></xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="href"><xsl:value-of select="$event-fetchForUpdatePending"/>&amp;calPath=<xsl:value-of select="$calPath"/>&amp;guid=<xsl:value-of select="$guid"/>&amp;recurrenceId=<xsl:value-of select="$recurrenceId"/></xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:choose>
                    <xsl:when test="summary != ''">
                      <xsl:value-of select="summary"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <em><xsl:copy-of select="$bwStr-EvLC-NoTitle"/></em>
                    </xsl:otherwise>
                  </xsl:choose>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="status = 'CANCELLED'"><strong><xsl:copy-of select="$bwStr-EvLC-Cancelled"/></strong><br/></xsl:when>
              <xsl:when test="status = 'TENTATIVE'"><xsl:copy-of select="$bwStr-EvLC-Tentative"/><br/></xsl:when>
            </xsl:choose>
            <a href="{$event-fetchForUpdate}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
              <xsl:choose>
                <xsl:when test="summary != ''">
                  <xsl:value-of select="summary"/>
                </xsl:when>
                <xsl:otherwise>
                  <em><xsl:copy-of select="$bwStr-EvLC-NoTitle"/></em>
                </xsl:otherwise>
              </xsl:choose>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <xsl:if test="$pending = 'true'">
        <xsl:choose>
          <xsl:when test="xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT">
            <td>
              <xsl:value-of select="xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT/values/text"/>
              <xsl:text> </xsl:text>
              (<xsl:value-of select="xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT/parameters/X-BEDEWORK-SUBMISSION-CLAIMANT-USER"/>)
            </td>
          </xsl:when>
          <xsl:otherwise>
            <td class="unclaimed"><xsl:copy-of select="$bwStr-EvLC-Unclaimed"/></td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <td class="date">
        <xsl:value-of select="start/shortdate"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="start/allday = 'false'">
            <xsl:value-of select="start/time"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$bwStr-AEEF-AllDay"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td class="date">
        <xsl:value-of select="end/shortdate"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="start/allday = 'false'">
            <xsl:value-of select="end/time"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$bwStr-AEEF-AllDay"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td class="calcat">
        <xsl:choose>
          <xsl:when test="$pending = 'true'">
            <xsl:for-each select="xproperties/X-BEDEWORK-SUBMIT-ALIAS">
              <xsl:value-of select="parameters/X-BEDEWORK-PARAM-DISPLAYNAME"/><br/>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="xproperties/X-BEDEWORK-ALIAS[contains(values/text,/bedework/currentCalSuite/resourcesHome)]">
              <xsl:value-of select="substring-after(values/text,/bedework/currentCalSuite/resourcesHome)"/><br/>
            </xsl:for-each>
            <xsl:if test="xproperties/X-BEDEWORK-ALIAS[not(contains(values/text,/bedework/currentCalSuite/resourcesHome))]">
              <xsl:variable name="tagsId">bwTags-<xsl:value-of select="guid"/></xsl:variable>
              <div class="bwEventListOtherGroupTags">
                <strong><xsl:copy-of select="$bwStr-EvLC-ThisEventCrossTagged"/></strong><br/>
                <input type="checkbox" name="tagsToggle" value="" onclick="toggleVisibility('{$tagsId}','bwOtherTags')"/>
                <xsl:copy-of select="$bwStr-EvLC-ShowTagsByOtherGroups"/>
                <div id="{$tagsId}" class="invisible">
                  <xsl:for-each select="xproperties/X-BEDEWORK-ALIAS[not(contains(values/text,/bedework/currentCalSuite/resourcesHome))]">
                    <xsl:value-of select="values/text"/><br/>
                  </xsl:for-each>
                </div>
              </div>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <xsl:if test="$pending = 'false'">
        <td class="calcat">
          <xsl:for-each select="categories/category">
            <xsl:value-of select="value"/><br/>
          </xsl:for-each>
        </td>
      </xsl:if>
      <td>
        <xsl:value-of select="description"/>
        <xsl:if test="recurring = 'true' or recurrenceId != ''">
          <div class="recurrenceEditLinks">
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-EvLC-RecurringEventEdit"/>
            <xsl:choose>
              <xsl:when test="$pending = 'true'">
                <!-- only master events can be published -->
                <a href="{$event-fetchForUpdatePending}&amp;calPath={$calPath}&amp;guid={$guid}">
                  <xsl:copy-of select="$bwStr-EvLC-Master"/>
                </a> |
              </xsl:when>
              <xsl:otherwise>
                <a href="{$event-fetchForUpdate}&amp;calPath={$calPath}&amp;guid={$guid}">
                  <xsl:copy-of select="$bwStr-EvLC-Master"/>
                </a> |
              </xsl:otherwise>
            </xsl:choose>
            <!-- recurrence instances can only be edited -->
            <a href="{$event-fetchForUpdate}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
              <xsl:copy-of select="$bwStr-EvLC-Instance"/>
            </a>
          </div>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
  
</xsl:stylesheet>