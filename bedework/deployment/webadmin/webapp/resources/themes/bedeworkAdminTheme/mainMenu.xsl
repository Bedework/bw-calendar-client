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
  
  <!--+++++++++++++++ Main Menu Tab ++++++++++++++++++++-->
  <xsl:template name="mainMenu">

    <div class="notes">
      <xsl:if test="/bedework/userInfo/superUser = 'true'">
        <p class="note">
          <xsl:copy-of select="$bwStr-MMnu-LoggedInAs"/>
        </p>
      </xsl:if>
    </div>

    <table id="mainMenu">
      <tr>
        <td>
          <a id="addEventLink" href="{$event-initAddEvent}">
            <xsl:if test="not(/bedework/currentCalSuite/name)">
              <xsl:attribute name="onclick">alert("<xsl:copy-of select="$bwStr-MMnu-YouMustBeOperating"/>");return false;</xsl:attribute>
            </xsl:if>
            <img src="{$resourcesRoot}/images/bwAdminAddEventIcon.jpg" width="140" height="140" alt="Add Event" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-AddEvent"/>
          </a>
        </td>
        <td>
          <a id="addContactLink" href="{$contact-initAdd}">
            <img src="{$resourcesRoot}/images/bwAdminAddContactIcon.jpg" width="100" height="100" alt="Add Event" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-AddContact"/>
          </a>
        </td>
        <td>
          <a id="addLocationLink" href="{$location-initAdd}">
            <img src="{$resourcesRoot}/images/bwAdminAddLocationIcon.jpg" width="100" height="100" alt="Add Event" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-AddLocation"/>
          </a>
        </td>
        <xsl:if test="/bedework/currentCalSuite/group = /bedework/userInfo/group">
          <xsl:if test="/bedework/currentCalSuite/currentAccess/current-user-privilege-set/privilege/write or /bedework/userInfo/superUser = 'true'">
            <!--
              Category management is a  super-user and calsuite admin feature;
              Categories underly much of the new single calendar and filtering model.-->
            <td>
              <a id="addCategoryLink" href="{$category-initAdd}">
                <img src="{$resourcesRoot}/images/bwAdminAddCategoryIcon.jpg" width="100" height="100" alt="Add Event" border="0"/>
                <br/><xsl:copy-of select="$bwStr-MMnu-AddCategory"/>
              </a>
            </td>
          </xsl:if>
        </xsl:if>
      </tr>
      <tr>
        <td>
          <a href="{$event-initUpdateEvent}">
            <xsl:attribute name="href"><xsl:value-of select="$event-initUpdateEvent"/>&amp;useDbSearch=true&amp;start=<xsl:value-of select="$curListDate"/>&amp;days=<xsl:value-of select="$curListDays"/>&amp;limitdays=true</xsl:attribute>
            <xsl:if test="not(/bedework/currentCalSuite/name)">
              <xsl:attribute name="onclick">alert("<xsl:copy-of select="$bwStr-MMnu-YouMustBeOperating"/>");return false;</xsl:attribute>
            </xsl:if>
            <img src="{$resourcesRoot}/images/bwAdminManageEventsIcon.jpg" width="100" height="73" alt="Manage Events" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-ManageEvents"/>
          </a>
        </td>
        <td>
          <a href="{$contact-initUpdate}">
            <img src="{$resourcesRoot}/images/bwAdminManageContactsIcon.jpg" width="100" height="73" alt="Manage Contacts" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-ManageContacts"/>
          </a>
        </td>
        <td>
          <a href="{$location-initUpdate}">
            <img src="{$resourcesRoot}/images/bwAdminManageLocsIcon.jpg" width="100" height="73" alt="Manage Locations" border="0"/>
            <br/><xsl:copy-of select="$bwStr-MMnu-ManageLocations"/>
          </a>
        </td>
        <xsl:if test="/bedework/currentCalSuite/group = /bedework/userInfo/group">
          <xsl:if test="/bedework/currentCalSuite/currentAccess/current-user-privilege-set/privilege/write or /bedework/userInfo/superUser = 'true'">
            <!--
              Category management is a super-user and calsuite admin feature;
              Categories underly much of the new single calendar and filtering model.-->
            <td>
              <a href="{$category-initUpdate}">
                <img src="{$resourcesRoot}/images/bwAdminManageCatsIcon.jpg" width="100" height="73" alt="Manage Categories" border="0"/>
                <br/><xsl:copy-of select="$bwStr-MMnu-ManageCategories"/>
              </a>
            </td>
          </xsl:if>
        </xsl:if>
      </tr>
    </table>

    <div id="mainMenuEventSearch">
      <h4 class="menuTitle"><xsl:copy-of select="$bwStr-MMnu-EventSearch"/></h4>
      <form name="searchForm" method="post" action="{$search}" id="searchForm">
        <input type="text" name="query" size="30">
          <xsl:attribute name="value"><xsl:value-of select="/bedework/searchResults/query"/></xsl:attribute>
        </input>
        <input type="submit" name="submit" value="{$bwStr-MMnu-Go}"/>
        <div id="searchFields">
          <xsl:copy-of select="$bwStr-MMnu-Limit"/>
          <input type="radio" name="searchLimits" id="bwSearchFromToday" value="fromToday" checked="checked"/>
          <label for="bwSearchFromToday">
            <xsl:copy-of select="$bwStr-MMnu-TodayForward"/>
          </label>
          <input type="radio" name="searchLimits" id="bwSearchPastDates" value="beforeToday"/>
          <label for="bwSearchPastDates">
            <xsl:copy-of select="$bwStr-MMnu-PastDates"/>
          </label>
          <input type="radio" name="searchLimits" id="bwSearchAllDates" value="none"/>
          <label for="bwSearchAllDates">
            <xsl:copy-of select="$bwStr-MMnu-AddDates"/>
          </label>
        </div>
      </form>
    </div>
  </xsl:template>
  
</xsl:stylesheet>