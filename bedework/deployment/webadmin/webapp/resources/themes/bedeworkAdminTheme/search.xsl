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
  
  <!--++++ SEARCH ++++-->
  <!-- templates: 
         - searchResult
         - searchResultPageNav
   -->
  <xsl:template name="searchResult">
    <h2 class="bwStatusConfirmed">
      <div id="searchFilter">
        <form name="searchForm" method="post" action="{$search}">
          <xsl:copy-of select="$bwStr-Srch-Search"/>Search:
          <input type="text" name="query" size="15">
            <xsl:attribute name="value"><xsl:value-of select="/bedework/searchResults/query"/></xsl:attribute>
          </input>
          <input type="submit" name="submit" value="{$bwStr-Srch-Go}"/>
          <xsl:copy-of select="$bwStr-Srch-Limit"/>
          
          <input type="radio" name="searchLimits" id="searchFromToday" value="fromToday">
            <xsl:if test="/bedework/searchResults/searchLimits = 'fromToday'">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
          <label for="searchFromToday"><xsl:copy-of select="$bwStr-Srch-TodayForward"/></label>
          
          <input type="radio" name="searchLimits" id="searchBeforeToday" value="beforeToday">
            <xsl:if test="/bedework/searchResults/searchLimits = 'beforeToday'">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
          <label for="searchBeforeToday"><xsl:copy-of select="$bwStr-Srch-PastDates"/></label>
          
          <input type="radio" name="searchLimits" id="searchNoLimits" value="none">
            <xsl:if test="/bedework/searchResults/searchLimits = 'none'">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
          <label for="searchNoLimits"><xsl:copy-of select="$bwStr-Srch-AllDates"/></label>
          
        </form>
      </div>
      <xsl:copy-of select="$bwStr-Srch-SearchResult"/>
    </h2>
    <table id="searchTable" cellpadding="0" cellspacing="0">
      <tr>
        <th colspan="5">
          <xsl:if test="/bedework/searchResults/numPages &gt; 1">
            <xsl:variable name="curPage" select="/bedework/searchResults/curPage"/>
            <div id="searchPageForm">
              <xsl:copy-of select="$bwStr-Srch-Page"/>
              <xsl:if test="/bedework/searchResults/curPage != 1">
                <xsl:variable name="prevPage" select="number($curPage) - 1"/>
                &lt;<a href="{$search-next}&amp;pageNum={$prevPage}"><xsl:copy-of select="$bwStr-Srch-Prev"/></a>
              </xsl:if>
              <xsl:text> </xsl:text>

              <xsl:call-template name="searchResultPageNav">
                <xsl:with-param name="page">
                  <xsl:choose>
                    <xsl:when test="number($curPage) - 10 &lt; 1">1</xsl:when>
                    <xsl:otherwise><xsl:value-of select="number($curPage) - 6"/></xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
              </xsl:call-template>

              <xsl:text> </xsl:text>
              <xsl:choose>
                <xsl:when test="$curPage != /bedework/searchResults/numPages">
                  <xsl:variable name="nextPage" select="number($curPage) + 1"/>
                  <a href="{$search-next}&amp;pageNum={$nextPage}"><xsl:copy-of select="$bwStr-Srch-Next"/></a>&gt;
                </xsl:when>
                <xsl:otherwise>
                  <span class="hidden"><xsl:copy-of select="$bwStr-Srch-Next"/>&gt;</span><!-- occupy the space to keep the navigation from moving around -->
                </xsl:otherwise>
              </xsl:choose>
            </div>
          </xsl:if>
          <strong><xsl:value-of select="/bedework/searchResults/resultSize"/></strong>
          <xsl:text> </xsl:text>
          <xsl:copy-of select="$bwStr-Srch-ResultReturnedFor"/><xsl:text> </xsl:text>
          <strong><em><xsl:value-of select="/bedework/searchResults/query"/></em></strong>
        </th>
      </tr>
      <xsl:if test="/bedework/searchResults/searchResult">
        <tr class="fieldNames">
          <td>
            <xsl:copy-of select="$bwStr-Srch-Relevance"/>
          </td>
          <td>
            <xsl:copy-of select="$bwStr-Srch-Title"/>
          </td>
          <td>
            <xsl:copy-of select="$bwStr-Srch-DateAndTime"/>
          </td>
          <!-- <td>
            topical areas
          </td>-->
          <td>
            <xsl:copy-of select="$bwStr-Srch-Location"/>
          </td>
        </tr>
      </xsl:if>
      <xsl:for-each select="/bedework/searchResults/searchResult">
        <xsl:variable name="calPath" select="event/calendar/encodedPath"/>
        <xsl:variable name="guid" select="event/guid"/>
        <xsl:variable name="recurrenceId" select="event/recurrenceId"/>
        <tr>
          <td class="relevance">
            <xsl:value-of select="ceiling(number(score)*100)"/>%
            <img src="{$resourcesRoot}/images/spacer.gif" height="4" class="searchRelevance">
              <xsl:attribute name="width"><xsl:value-of select="ceiling((number(score)*100) div 1.5)"/></xsl:attribute>
            </img>
          </td>
          <td>
            <a href="{$event-fetchForDisplay}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
              <xsl:value-of select="event/summary"/>
              <xsl:if test="event/summary = ''"><em><xsl:copy-of select="$bwStr-Srch-NoTitle"/></em></xsl:if>
            </a>
          </td>
          <td>
            <xsl:value-of select="event/start/longdate"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="event/start/time"/>
            <xsl:choose>
              <xsl:when test="event/start/longdate != event/end/longdate">
                - <xsl:value-of select="event/end/longdate"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="event/end/time"/>
              </xsl:when>
              <xsl:when test="event/start/time != event/end/time">
                - <xsl:value-of select="event/end/time"/>
              </xsl:when>
            </xsl:choose>
          </td>
          <!--
          <td>
            <xsl:for-each select="xproperties/X-BEDEWORK-ALIAS">
              <xsl:call-template name="substring-afterLastInstanceOf">
                <xsl:with-param name="string" select="values/text"/>
                <xsl:with-param name="char">/</xsl:with-param>
              </xsl:call-template><br/>
            </xsl:for-each>
          </td>-->
          <td>
            <xsl:value-of select="event/location/address"/>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template name="searchResultPageNav">
    <xsl:param name="page">1</xsl:param>
    <xsl:variable name="curPage" select="/bedework/searchResults/curPage"/>
    <xsl:variable name="numPages" select="/bedework/searchResults/numPages"/>
    <xsl:variable name="endPage">
      <xsl:choose>
        <xsl:when test="number($curPage) + 6 &gt; number($numPages)"><xsl:value-of select="$numPages"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="number($curPage) + 6"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$page = $curPage">
        <xsl:value-of select="$page"/>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$search-next}&amp;pageNum={$page}">
          <xsl:value-of select="$page"/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:if test="$page &lt; $endPage">
       <xsl:call-template name="searchResultPageNav">
         <xsl:with-param name="page" select="number($page)+1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>