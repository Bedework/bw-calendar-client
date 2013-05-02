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

  <!--==== DATE SELECTION  ====-->

  <xsl:template name="dateSelect">
    <form id="dateSelect" name="calForm" method="post" action="{$setViewPeriod}">
      <xsl:copy-of select="$bwStr-Navi-GoToDate"/>
      <xsl:text> </xsl:text>
      <table border="0" cellpadding="0" cellspacing="0">
        <tr>
          <xsl:if test="/bedework/periodname!='Year'">
            <td>
              <select name="viewStartDate.month">
                <xsl:for-each select="/bedework/monthvalues/val">
                  <xsl:variable name="temp" select="."/>
                  <xsl:variable name="pos" select="position()"/>
                  <xsl:choose>
                    <xsl:when test="/bedework/monthvalues[start=$temp]">
                      <option value="{$temp}" selected="selected">
                        <xsl:value-of select="/bedework/monthlabels/val[position()=$pos]"/>
                      </option>
                    </xsl:when>
                    <xsl:otherwise>
                      <option value="{$temp}">
                        <xsl:value-of select="/bedework/monthlabels/val[position()=$pos]"/>
                      </option>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </select>
            </td>
            <xsl:if test="/bedework/periodname!='Month'">
              <td>
                <select name="viewStartDate.day">
                  <xsl:for-each select="/bedework/dayvalues/val">
                    <xsl:variable name="temp" select="."/>
                    <xsl:variable name="pos" select="position()"/>
                    <xsl:choose>
                      <xsl:when test="/bedework/dayvalues[start=$temp]">
                        <option value="{$temp}" selected="selected">
                          <xsl:value-of select="/bedework/daylabels/val[position()=$pos]"/>
                        </option>
                      </xsl:when>
                      <xsl:otherwise>
                        <option value="{$temp}">
                          <xsl:value-of select="/bedework/daylabels/val[position()=$pos]"/>
                        </option>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:for-each>
                </select>
              </td>
            </xsl:if>
          </xsl:if>
          <td>
            <xsl:variable name="temp" select="/bedework/yearvalues/start"/>
            <input type="text" name="viewStartDate.year" maxlength="4" size="4" value="{$temp}"/>
          </td>
          <td>
            <input name="submit" type="submit" value="{$bwStr-Navi-Go}"/>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>


</xsl:stylesheet>
