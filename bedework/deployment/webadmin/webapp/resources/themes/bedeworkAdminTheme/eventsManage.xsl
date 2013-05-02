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

  <!--++++++++++++++++++ Manage Events List ++++++++++++++++++++-->
  <xsl:template name="eventList">
    <h2><xsl:copy-of select="$bwStr-EvLs-ManageEvents"/></h2>
    <p>
      <xsl:copy-of select="$bwStr-EvLs-SelectEvent"/>
      <input type="button" name="return" value="{$bwStr-EvLs-PageTitle}" onclick="javascript:location.replace('{$event-initAddEvent}')"/>
    </p>

    <div id="bwEventListControls">
      <form name="calForm" id="bwManageEventListControls" method="post" action="{$event-initUpdateEvent}">
        <label for="bwListWidgetStartDate"><xsl:copy-of select="$bwStr-EvLs-StartDate"/></label>
        <input id="bwListWidgetStartDate" class="noFocus" name="start" size="10" onchange="setListDate(this.form);"/>
        <input type="hidden" name="setappvar" id="curListDateHolder"/>
        <input type="hidden" name="limitdays" value="true"/>
        <input type="hidden" name="useDbSearch" value="true"/>
        <span id="daysSetterBox">
          <label for="days"><xsl:copy-of select="$bwStr-EvLs-Days"/></label>
          <xsl:text> </xsl:text>
          <!-- <xsl:value-of select="/bedework/defaultdays"/> -->
          <select id="days" name="days" onchange="setListDate(this.form);">
            <xsl:call-template name="buildListDays"/>
          </select>
          <input type="hidden" id="curListDaysHolder" name="setappvar"/>
        </span>
        
        <!-- This block contains the original Show Active/All toggle.  
             Uncomment this block to use, though it can be slow if working 
             with large very large numbers of events. 
        <xsl:copy-of select="$bwStr-EvLs-Show"/>
        <xsl:copy-of select="/bedework/formElements/form/listAllSwitchFalse/*"/>
        <xsl:copy-of select="$bwStr-EvLs-Active"/>
        <xsl:copy-of select="/bedework/formElements/form/listAllSwitchTrue/*"/>
        <xsl:copy-of select="$bwStr-EvLs-All"/>
        -->
      </form>

      <form name="filterEventsForm"
            id="bwFilterEventsForm"
            action="{$event-initUpdateEvent}">
        <xsl:copy-of select="$bwStr-EvLs-FilterBy"/>
        <select name="setappvar" onchange="this.form.submit();">
          <option value="catFilter(none)"><xsl:copy-of select="$bwStr-EvLs-SelectCategory"/></option>
          <xsl:for-each select="/bedework/events//event/categories//category[generate-id() = generate-id(key('catUid',uid)[1])]">
            <xsl:sort order="ascending" select="value"/>
            <xsl:variable name="uid" select="uid"/>
            <option value="catFilter({$uid})">
              <xsl:if test="/bedework/appvar[key='catFilter']/value = uid">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:value-of select="value"/>
            </option>
          </xsl:for-each>
        </select>
        <input type="hidden" name="start" value="{$curListDate}"/>
        <input type="hidden" name="limitdays" value="true"/>
        <input type="hidden" name="useDbSearch" value="true"/>
        <xsl:if test="/bedework/appvar[key='catFilter'] and /bedework/appvar[key='catFilter']/value != 'none'">
          <input type="submit" value="{$bwStr-EvLs-ClearFilter}" onclick="this.form.setappvar.selectedIndex = 0"/>
        </xsl:if>
      </form>
    </div>
    <xsl:call-template name="eventListCommon"/>
  </xsl:template>
  
  <xsl:template name="buildListDays">
    <xsl:param name="index">1</xsl:param>
    <xsl:variable name="max" select="/bedework/maxdays"/>
    <xsl:if test="number($index) &lt; number($max)">
      <option name="listDays($index)">
        <xsl:if test="$index = $curListDays"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
        <xsl:value-of select="$index"/>
      </option>
      <xsl:call-template name="buildListDays">
        <xsl:with-param name="index"><xsl:value-of select="number($index)+1"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>