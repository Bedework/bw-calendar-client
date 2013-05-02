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

  <!-- BEDEWORK THEME SETTINGS -->

  <!-- URL of html resources (images, css, other html) for the current theme -->
  <xsl:variable name="resourcesRoot"><xsl:value-of select="/bedework/browserResourceRoot" />/themes/bedeworkWhite</xsl:variable>


  <!-- ============================== -->
  <!-- Features for the current theme -->
  <!-- ============================== -->
  <!-- Note: Set the global calendar suite preferences
       in the administrative web client (default view, default viewPeriod, etc) -->
       
  <!-- To see changes to this file (or any xslt file) reflected on a running system, 
       append "refreshXslt=yes" to the query string in your browser's address bar.
       For example: "http://localhost:8080/cal/showMain.rdo?refreshXslt=yes 
       For more information about theming, see the Bedework Manual -->


  <!-- FAVICON -->
  <!-- address bar icon -->
  <xsl:variable name="favicon"><xsl:value-of select="$resourcesRoot"/>/images/bedework.ico</xsl:variable>
  

  
  <!-- VIEWS and SUBSCRIPTIONS NAVIGATION TREES -->
  <!-- In this theme, you can use the calendar suite's views,   
       its underlying calendar subscriptions, or both to construct the left navigation menu.
       If both are used, you'll see both menus in the left menu bar.
       Typically, you'll want to use one or the other. -->
  
  <!-- VIEWS LISTING -->
  <!-- "Views" provide an abstraction layer of arbitrary named collections of 
       subscriptions but (currently) has no hierarchy. View are maintained in the 
       "Calendar Suite" tab of the admin web client. -->
<xsl:variable name="useViewsNav">false</xsl:variable>

  <!-- SUBSCRIPTIONS LISTING -->
  <!-- "Subscriptions" provide the full hierarchy of calendars within a calendar suite.
       These are presented with an explorer-like navigation paradigm.  This listing
       is managed in the "Calendar Suite" tab of the admin web client. By deafault, 
       the top level subscriptions will be shown with all folders closed. Generating 
       the subscriptions listing requires a second request to the server (an ajax call).  -->
