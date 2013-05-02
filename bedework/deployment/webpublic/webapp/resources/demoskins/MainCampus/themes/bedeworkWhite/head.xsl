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


  <!--==== Head Section ====-->
  <xsl:template name="head">

    <head>
      <title>
        <xsl:if test="/bedework/page='event'">
          <xsl:value-of select="/bedework/event/summary" />
          <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:copy-of select="$bwStr-Root-PageTitle" />
      </title>

      <meta content="text/html;charset=utf-8" http-equiv="Content-Type" />

      <!-- address bar favicon -->
      <link rel="icon" type="image/ico" href="{$favicon}" />

      <!-- load css -->
      <link rel="stylesheet" type="text/css" media="screen" href="{$resourcesRoot}/css/bwTheme.css" />
      <link rel="stylesheet" type="text/css" media="print" href="{$resourcesRoot}/css/print.css" />

      <!-- Dependencies -->
      <xsl:text disable-output-escaping="yes">
        <![CDATA[
        <!--[if IE 6]>
          <link rel="stylesheet" type="text/css" media="screen" href="/calrsrc.MainCampus/default/default/defaultTheme/css/ie6.css"/>
        <![endif]-->

        <!--[if IE 7]>
          <link rel="stylesheet" type="text/css" media="screen" href="/calrsrc.MainCampus/default/default/defaultTheme/css/ie7.css"/>
        <![endif]-->
        ]]>
      </xsl:text>

      <!-- load javascript -->
      <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-1.3.2.min.js">&#160;</script>
      <script type="text/javascript" src="{$resourcesRoot}/javascript/yui/yahoo-dom-event.js">&#160;</script>
      <script type="text/javascript" src="{$resourcesRoot}/javascript/yui/calendar-min.js">&#160;</script>
      <script type="text/javascript" src="{$resourcesRoot}/javascript/yui/animation-min.js">&#160;</script>
      <xsl:if test="/bedework/page='searchResult'">
        <script type="text/javascript" src="{$resourcesRoot}/javascript/catSearch.js">&#160;</script>
      </xsl:if>
      <script type="text/javascript">
        <xsl:call-template name="themeJavascriptVariables"/>
      </script>
      <script type="text/javascript" src="{$resourcesRoot}/javascript/bedework.js">&#160;</script>

      <!-- set up the javascript navigational calendar -->
      <script type="text/javascript">
        <xsl:call-template name="jsonDataObject" />
      </script>
      <xsl:call-template name="jsCalendarLocale"/>
      <script type="text/javascript" src="{$resourcesRoot}/javascript/ifs-calendar.js">&#160;</script>

      <xsl:if test="/bedework/page='displayCalendarForExport'">
        <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-ui-1.7.1.custom.min.js">&#160;</script>
        <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/jquery-ui-1.7.1.custom.css"/>
        <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/bedeworkJquery.css"/>
        <script type="text/javascript">
          <xsl:call-template name="setupDatePicker"/>
        </script>
      </xsl:if>
    </head>
  </xsl:template>

  <!-- JavaScript Array for Calendar -->
  <xsl:template name="jsonDataObject">
    <!-- Month to display (to be handed in to js, so it aligns with the events beeing shown) -->
    <xsl:variable name="displayMonthYear"><xsl:value-of select="substring(/bedework/currentdate/date,5,2)" /><xsl:text>/</xsl:text><xsl:value-of select="substring(/bedework/currentdate/date,0,5)" /></xsl:variable>
    <xsl:variable name="rangeStartMo"><xsl:value-of select="substring(/bedework/firstday/date,5,2)" /></xsl:variable>
    <xsl:variable name="rangeStartDay"><xsl:value-of select="substring(/bedework/firstday/date,7,2)" /></xsl:variable>
    <xsl:variable name="rangeStartYear"><xsl:value-of select="substring(/bedework/firstday/date,1,4)" /></xsl:variable>
    <xsl:variable name="rangeEndMo"><xsl:value-of select="substring(/bedework/lastday/date,5,2)" /></xsl:variable>
    <xsl:variable name="rangeEndDay"><xsl:value-of select="substring(/bedework/lastday/date,7,2)" /></xsl:variable>
    <xsl:variable name="rangeEndYear"><xsl:value-of select="substring(/bedework/lastday/date,1,4)" /></xsl:variable>
    navcalendar = [ "<xsl:value-of select="$displayMonthYear" />", "<xsl:value-of select="$rangeStartMo" />", "<xsl:value-of select="$rangeStartDay" />", "<xsl:value-of select="$rangeStartYear" />", "<xsl:value-of select="$rangeEndMo" />", "<xsl:value-of select="$rangeEndDay" />", "<xsl:value-of select="$rangeEndYear" />", ];
  </xsl:template>

  <xsl:template name="setupDatePicker">
    <xsl:comment>
     $.datepicker.setDefaults({
       constrainInput: true,
       dateFormat: "yy-mm-dd",
       showOn: "both",
       buttonImage: "<xsl:value-of select='$resourcesRoot'/>/images/calIcon.gif",
       buttonImageOnly: true,
       gotoCurrent: true,
       duration: ""
     });
     $(document).ready(function() {
       $("#bwExportCalendarWidgetStartDate").datepicker({
       }).attr("readonly", "readonly");
       $("#bwExportCalendarWidgetEndDate").datepicker({
       }).attr("readonly", "readonly");
     });
     </xsl:comment>
  </xsl:template>

</xsl:stylesheet>
