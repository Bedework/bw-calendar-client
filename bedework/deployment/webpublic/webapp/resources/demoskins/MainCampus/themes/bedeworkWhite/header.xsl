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
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">

  <!--==== HEADER TEMPLATES and NAVIGATION  ====-->
  <!-- these templates are separated out for convenience and to simplify the default template -->

  <xsl:template name="titleBar">
    <div id="head-top">
      <div id="title-text">
        <xsl:call-template name="headerTextLinks"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="dateSelectForm">
    <form name="calForm" method="post" action="{$setViewPeriod}">
      <xsl:if test="/bedework/periodname!='Year'">
        <select name="viewStartDate.month">
          <xsl:for-each select="/bedework/monthvalues/val">
            <xsl:variable name="temp" select="." />
            <xsl:variable name="pos" select="position()" />
            <xsl:choose>
              <xsl:when
                test="/bedework/monthvalues[start=$temp]">
                <option value="{$temp}"
                  selected="selected">
                  <xsl:value-of
                    select="/bedework/monthlabels/val[position()=$pos]" />
                </option>
              </xsl:when>
              <xsl:otherwise>
                <option value="{$temp}">
                  <xsl:value-of
                    select="/bedework/monthlabels/val[position()=$pos]" />
                </option>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </select>
        <xsl:if test="/bedework/periodname!='Month'">
          <select name="viewStartDate.day">
            <xsl:for-each
              select="/bedework/dayvalues/val">
              <xsl:variable name="temp" select="." />
              <xsl:variable name="pos"
                select="position()" />
              <xsl:choose>
                <xsl:when
                  test="/bedework/dayvalues[start=$temp]">
                  <option value="{$temp}"
                    selected="selected">
                    <xsl:value-of
                      select="/bedework/daylabels/val[position()=$pos]" />
                  </option>
                </xsl:when>
                <xsl:otherwise>
                  <option value="{$temp}">
                    <xsl:value-of
                      select="/bedework/daylabels/val[position()=$pos]" />
                  </option>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </select>
        </xsl:if>
      </xsl:if>
      <xsl:variable name="temp"
        select="/bedework/yearvalues/start" />
      <input type="text" name="viewStartDate.year" maxlength="4"
        size="4" value="{$temp}" />
      <input name="submit" type="submit" value="go" />
    </form>
  </xsl:template>

  <xsl:template name="tabs">
    <div id="nav-search-wrap">
      <form name="searchForm" id="basic_search" method="post"
        onsubmit="if (this.query.value == '') return false;"
        action="{$search}">
        <!--advance search link  -->
        <input type="text" size="30" name="query" id="basicsearchbox">
          <xsl:attribute name="value"><xsl:value-of select="/bedework/searchResults/query" /></xsl:attribute>
        </input>
        <input id="searchSubmit" type="submit" name="submit" value="{$bwStr-Tabs-Search}" />
      </form>
      <ul id="nav-main">
        <xsl:variable name="currentClass">current</xsl:variable>
        <li>
          <a
            href="{$setViewPeriod}&amp;viewType=dayView&amp;date={$curdate}">
            <xsl:if test="/bedework/periodname='Day'">
              <xsl:attribute name="class">
                <xsl:value-of select="$currentClass" />
              </xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$bwStr-Tabs-Day"/>
          </a>
        </li>
        <li>
          <a
            href="{$setViewPeriod}&amp;viewType=weekView&amp;date={$curdate}">
            <xsl:if test="/bedework/periodname='Week'">
              <xsl:attribute name="class">
                <xsl:value-of select="$currentClass" />
              </xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$bwStr-Tabs-Week"/>
          </a>
        </li>
        <li>
          <a
            href="{$setViewPeriod}&amp;viewType=monthView&amp;date={$curdate}">
            <xsl:if test="/bedework/periodname='Month'">
              <xsl:attribute name="class">
                <xsl:value-of select="$currentClass" />
              </xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$bwStr-Tabs-Month"/>
          </a>
        </li>
        <li>
          <a
            href="{$setViewPeriod}&amp;viewType=yearView&amp;date={$curdate}">
            <xsl:if test="/bedework/periodname='Year'">
              <xsl:attribute name="class">
                <xsl:value-of select="$currentClass" />
              </xsl:attribute>
            </xsl:if>
            Jump To Date
          </a>
        </li>
        <li>
          <a
            href="{$setViewPeriod}&amp;viewType=todayView">
            <xsl:if test="/bedework/periodname='Today'">
              <xsl:attribute name="class">
                <xsl:value-of select="$currentClass" />
              </xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$bwStr-Tabs-Today"/>
          </a>
        </li>
      </ul>
      <div class="clear">&#160;</div>
    </div>
  </xsl:template>

</xsl:stylesheet>
