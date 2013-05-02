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
  
  <!--==== BANNER and MENU TABS  ====-->

  <xsl:template name="header">
    <div id="header">
      <a href="/bedework/">
        <img id="logo"
            alt="logo"
            src="{$resourcesRoot}/images/bedeworkAdminLogo.gif"
            width="217"
            height="40"
            border="0"/>
      </a>
      <h1>
        <xsl:copy-of select="$bwStr-Head-BedeworkPubEventsAdmin"/>
      </h1>
    </div>
    <xsl:call-template name="messagesAndErrors"/>
    <table id="statusBarTable">
      <tr>
        <td class="leftCell">
          <xsl:copy-of select="$bwStr-Head-CalendarSuite"/><xsl:text> </xsl:text>
          <span class="status">
            <xsl:choose>
              <xsl:when test="/bedework/currentCalSuite/name">
                <xsl:value-of select="/bedework/currentCalSuite/name"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="$bwStr-Head-None"/>
              </xsl:otherwise>
            </xsl:choose>
          </span>
          <xsl:text> </xsl:text>
        </td>
        <xsl:if test="/bedework/userInfo/user">
          <td class="rightCell">
              <span id="groupDisplay">
                <xsl:copy-of select="$bwStr-Head-Group"/><xsl:text> </xsl:text>
                <span class="status">
                  <xsl:choose>
                    <xsl:when test="/bedework/userInfo/group">
                      <xsl:value-of select="/bedework/userInfo/group"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:copy-of select="$bwStr-Head-None"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </span>
                <xsl:text> </xsl:text>
                <xsl:if test="(/bedework/userInfo/group and /bedework/userInfo/oneGroup = 'false') or /bedework/userInfo/superUser = 'true'">
                  <a href="{$admingroup-switch}" class="fieldInfo"><xsl:copy-of select="$bwStr-Head-Change"/></a>
                </xsl:if>
                <xsl:text> </xsl:text>
              </span>
            <xsl:copy-of select="$bwStr-Head-LoggedInAs"/><xsl:text> </xsl:text>
            <span class="status">
              <xsl:value-of select="/bedework/userInfo/currentUser"/>
            </span>
            <xsl:text> </xsl:text>
            <a href="{$logout}" id="bwLogoutButton" class="fieldInfo"><xsl:copy-of select="$bwStr-Head-LogOut"/></a>
          </td>
        </xsl:if>
      </tr>
    </table>

    <ul id="bwAdminMenu">
      <li>
        <xsl:if test="/bedework/tab = 'main'">
          <xsl:attribute name="class">selected</xsl:attribute>
        </xsl:if>
        <a href="{$setup}&amp;listAllEvents=false"><xsl:copy-of select="$bwStr-Head-MainMenu"/></a>
      </li>
      <li>
        <xsl:if test="/bedework/tab = 'pending'">
          <xsl:attribute name="class">selected</xsl:attribute>
        </xsl:if>
        <a href="{$initPendingTab}&amp;calPath={$submissionsRootEncoded}&amp;listAllEvents=true&amp;useDbSearch=true"><xsl:copy-of select="$bwStr-Head-PendingEvents"/></a>
      </li>
      <xsl:if test="/bedework/currentCalSuite/group = /bedework/userInfo/group">
        <xsl:if test="/bedework/currentCalSuite/currentAccess/current-user-privilege-set/privilege/write or /bedework/userInfo/superUser = 'true'">
          <li>
            <xsl:if test="/bedework/tab = 'calsuite'">
              <xsl:attribute name="class">selected</xsl:attribute>
            </xsl:if>
            <a href="{$showCalsuiteTab}"><xsl:copy-of select="$bwStr-Head-CalendarSuite"/></a>
          </li>
        </xsl:if>
      </xsl:if>
      <xsl:if test="/bedework/userInfo/superUser='true'">
        <li>
          <xsl:if test="/bedework/tab = 'users'">
            <xsl:attribute name="class">selected</xsl:attribute>
          </xsl:if>
          <a href="{$showUsersTab}"><xsl:copy-of select="$bwStr-Head-Users"/></a>
        </li>
        <li>
          <xsl:if test="/bedework/tab = 'system'">
            <xsl:attribute name="class">selected</xsl:attribute>
          </xsl:if>
          <a href="{$showSystemTab}"><xsl:copy-of select="$bwStr-Head-System"/></a>
        </li>
      </xsl:if>
    </ul>
  </xsl:template>
  
</xsl:stylesheet>