<xsl:variable name="useSubscriptionsNav">true</xsl:variable>  
  
  
  
  
  
  
  <!-- EVENT ACTION ICONS -->
  <!-- which services to include for event actions in list and detail view: -->
  
  <!-- download ics file -->
  <xsl:variable name="eventIconDownloadIcs">true</xsl:variable>
  
  <!-- download event to my personal Bedework calendar ... use only
       if you plan on running the Bedework personal client  --> 
  <xsl:variable name="eventIconAddToMyCal">true</xsl:variable>
  
  <!-- add to Google calendar --> 
  <xsl:variable name="eventIconGoogleCal">true</xsl:variable>
  
  <!-- add to Facebook... works once the site is hosted at 
       an accessible web address --> 
  <xsl:variable name="eventIconFacebook">true</xsl:variable> 
  
  <!-- "Share This" - included here as a suggestion only.  
       To use the Share This service you must configure it
       explicitly for your site.  You will need to update 
       listEvents.xsl and event.xsl to include 
       the javascript code expected. If you use the Share This
       service, you will likely want to turn off the Facebook 
       icon (above) as it is included already. 
       See https://wiki.jasig.org/pages/viewpage.action?pageId=50860497 -->
  <xsl:variable name="eventIconShareThis">true</xsl:variable>   
    
    

  <!-- FEATURED EVENTS -->
  <!-- Display the featured event images? -->
  <xsl:variable name="featuredEventsEnabled">true</xsl:variable>
  <xsl:variable name="featuredEventsAlwaysOn">false</xsl:variable>
  <xsl:variable name="featuredEventsForDay">true</xsl:variable>
  <xsl:variable name="featuredEventsForWeek">true</xsl:variable>
  <xsl:variable name="featuredEventsForMonth">false</xsl:variable>
  <xsl:variable name="featuredEventsForYear">false</xsl:variable>
  <xsl:variable name="featuredEventsForEventDisplay">false</xsl:variable>
  <xsl:variable name="featuredEventsForCalList">false</xsl:variable>




  <!-- ONGOING EVENTS -->
  <!-- Use the ongoing events sidebar? -->
  <!-- If ongoing events sidebar is enabled,
   you must set UseCategory for ongoing events to appear. -->
  <xsl:variable name="ongoingEventsEnabled">true</xsl:variable>

  <!-- Use the specified category to mark an event as ongoing.  -->
  <xsl:variable name="ongoingEventsUseCategory">true</xsl:variable>
  <!-- The following CatUid represents category "sys/Ongoing" -->
  <xsl:variable name="ongoingEventsCatUid">402881e7-25b99d14-0125-b9a50c22-00000002</xsl:variable>

  <!-- Always display sidebar, even if no events are ongoing? -->
  <xsl:variable name="ongoingEventsAlwaysDisplayed">true</xsl:variable>

  <!-- Reveal ongoing events in the main event list
       when a collection (e.g calendar "Exhibits") is directly selected? -->
  <xsl:variable name="ongoingEventsShowForCollection">true</xsl:variable>





  <!-- FEED URL AND WIDGET BUILDER -->
  <!-- The urlbuilder constructs filtered feeds (e.g. json, rss, xml)
       and widgets and points to the cached feeder application for delivery. -->
  
  <!-- Location of the urlbuilder application; this is set to the 
       default quickstart location. If you move it, you must change this
       value. -->
  <xsl:variable name="urlbuilder">/urlbuilder</xsl:variable>
  
  <!-- Embed the urlbuilder??
       If true, the urlbuilder will be rendered in an iframe.
       If false, it will be treated as an external link.  -->
  <xsl:variable name="embedUrlBuilder">true</xsl:variable>
  





  <!-- JAVASCRIPT CONSTANTS -->
  <xsl:template name="themeJavascriptVariables">
    // URL for the header/logo area
    var headerBarLink = "/bedework";
  </xsl:template>




  <!-- CUSTOM CONTENT: HEADER, LEFT COLUMN, FOOTER -->
  <!-- The three templates below are pulled into the theme 
       and use internationalized strings
       (found in ../default/strings.xsl).

       If you plan on using only one language, you can safely
       change the <xsl:copy> blocks to plain text below.

       If you plan on using more than one language, use the
       strings.xsl file.  See the manual about setting up a
       strings.xsl for multiple languages. -->

  <!-- HEADER TEXT/LINKS -->
  <!-- the text and links found (by default) in the
       lower right of the header.  -->
  <xsl:template name="headerTextLinks">
    <h2>
      Public Events Calendar
    </h2>
  </xsl:template>

  <!-- FOOTER TEXT/LINKS -->
  <!-- Show the skin select box in the footer?
  You may also opt to remove the form in footer.xsl. -->
  <xsl:variable name="showFootForm">true</xsl:variable>

  <!-- text in the footer -->
  <xsl:template name="footerText">
    <xsl:copy-of select="$bwStr-Foot-BasedOnThe" />
    <xsl:text> </xsl:text>
    <a href="http://www.jasig.org/bedework/documentation">
      <xsl:copy-of select="$bwStr-Foot-BedeworkCalendarSystem" />
    </a>
    |
    <a href="http://www.jasig.org/bedework/whosusing">
      <xsl:copy-of select="$bwStr-Foot-ProductionExamples" />
    </a>
    |
    <a href="?noxslt=yes">
      <xsl:copy-of select="$bwStr-Foot-ShowXML" />
    </a>
    |
    <a href="?refreshXslt=yes">
      <xsl:copy-of select="$bwStr-Foot-RefreshXSLT" />
    </a>
    <br/>
    <xsl:copy-of select="$bwStr-Foot-Credits" />
  </xsl:template>


  <!-- LEFT COLUMN TEXT -->
  <!-- custom text in the left column -->
  <xsl:template name="leftColumnText">
     <ul class="sideLinks">
       <li>
         <a href="/caladmin"><xsl:copy-of select="$bwStr-LCol-ManageEvents"/></a>
       </li>
       <li>
         <a href="/eventsubmit">
           <xsl:copy-of select="$bwStr-LCol-Submit"/>
         </a>
       </li>
       <li>
         <a href="http://www.jasig.org/bedework/documentation"><xsl:copy-of select="$bwStr-LCol-Help"/></a>
       </li>
     </ul>
     <ul class="sideLinksExpand">
       <li>
         <h4 class="additionalUnivClicker">Other Calendars +</h4>
         <ul id="additionalUnivSub">
           <li>
             <a href="http://calendar.duke.edu">
               calendar.duke.edu
             </a>
           </li>
           <li>
             <a href="http://events.rpi.edu">
               events.rpi.edu
             </a>
           </li>
		 </ul>
       </li>
       <li>
         <h4 class="additionalOptionsClicker">Other Links +</h4>
         <ul id="additionalOptionsSub">
           <li>
             <a href="http://jasig.org/bedework">
               Bedework
             </a>
           </li>
         </ul>
       </li>
     </ul>
   </xsl:template>




  <!-- NOT YET ENABLED -->
  <!-- the following features did not make the 3.8 release, and are here
       for reference -->

   <!-- DEADLINES/TASKS -->
   <!-- use the deadlines sidebar? -->
   <!-- if deadlines sidebar is enabled, deadlines will appear
        in the sidebar under ongoing events.  Deadlines will
        be presented as tasks and will be treated as such in
        calendar clients. -->
   <!-- <xsl:variable name="deadlinesEnabled">false</xsl:variable> -->

   <!-- always display sidebar, even if no deadlines are present? -->
   <!-- <xsl:variable name="deadlinesAlwaysDisplayed">true</xsl:variable> -->

  <!-- FOR ONGOING EVENTS -->
  <!-- pull normal events that are longer than day range into ongoing list? -->
  <!-- (this automates the use of ongoing - tagging not needed) -->
  <!-- <xsl:variable name="ongoingEventsUseDayRange">false</xsl:variable> -->
  <!-- <xsl:variable name="ongoingEventsDayRange">12</xsl:variable> -->

</xsl:stylesheet>
