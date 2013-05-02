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
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">
<xsl:output
  method="xml"
  indent="no"
  media-type="text/html"
  doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
  standalone="yes"
  omit-xml-declaration="yes"/>

  <!-- ========================================================= -->
  <!--         PUBLIC EVENTS SUBMISSION CALENDAR STYLESHEET      -->
  <!-- ========================================================= -->

  <!-- DEFINE INCLUDES -->
  <xsl:include href="/bedework-common/default/default/util.xsl"/>
  
  <!-- include the language string libraries -->
  <xsl:include href="/bedework-common/default/default/errors.xsl" />
  <xsl:include href="/bedework-common/default/default/messages.xsl" />
  <xsl:include href="./strings.xsl"/>

  <!-- DEFINE GLOBAL CONSTANTS -->
  <!-- URL of html resources (images, css, other html); by default this is
       set to the application root, but for the personal calendar
       this should be changed to point to a
       web server over https to avoid mixed content errors, e.g.,
  <xsl:variable name="resourcesRoot">https://mywebserver.edu/myresourcesdir</xsl:variable>
    -->
  <xsl:variable name="resourcesRoot" select="/bedework/browserResourceRoot"/>

  <!-- URL of the XSL template directory -->
  <!-- The approot is an appropriate place to put
       included stylesheets and xml fragments. These are generally
       referenced relatively (like errors.xsl and messages.xsl above);
       this variable is here for your convenience if you choose to
       reference it explicitly.  It is not used in this stylesheet, however,
       and can be safely removed if you so choose. -->
  <xsl:variable name="appRoot" select="/bedework/approot"/>

  <!-- Properly encoded prefixes to the application actions; use these to build
       urls; allows the application to be used without cookies or within a portal.
       These urls are rewritten in header.jsp and simply passed through for use
       here. Every url includes a query string (either ?b=de or a real query
       string) so that all links constructed in this stylesheet may begin the
       query string with an ampersand. -->

  <xsl:variable name="submissionsRootEncoded" select="/bedework/submissionsRoot/encoded"/>
  <xsl:variable name="submissionsRootUnencoded" select="/bedework/submissionsRoot/unencoded"/>

  <!-- main -->
  <xsl:variable name="setup" select="/bedework/urlPrefixes/setup"/>
  <xsl:variable name="initEvent" select="/bedework/urlPrefixes/event/initEvent"/>
  <xsl:variable name="initPendingEvents" select="/bedework/urlPrefixes/event/initPendingEvents"/>
  <xsl:variable name="addEvent" select="/bedework/urlPrefixes/event/addEvent"/>
  <xsl:variable name="editEvent" select="/bedework/urlPrefixes/event/editEvent"/>
  <xsl:variable name="gotoEditEvent" select="/bedework/urlPrefixes/event/gotoEditEvent"/>
  <xsl:variable name="updateEvent" select="/bedework/urlPrefixes/event/updateEvent"/>
  <xsl:variable name="delEvent" select="/bedework/urlPrefixes/event/delEvent"/>
  <xsl:variable name="initUpload" select="/bedework/urlPrefixes/misc/initUpload"/>
  <xsl:variable name="upload" select="/bedework/urlPrefixes/misc/upload"/>

  <!-- URL of the web application - includes web context -->
  <xsl:variable name="urlPrefix" select="/bedework/urlprefix"/>

  <!-- Other generally useful global variables -->
  <xsl:variable name="prevdate" select="/bedework/previousdate"/>
  <xsl:variable name="nextdate" select="/bedework/nextdate"/>
  <xsl:variable name="curdate" select="/bedework/currentdate/date"/>
  <xsl:variable name="skin">default</xsl:variable>
  <xsl:variable name="publicCal">/cal</xsl:variable>

  <!-- the following variable can be set to "true" or "false";
       to use jQuery widgets and fancier UI features, set to false - these are
       not guaranteed to work in portals. -->
  <xsl:variable name="portalFriendly">false</xsl:variable>

 <!-- BEGIN MAIN TEMPLATE -->
  <xsl:template match="/">
    <html lang="en">
      <head>
        <xsl:call-template name="headSection"/>
      </head>
      <body>
        <xsl:choose>
          <xsl:when test="/bedework/page = 'addEvent'">
            <xsl:attribute name="onload">focusElement('bwEventTitle');bwSetupDatePickers();</xsl:attribute>
          </xsl:when>
          <xsl:when test="/bedework/page = 'editEvent'">
            <xsl:attribute name="onload">initRXDates();initXProperties();bwSetupDatePickers();</xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <div id="bedework"><!-- main wrapper div -->
          <xsl:call-template name="header"/>
          <xsl:call-template name="messagesAndErrors"/>
          <xsl:call-template name="menuTabs"/>
          <div id="bodyContent">
            <xsl:choose>
              <xsl:when test="/bedework/page='addEvent'">
                <xsl:apply-templates select="/bedework/formElements" mode="addEvent"/>
              </xsl:when>
              <xsl:when test="/bedework/page='eventList'">
                <xsl:call-template name="eventList"/>
              </xsl:when>
              <xsl:when test="/bedework/page='editEvent'">
                <xsl:apply-templates select="/bedework/formElements" mode="editEvent"/>
              </xsl:when>
              <xsl:when test="/bedework/page='upload'">
                <xsl:call-template name="upload" />
              </xsl:when>
              <xsl:otherwise>
                <!-- home / entrance screen -->
                <xsl:call-template name="home"/>
              </xsl:otherwise>
            </xsl:choose>
          </div>
          <!-- footer -->
          <xsl:call-template name="footer"/>
        </div>
      </body>
    </html>
  </xsl:template>

  <!--==== HEAD SECTION  ====-->
  <xsl:template name="headSection">
    <title><xsl:copy-of select="$bwStr-Head-BedeworkSubmitPubEv"/></title>
    <meta name="robots" content="noindex,nofollow"/>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <link rel="stylesheet" href="{$resourcesRoot}/default/default/default.css"/>
    <link rel="icon" type="image/ico" href="{$resourcesRoot}/resources/bedework.ico" />
    
    <!-- note: the non-breaking spaces in the script bodies below are to avoid
         losing the script closing tags (which avoids browser problems) -->
    <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-1.3.2.min.js">&#160;</script>
    <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-ui-1.7.1.custom.min.js">&#160;</script>
    <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/jquery-ui-1.7.1.custom.css"/>
    <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/bedeworkJquery.css"/>
    
    <script type="text/javascript" src="{$resourcesRoot}/resources/bedework.js">&#160;</script>
    
    <xsl:if test="/bedework/page='addEvent' or /bedework/page='editEvent'">
      <script type="text/javascript" src="/bedework-common/javascript/bedework/bwClock.js">&#160;</script>
      <link rel="stylesheet" href="/bedework-common/javascript/bedework/bwClock.css"/>
      <xsl:choose>
        <xsl:when test="$portalFriendly = 'true'">
          <script type="text/javascript" src="{$resourcesRoot}/resources/dynCalendarWidget.js">&#160;</script>
          <link rel="stylesheet" href="{$resourcesRoot}/resources/dynCalendarWidget.css"/>
        </xsl:when>
        <xsl:otherwise>
          <script type="text/javascript">
            <xsl:comment>
            $.datepicker.setDefaults({
              constrainInput: true,
              dateFormat: "yy-mm-dd",
              showOn: "both",
              buttonImage: "<xsl:value-of select='$resourcesRoot'/>/resources/calIcon.gif",
              buttonImageOnly: true,
              gotoCurrent: true,
              duration: ""
            });

            function bwSetupDatePickers() {
              // startdate
              $("#bwEventWidgetStartDate").datepicker({
                defaultDate: new Date(<xsl:value-of select="/bedework/formElements/form/start/yearText/input/@value"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="/bedework/formElements/form/start/day/select/option[@selected = 'selected']/@value"/>)
              }).attr("readonly", "readonly");
              $("#bwEventWidgetStartDate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/start/rfc3339DateTime,'T')"/>');
              
              // starttime
              $("#bwStartClock").bwTimePicker({
                hour24: <xsl:value-of select="/bedework/hour24"/>,
                attachToId: "calWidgetStartTimeHider",
                hourIds: ["eventStartDateHour","eventStartDateSchedHour"],
                minuteIds: ["eventStartDateMinute","eventStartDateSchedMinute"],
                ampmIds: ["eventStartDateAmpm","eventStartDateSchedAmpm"],
                hourLabel: "<xsl:value-of select="$bwStr-Cloc-Hour"/>",
                minuteLabel: "<xsl:value-of select="$bwStr-Cloc-Minute"/>",
                amLabel: "<xsl:value-of select="$bwStr-Cloc-AM"/>",
                pmLabel: "<xsl:value-of select="$bwStr-Cloc-PM"/>"
              });
              
              // enddate
              $("#bwEventWidgetEndDate").datepicker({
                defaultDate: new Date(<xsl:value-of select="/bedework/formElements/form/end/dateTime/yearText/input/@value"/>, <xsl:value-of select="number(/bedework/formElements/form/end/dateTime/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="/bedework/formElements/form/end/dateTime/day/select/option[@selected = 'selected']/@value"/>)
              }).attr("readonly", "readonly");
              $("#bwEventWidgetEndDate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/end/rfc3339DateTime,'T')"/>');
              
              // endtime
              $("#bwEndClock").bwTimePicker({
                hour24: <xsl:value-of select="/bedework/hour24"/>,
                attachToId: "calWidgetEndTimeHider",
                hourIds: ["eventEndDateHour"],
                minuteIds: ["eventEndDateMinute"],
                ampmIds: ["eventEndDateAmpm"],
                hourLabel: "<xsl:value-of select="$bwStr-Cloc-Hour"/>",
                minuteLabel: "<xsl:value-of select="$bwStr-Cloc-Minute"/>",
                amLabel: "<xsl:value-of select="$bwStr-Cloc-AM"/>",
                pmLabel: "<xsl:value-of select="$bwStr-Cloc-PM"/>"
              });
            }
            </xsl:comment>
          </script>
        </xsl:otherwise>
      </xsl:choose>
      <script type="text/javascript" src="{$resourcesRoot}/resources/bedeworkEventForm.js">&#160;</script>
      <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkXProperties.js">&#160;</script>
      <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkUtil.js">&#160;</script>
    </xsl:if>
    <script type="text/javascript">
      <xsl:comment>
      function focusElement(id) {
      // focuses element by id
        document.getElementById(id).focus();
      }
      function initRXDates() {
        // return string values to be loaded into javascript for rdates
        <xsl:for-each select="/bedework/formElements/form/rdates/rdate">
          bwRdates.update('<xsl:value-of select="date"/>','<xsl:value-of select="time"/>',false,false,false,'<xsl:value-of select="tzid"/>');
        </xsl:for-each>
        // return string values to be loaded into javascript for exdates
        <xsl:for-each select="/bedework/formElements/form/exdates/rdate">
          bwExdates.update('<xsl:value-of select="date"/>','<xsl:value-of select="time"/>',false,false,false,'<xsl:value-of select="tzid"/>');
        </xsl:for-each>
      }
      function initXProperties() {
        <xsl:for-each select="/bedework/formElements/form/xproperties/node()[text()]">
          bwXProps.init("<xsl:value-of select="name()"/>",[<xsl:for-each select="parameters/node()">["<xsl:value-of select="name()"/>","<xsl:value-of select="node()"/>"]<xsl:if test="position() != last()">,</xsl:if></xsl:for-each>],"<xsl:call-template name="escapeJson"><xsl:with-param name="string"><xsl:value-of select="values/text"/></xsl:with-param></xsl:call-template>");
        </xsl:for-each>
      }
      </xsl:comment>
    </script>
  </xsl:template>

  <!--==== HEADER TEMPLATES and NAVIGATION  ====-->

  <xsl:template name="header">
    <div id="header">
      <a href="/bedework/">
        <img id="logo"
            alt="logo"
            src="{$resourcesRoot}/resources/bedeworkAdminLogo.gif"
            width="217"
            height="40"
            border="0"/>
      </a>
      <!-- set the page heading: -->
      <h1>
        <xsl:copy-of select="$bwStr-Hedr-BedeworkPubEventSub"/>
      </h1>
    </div>
    <div id="statusBar">
      <xsl:copy-of select="$bwStr-Hedr-LoggedInAs"/>
          <xsl:text> </xsl:text>
          <strong><xsl:value-of select="/bedework/userid"/></strong>
          <xsl:text> </xsl:text>
          <span class="logout"><a href="{$setup}&amp;logout=true"><xsl:copy-of select="$bwStr-Hedr-Logout"/></a></span>
    </div>
  </xsl:template>

  <xsl:template name="messagesAndErrors">
    <xsl:if test="/bedework/message">
      <ul id="messages">
        <xsl:for-each select="/bedework/message">
          <li><xsl:apply-templates select="."/></li>
        </xsl:for-each>
      </ul>
    </xsl:if>
    <xsl:if test="/bedework/error">
      <ul id="errors">
        <xsl:for-each select="/bedework/error">
          <li><xsl:apply-templates select="."/></li>
        </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>

  <!--==== MENUTABS ====-->
  <xsl:template name="menuTabs">
    <ul id="menuTabs">
      <xsl:choose>
        <xsl:when test="/bedework/page='home'">
          <li class="selected"><xsl:copy-of select="$bwStr-MeTa-Overview"/></li>
          <li><a href="{$initEvent}"><xsl:copy-of select="$bwStr-MeTa-AddEvent"/></a></li>
          <li><a href="{$initPendingEvents}&amp;calPath={$submissionsRootEncoded}&amp;listAllEvents=true"><xsl:copy-of select="$bwStr-MeTa-MyPendingEvents"/></a></li>
        </xsl:when>
        <xsl:when test="/bedework/page='eventList'">
          <li><a href="{$setup}"><xsl:copy-of select="$bwStr-MeTa-Overview"/></a></li>
          <li><a href="{$initEvent}"><xsl:copy-of select="$bwStr-MeTa-AddEvent"/></a></li>
          <li class="selected"><xsl:copy-of select="$bwStr-MeTa-MyPendingEvents"/></li>
        </xsl:when>
        <xsl:otherwise>
          <li><a href="{$setup}"><xsl:copy-of select="$bwStr-MeTa-Overview"/></a></li>
          <li class="selected"><xsl:copy-of select="$bwStr-MeTa-AddEvent"/></li>
          <li><a href="{$initPendingEvents}&amp;calPath={$submissionsRootEncoded}&amp;listAllEvents=true"><xsl:copy-of select="$bwStr-MeTa-MyPendingEvents"/></a></li>
        </xsl:otherwise>
      </xsl:choose>
    </ul>
  </xsl:template>

  <!--==== HOME ====-->
  <xsl:template name="home">
    <div class="navButtons navBox">
      <a href="{$initEvent}"><xsl:copy-of select="$bwStr-Home-Start"/>
        <img alt="previous"
          src="{$resourcesRoot}/resources/arrowRight.gif"
          width="13"
          height="13"
          border="0"/>
      </a>
    </div>
    <h1><xsl:copy-of select="$bwStr-Home-EnteringEvents"/></h1>
    <ol id="introduction">
      <li>
        <xsl:copy-of select="$bwStr-Home-BeforeSubmitting"/><xsl:text> </xsl:text><a href="/cal"><xsl:copy-of select="$bwStr-Home-SeeIfItHasBeenEntered"/></a>.<xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Home-ItIsPossible"/>
      </li>
      <li>
        <xsl:copy-of select="$bwStr-Home-MakeYourTitles"/>
      </li>
      <li>
        <xsl:copy-of select="$bwStr-Home-DoNotInclude"/>
      </li>
    </ol>
  </xsl:template>

  <!--==== ADD EVENT ====-->
  <xsl:template match="formElements" mode="addEvent">
    <xsl:variable name="submitter">
      <xsl:choose>
        <xsl:when test="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']/values/text"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="/bedework/userid"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <form name="eventForm" method="post" action="{$addEvent}" id="standardForm" onsubmit="return setEventFields(this,{$portalFriendly},'{$submitter}');">
      <xsl:apply-templates select="." mode="eventForm"/>
    </form>
  </xsl:template>

  <!--==== EDIT EVENT ====-->
  <xsl:template match="formElements" mode="editEvent">
    <xsl:variable name="submitter">
      <xsl:choose>
        <xsl:when test="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']/values/text"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="/bedework/userid"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <form name="eventForm" method="post" action="{$updateEvent}" id="standardForm" onsubmit="return setEventFields(this,{$portalFriendly},'{$submitter}');">
      <xsl:apply-templates select="." mode="eventForm"/>
    </form>
  </xsl:template>


  <!--==== ADD and EDIT EVENT FORM ====-->
  <xsl:template match="formElements" mode="eventForm">
    <xsl:variable name="calPathEncoded" select="form/calendar/encodedPath"/>
    <xsl:variable name="calPath" select="form/calendar/path"/>
    <xsl:variable name="guid" select="guid"/>
    <xsl:variable name="recurrenceId" select="recurrenceId"/>
    <!-- comment field to hold the user's suggestions:  -->
    <input type="hidden" name="xbwsubmitcomment" id="bwEventComment" value=""/>

      <!-- event info for edit event -->
      <xsl:if test="/bedework/creating != 'true'">

        <table class="common" cellspacing="0">
          <tr>
            <th colspan="2" class="commonHeader">
              <div id="eventActions">
                <xsl:choose>
                  <xsl:when test="recurrenceId != ''">
                    <img src="{$resourcesRoot}/resources/trashIcon.gif" width="13" height="13" border="0" alt="delete"/>
                    <xsl:copy-of select="$bwStr-FoEl-DeleteColon"/>
                    <a href="{$delEvent}&amp;calPath={$calPath}&amp;guid={$guid}" title="delete master (recurring event)"><xsl:copy-of select="$bwStr-FoEl-All"/></a>,
                    <a href="{$delEvent}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}" title="delete instance (recurring event)"><xsl:copy-of select="$bwStr-FoEl-Instance"/>instance</a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{$delEvent}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}" title="delete event">
                      <img src="{$resourcesRoot}/resources/trashIcon.gif" width="13" height="13" border="0" alt="delete"/>
                      <xsl:copy-of select="$bwStr-FoEl-Delete"/>
                      <xsl:if test="form/recurringEntity='true'">
                        <xsl:copy-of select="$bwStr-FoEl-All"/>
                      </xsl:if>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
              <!-- Display type of event -->
              <xsl:variable name="entityType">
                <xsl:choose>
                  <xsl:when test="entityType = '2'"><xsl:copy-of select="$bwStr-FoEl-TASK"/></xsl:when>
                  <xsl:when test="scheduleMethod = '2'"><xsl:copy-of select="$bwStr-FoEl-Meeting"/></xsl:when>
                  <xsl:otherwise><xsl:copy-of select="$bwStr-FoEl-EVENT"/></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:if test="form/recurringEntity='true' or recurrenceId != ''">
                <xsl:copy-of select="$bwStr-FoEl-Recurring"/>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="form">
                  <!-- just a placeholder: need to add owner to the jsp -->
                  <xsl:copy-of select="$bwStr-FoEl-Personal"/><xsl:text> </xsl:text><xsl:value-of select="$entityType"/>
                </xsl:when>
                <xsl:when test="public = 'true'">
                  <xsl:copy-of select="$bwStr-FoEl-Public"/><xsl:text> </xsl:text><xsl:value-of select="$entityType"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$entityType"/> (<xsl:value-of select="calendar/owner"/>)
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="form/recurringEntity='true' and recurrenceId = ''">
                <xsl:text> </xsl:text>
                <em><xsl:copy-of select="$bwStr-FoEl-RecurrenceMaster"/></em>
              </xsl:if>
            </th>
          </tr>
        </table>
      </xsl:if>

    <div id="instructions">
      <div id="bwHelp-Details">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location');hide('bwEventTab-Details','bwHelp-Details','bwBottomNav-Details');"
             onclick="return validateStep1();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
        <xsl:copy-of select="$bwStr-FoEl-Step1"/>
      </div>
      <div id="bwHelp-Location" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Details','bwHelp-Details','bwBottomNav-Details'); hide('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwEventTab-Contact','bwHelp-Contact','bwBottomNav-Contact'); hide('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location');"
             onclick="return validateStep2();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
        <xsl:copy-of select="$bwStr-FoEl-Step2"/>
      </div>
      <div id="bwHelp-Contact" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location'); hide('bwHelp-Contact','bwEventTab-Contact','bwBottomNav-Contact');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwEventTab-Categories','bwHelp-Categories','bwBottomNav-Categories'); hide('bwHelp-Contact','bwEventTab-Contact','bwBottomNav-Contact');"
             onclick="return validateStep3();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
        <xsl:copy-of select="$bwStr-FoEl-Step3"/>
      </div>
      <div id="bwHelp-Categories" class="invisible">
        <!-- this tab is now "topical areas - we will leave the ids named "categories" for now. -->
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Contact','bwHelp-Contact','bwBottomNav-Contact'); hide('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwHelp-Review','bwEventTab-Review','bwBottomNav-Review'); hide('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories'); ">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
        <xsl:copy-of select="$bwStr-FoEl-Step4"/>
      </div>
      <div id="bwHelp-Review" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories'); hide('bwHelp-Review','bwEventTab-Review','bwBottomNav-Review'); ">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a>
          <span class="hidden">
            <!-- this is here just to take up the appropriate space  -->
            <xsl:text> </xsl:text>|<xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </span>
        </div>
        <xsl:copy-of select="$bwStr-FoEl-Step5"/>
      </div>
    </div>

    <div id="eventFormContent">
      <!-- Basic tab -->
      <!-- ============== -->
      <!-- this tab is visible by default -->
      <div id="bwEventTab-Details">
        <table cellspacing="0" class="common">
          <!-- Calendar -->
          <!-- ======== -->
          <xsl:variable name="submissionCalendars">
            <xsl:value-of select="count(/bedework/myCalendars//calendar[calType='1'])"/>
          </xsl:variable>
          <tr>
            <xsl:if test="$submissionCalendars = 1">
              <xsl:attribute name="class">invisible</xsl:attribute>
              <!-- hide this row altogether if there is only one calendar; if you want the calendar
                   path displayed, comment out this xsl:if. -->
            </xsl:if>
            <td class="fieldname">
              <xsl:copy-of select="$bwStr-FoEl-Calendar"/>
            </td>
            <td class="fieldval">
              <xsl:choose>
                <xsl:when test="$submissionCalendars = 1">
                  <!-- there is only 1 writable calendar, just send a hidden field -->
                  <xsl:variable name="newCalPath"><xsl:value-of select="/bedework/myCalendars//calendar[calType='1']/path"/></xsl:variable>
                  <input type="hidden" name="newCalPath" value="{$newCalPath}"/>
                  <span id="bwEventCalDisplay">
                    <xsl:value-of select="$newCalPath"/>
                  </span>
                </xsl:when>
                <xsl:otherwise>
                  <select name="newCalPath" id="bwNewCalPathField">
                    <xsl:for-each select="/bedework/myCalendars//calendar[calType='1']">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="path"/></xsl:attribute>
                        <xsl:value-of select="substring-after(substring-after(path,/bedework/submissionsRoot/unencoded),'/')"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
          <!--  Summary (title) of event  -->
          <!--  ========================= -->
          <tr>
            <td class="fieldname">
              <xsl:copy-of select="$bwStr-FoEl-Title"/>
            </td>
            <td class="fieldval">
              <div id="bwEventTitleNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-MustIncludeTitle"/></div> <!-- a holder for validation notes -->
              <xsl:variable name="title" select="form/title/input/@value"/>
              <input type="text" name="summary" size="80" value="{$title}" id="bwEventTitle"/>
            </td>
          </tr>

          <!--  Date and Time -->
          <!--  ============= -->
          <tr>
            <td class="fieldname">
              <xsl:copy-of select="$bwStr-FoEl-DateAndTime"/>
            </td>
            <td class="fieldval">
              <!-- Set the timefields class for the first load of the page;
                   subsequent changes will take place using javascript without a
                   page reload. -->
              <xsl:variable name="timeFieldsClass">
                <xsl:choose>
                  <xsl:when test="form/allDay/input/@checked='checked'">invisible</xsl:when>
                  <xsl:otherwise>timeFields</xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

              <!-- All day flag -->
	            <input type="checkbox" name="allDayFlag" id="allDayFlag" onclick="swapAllDayEvent(this)" value="off">
	              <xsl:if test="form/allDay/input/@checked='checked'">
	                <xsl:attribute name="checked">checked</xsl:attribute>
	                <xsl:attribute name="value">on</xsl:attribute>
	              </xsl:if>
	            </input>
	            <input type="hidden" name="eventStartDate.dateOnly" value="off" id="allDayStartDateField">
	              <xsl:if test="form/allDay/input/@checked='checked'">
	                <xsl:attribute name="value">on</xsl:attribute>
	              </xsl:if>
	            </input>
	            <input type="hidden" name="eventEndDate.dateOnly" value="off" id="allDayEndDateField">
	              <xsl:if test="form/allDay/input/@checked='checked'">
	                <xsl:attribute name="value">on</xsl:attribute>
	              </xsl:if>
	            </input>
	            <label for="allDayFlag">
	              <xsl:copy-of select="$bwStr-FoEl-AllDay"/>
	            </label>

              <!-- HIDE floating event: no timezone (and not UTC)
              <xsl:choose>
                <xsl:when test="form/floating/input/@checked='checked'">
                  <input type="checkbox" name="floatingFlag" id="floatingFlag" onclick="swapFloatingTime(this)" value="on" checked="checked"/>
                  <input type="hidden" name="eventStartDate.floating" value="true" id="startFloating"/>
                  <input type="hidden" name="eventEndDate.floating" value="true" id="endFloating"/>
                </xsl:when>
                <xsl:otherwise>
                  <input type="checkbox" name="floatingFlag" id="floatingFlag" onclick="swapFloatingTime(this)" value="off"/>
                  <input type="hidden" name="eventStartDate.floating" value="false" id="startFloating"/>
                  <input type="hidden" name="eventEndDate.floating" value="false" id="endFloating"/>
                </xsl:otherwise>
              </xsl:choose>
              floating -->

              <!-- HIDE store time as coordinated universal time (UTC)
              <xsl:choose>
                <xsl:when test="form/storeUTC/input/@checked='checked'">
                  <input type="checkbox" name="storeUTCFlag" id="storeUTCFlag" onclick="swapStoreUTC(this)" value="on" checked="checked"/>
                  <input type="hidden" name="eventStartDate.storeUTC" value="true" id="startStoreUTC"/>
                  <input type="hidden" name="eventEndDate.storeUTC" value="true" id="endStoreUTC"/>
                </xsl:when>
                <xsl:otherwise>
                  <input type="checkbox" name="storeUTCFlag" id="storeUTCFlag" onclick="swapStoreUTC(this)" value="off"/>
                  <input type="hidden" name="eventStartDate.storeUTC" value="false" id="startStoreUTC"/>
                  <input type="hidden" name="eventEndDate.storeUTC" value="false" id="endStoreUTC"/>
                </xsl:otherwise>
              </xsl:choose>
              store as UTC-->

              <br/>
              <div class="dateStartEndBox">
                <strong><xsl:copy-of select="$bwStr-FoEl-Start"/></strong>
                <div class="dateFields">
                  <span class="startDateLabel"><xsl:copy-of select="$bwStr-FoEl-Date"/><xsl:text> </xsl:text></span>
                  <xsl:choose>
                    <xsl:when test="$portalFriendly = 'true'">
                      <xsl:copy-of select="/bedework/formElements/form/start/month/*"/>
                      <xsl:copy-of select="/bedework/formElements/form/start/day/*"/>
                      <xsl:choose>
                        <xsl:when test="/bedework/creating = 'true'">
                          <xsl:copy-of select="/bedework/formElements/form/start/year/*"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:copy-of select="/bedework/formElements/form/start/yearText/*"/>
                        </xsl:otherwise>
                      </xsl:choose>
                      <script language="JavaScript" type="text/javascript">
                        <xsl:comment>
                        startDateDynCalWidget = new dynCalendar('startDateDynCalWidget', <xsl:value-of select="number(/bedework/formElements/form/start/yearText/input/@value)"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected='selected']/@value)-1"/>, <xsl:value-of select="number(/bedework/formElements/form/start/day/select/option[@selected='selected']/@value)"/>, 'startDateCalWidgetCallback', '<xsl:value-of select="$resourcesRoot"/>/resources/');
                        </xsl:comment>
                      </script>
                    </xsl:when>
                    <xsl:otherwise>
                      <input type="text" name="bwEventWidgetStartDate" id="bwEventWidgetStartDate" size="10"/>
                      <script language="JavaScript" type="text/javascript">
                        <xsl:comment>
                        /*$("#bwEventWidgetStartDate").datepicker({
                          defaultDate: new Date(<xsl:value-of select="form/start/yearText/input/@value"/>, <xsl:value-of select="number(form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="form/start/day/select/option[@selected = 'selected']/@value"/>)
                        }).attr("readonly", "readonly");
                        $("#bwEventWidgetStartDate").val('<xsl:value-of select="substring-before(form/start/rfc3339DateTime,'T')"/>');
                        */
                        </xsl:comment>
                      </script>
                      <input type="hidden" name="eventStartDate.year">
                        <xsl:attribute name="value"><xsl:value-of select="form/start/yearText/input/@value"/></xsl:attribute>
                      </input>
                      <input type="hidden" name="eventStartDate.month">
                        <xsl:attribute name="value"><xsl:value-of select="form/start/month/select/option[@selected = 'selected']/@value"/></xsl:attribute>
                      </input>
                      <input type="hidden" name="eventStartDate.day">
                        <xsl:attribute name="value"><xsl:value-of select="form/start/day/select/option[@selected = 'selected']/@value"/></xsl:attribute>
                      </input>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
                <div class="{$timeFieldsClass}" id="startTimeFields">
                  <span id="calWidgetStartTimeHider" class="show">
	                  <select name="eventStartDate.hour" id="eventStartDateHour">
	                    <xsl:copy-of select="form/start/hour/select/*"/>
	                  </select>
	                  <select name="eventStartDate.minute" id="eventStartDateMinute">
	                    <xsl:copy-of select="form/start/minute/select/*"/>
	                  </select>
	                  <xsl:if test="form/start/ampm">
	                    <select name="eventStartDate.ampm" id="eventStartDateAmpm">
	                      <xsl:copy-of select="form/start/ampm/select/*"/>
	                    </select>
	                  </xsl:if>
                    <xsl:text> </xsl:text>
                    <img src="{$resourcesRoot}/resources/clockIcon.gif" width="16" height="15" border="0" alt="bwClock" id="bwStartClock"/>

                    <select name="eventStartDate.tzid" id="startTzid" class="timezones">
                      <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                      <option value="-1"><xsl:copy-of select="$bwStr-FoEl-SelectTimezone"/></option>
                      <xsl:variable name="startTzId" select="form/start/tzid"/>
                      <xsl:for-each select="/bedework/timezones/timezone">
                        <option>
                          <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
                          <xsl:if test="$startTzId = id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                          <xsl:value-of select="name"/>
                        </option>
                      </xsl:for-each>
                    </select>
                  </span>
                </div>
              </div>
              <div class="dateStartEndBox">
                <strong>
                  <xsl:choose>
                    <xsl:when test="form/entityType = '2'"><xsl:copy-of select="$bwStr-FoEl-Due"/></xsl:when>
                    <xsl:otherwise><xsl:copy-of select="$bwStr-FoEl-End"/></xsl:otherwise>
                  </xsl:choose>
                </strong>
                <xsl:choose>
                  <xsl:when test="form/end/type='E'">
                    <input type="radio" name="eventEndType" id="bwEndDateTimeButton" value="E" checked="checked" onclick="changeClass('endDateTime','shown');changeClass('endDuration','invisible');"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <input type="radio" name="eventEndType" id="bwEndDateTimeButton" value="E" onclick="changeClass('endDateTime','shown');changeClass('endDuration','invisible');"/>
                  </xsl:otherwise>
                </xsl:choose>
	              <label for="bwEndDateTimeButton">
	                <xsl:copy-of select="$bwStr-FoEl-Date"/>
	              </label>
                <xsl:variable name="endDateTimeClass">
                  <xsl:choose>
                    <xsl:when test="form/end/type='E'">shown</xsl:when>
                    <xsl:otherwise>invisible</xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <div class="{$endDateTimeClass}" id="endDateTime">
                  <div class="dateFields">
                    <xsl:choose>
                      <xsl:when test="$portalFriendly = 'true'">
                        <xsl:copy-of select="/bedework/formElements/form/end/dateTime/month/*"/>
                        <xsl:copy-of select="/bedework/formElements/form/end/dateTime/day/*"/>
                        <xsl:choose>
                          <xsl:when test="/bedework/creating = 'true'">
                            <xsl:copy-of select="/bedework/formElements/form/end/dateTime/year/*"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:copy-of select="/bedework/formElements/form/end/dateTime/yearText/*"/>
                          </xsl:otherwise>
                        </xsl:choose>
                        <script language="JavaScript" type="text/javascript">
                        <xsl:comment>
                          endDateDynCalWidget = new dynCalendar('endDateDynCalWidget', <xsl:value-of select="number(/bedework/formElements/form/start/yearText/input/@value)"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected='selected']/@value)-1"/>, <xsl:value-of select="number(/bedework/formElements/form/start/day/select/option[@selected='selected']/@value)"/>, 'endDateCalWidgetCallback', '<xsl:value-of select="$resourcesRoot"/>/resources/');
                        </xsl:comment>
                        </script>
                      </xsl:when>
                      <xsl:otherwise>
                        <input type="text" name="bwEventWidgetEndDate" id="bwEventWidgetEndDate" size="10"/>
                        <script language="JavaScript" type="text/javascript">
                          <xsl:comment>
                          /*$("#bwEventWidgetEndDate").datepicker({
                            defaultDate: new Date(<xsl:value-of select="form/end/dateTime/yearText/input/@value"/>, <xsl:value-of select="number(form/end/dateTime/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="form/end/dateTime/day/select/option[@selected = 'selected']/@value"/>)
                          }).attr("readonly", "readonly");
                          $("#bwEventWidgetEndDate").val('<xsl:value-of select="substring-before(form/end/rfc3339DateTime,'T')"/>');
                          */
                          </xsl:comment>
                        </script>
                        <input type="hidden" name="eventEndDate.year">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/dateTime/yearText/input/@value"/></xsl:attribute>
                        </input>
                        <input type="hidden" name="eventEndDate.month">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/dateTime/month/select/option[@selected = 'selected']/@value"/></xsl:attribute>
                        </input>
                        <input type="hidden" name="eventEndDate.day">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/dateTime/day/select/option[@selected = 'selected']/@value"/></xsl:attribute>
                        </input>
                      </xsl:otherwise>
                    </xsl:choose>
                  </div>
                  <div class="{$timeFieldsClass}" id="endTimeFields">
                    <span id="calWidgetEndTimeHider" class="show">
	                    <select name="eventEndDate.hour" id="eventEndDateHour">
	                      <xsl:copy-of select="form/end/dateTime/hour/select/*"/>
	                    </select>
	                    <select name="eventEndDate.minute" id="eventEndDateMinute">
	                      <xsl:copy-of select="form/end/dateTime/minute/select/*"/>
	                    </select>
	                    <xsl:if test="form/start/ampm">
	                      <select name="eventEndDate.ampm" id="eventEndDateAmpm">
	                        <xsl:copy-of select="form/end/dateTime/ampm/select/*"/>
	                      </select>
	                    </xsl:if>
                      <xsl:text> </xsl:text>
                      <img src="{$resourcesRoot}/resources/clockIcon.gif" width="16" height="15" border="0" alt="bwClock" id="bwEndClock"/>

                      <select name="eventEndDate.tzid" id="endTzid" class="timezones">
                        <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                        <option value="-1"><xsl:copy-of select="$bwStr-FoEl-SelectTimezone"/></option>
                        <xsl:variable name="endTzId" select="form/end/dateTime/tzid"/>
                        <xsl:for-each select="/bedework/timezones/timezone">
                          <option>
                            <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
                              <xsl:if test="$endTzId = id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                            <xsl:value-of select="name"/>
                          </option>
                        </xsl:for-each>
                      </select>
                    </span>
                  </div>
                </div><br/>
                <div id="clock" class="invisible">
                  <xsl:call-template name="clock"/>
                </div>
                <div class="dateFields">
                  <xsl:choose>
                    <xsl:when test="form/end/type='D'">
                      <input type="radio" name="eventEndType" id="bwEndDurationButton" value="D" checked="checked" onclick="changeClass('endDateTime','invisible');changeClass('endDuration','shown');"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <input type="radio" name="eventEndType" id="bwEndDurationButton" value="D" onclick="changeClass('endDateTime','invisible');changeClass('endDuration','shown');"/>
                    </xsl:otherwise>
                  </xsl:choose>
	                <label for="bwEndDurationButton">
	                  <xsl:copy-of select="$bwStr-FoEl-Duration"/>
	                </label>
                  <xsl:variable name="endDurationClass">
                    <xsl:choose>
                      <xsl:when test="form/end/type='D'">shown</xsl:when>
                      <xsl:otherwise>invisible</xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="durationHrMinClass">
                    <xsl:choose>
                      <xsl:when test="form/allDay/input/@checked='checked'">invisible</xsl:when>
                      <xsl:otherwise>shown</xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <div class="{$endDurationClass}" id="endDuration">
                    <xsl:choose>
                      <xsl:when test="form/end/duration/weeks/input/@value = '0'">
                      <!-- we are using day, hour, minute format -->
                      <!-- must send either no week value or week value of 0 (zero) -->
                        <div class="durationBox">
                          <input type="radio" name="eventDuration.type" value="daytime" onclick="swapDurationType('daytime')" checked="checked"/>
                          <xsl:variable name="daysStr" select="form/end/duration/days/input/@value"/>
                          <input type="text" name="eventDuration.daysStr" size="2" value="{$daysStr}" id="durationDays"/><xsl:copy-of select="$bwStr-FoEl-Days"/>
                          <span id="durationHrMin" class="{$durationHrMinClass}">
                            <xsl:variable name="hoursStr" select="form/end/duration/hours/input/@value"/>
                            <input type="text" name="eventDuration.hoursStr" size="2" value="{$hoursStr}" id="durationHours"/><xsl:copy-of select="$bwStr-FoEl-Hours"/>
                            <xsl:variable name="minutesStr" select="form/end/duration/minutes/input/@value"/>
                            <input type="text" name="eventDuration.minutesStr" size="2" value="{$minutesStr}" id="durationMinutes"/><xsl:copy-of select="$bwStr-FoEl-Minutes"/>
                          </span>
                        </div>
                        <span class="durationSpacerText"><xsl:copy-of select="$bwStr-FoEl-Or"/></span>
                        <div class="durationBox">
                          <input type="radio" name="eventDuration.type" value="weeks" onclick="swapDurationType('week')"/>
                          <xsl:variable name="weeksStr" select="form/end/duration/weeks/input/@value"/>
                          <input type="text" name="eventDuration.weeksStr" size="2" value="{$weeksStr}" id="durationWeeks" disabled="disabled"/><xsl:copy-of select="$bwStr-FoEl-Weeks"/>
                        </div>
                      </xsl:when>
                      <xsl:otherwise>
                        <!-- we are using week format -->
                        <div class="durationBox">
                          <input type="radio" name="eventDuration.type" value="daytime" onclick="swapDurationType('daytime')"/>
                          <xsl:variable name="daysStr" select="form/end/duration/days/input/@value"/>
                          <input type="text" name="eventDuration.daysStr" size="2" value="{$daysStr}" id="durationDays" disabled="disabled"/><xsl:copy-of select="$bwStr-FoEl-Days"/>
                          <span id="durationHrMin" class="{$durationHrMinClass}">
                            <xsl:variable name="hoursStr" select="form/end/duration/hours/input/@value"/>
                            <input type="text" name="eventDuration.hoursStr" size="2" value="{$hoursStr}" id="durationHours" disabled="disabled"/><xsl:copy-of select="$bwStr-FoEl-Hours"/>
                            <xsl:variable name="minutesStr" select="form/end/duration/minutes/input/@value"/>
                            <input type="text" name="eventDuration.minutesStr" size="2" value="{$minutesStr}" id="durationMinutes" disabled="disabled"/><xsl:copy-of select="$bwStr-FoEl-Minutes"/>
                          </span>
                        </div>
                        <span class="durationSpacerText"><xsl:copy-of select="$bwStr-FoEl-Or"/></span>
                        <div class="durationBox">
                          <input type="radio" name="eventDuration.type" value="weeks" onclick="swapDurationType('week')" checked="checked"/>
                          <xsl:variable name="weeksStr" select="form/end/duration/weeks/input/@value"/>
                          <input type="text" name="eventDuration.weeksStr" size="2" value="{$weeksStr}" id="durationWeeks"/><xsl:copy-of select="$bwStr-FoEl-Weeks"/>
                        </div>
                      </xsl:otherwise>
                    </xsl:choose>
                  </div>
                </div><br/>
                <div class="dateFields" id="noDuration">
                  <xsl:choose>
                    <xsl:when test="form/end/type='N'">
                      <input type="radio" name="eventEndType" id="bwEndNoneButton" value="N" checked="checked" onclick="changeClass('endDateTime','invisible');changeClass('endDuration','invisible');"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <input type="radio" name="eventEndType" id="bwEndNoneButton" value="N" onclick="changeClass('endDateTime','invisible');changeClass('endDuration','invisible');"/>
                    </xsl:otherwise>
                  </xsl:choose>
                  <label for="bwEndNoneButton">
	                  <xsl:copy-of select="$bwStr-FoEl-This"/><xsl:text> </xsl:text>
	                  <xsl:choose>
	                    <xsl:when test="form/entityType = '2'"><xsl:copy-of select="$bwStr-FoEl-Task"/></xsl:when>
	                    <xsl:otherwise><xsl:copy-of select="$bwStr-FoEl-Event"/></xsl:otherwise>
	                  </xsl:choose>
	                  <xsl:text> </xsl:text>
                    <xsl:copy-of select="$bwStr-FoEl-HasNoDurationEndDate"/>
                  </label>
                </div>
              </div>
            </td>
          </tr>

          <!--  Description  -->
          <tr>
            <td class="fieldname"><xsl:copy-of select="$bwStr-FoEl-Description"/></td>
            <td class="fieldval">
              <div id="bwEventDescNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-MustIncludeDescription"/></div> <!-- a holder for validation notes -->
              <xsl:choose>
                <xsl:when test="normalize-space(form/desc/textarea) = ''">
                  <textarea name="description" cols="60" rows="4" id="bwEventDesc">
                    <xsl:text> </xsl:text>
                  </textarea>
                  <!-- keep this space to avoid browser
                  rendering errors when the text area is empty -->
                </xsl:when>
                <xsl:otherwise>
                  <textarea name="description" cols="60" rows="4" id="bwEventDesc">
                    <xsl:value-of select="form/desc/textarea"/>
                  </textarea>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
          <!--  Status  -->
          <!-- <tr>
            <td class="fieldname">
              <xsl:copy-of select="$bwStr-FoEl-Status"/>
            </td>
            <td class="fieldval">
              <input type="radio" name="eventStatus" value="CONFIRMED">
                <xsl:if test="form/status = 'CONFIRMED' or /bedework/creating = 'true' or form/status = ''"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <xsl:copy-of select="$bwStr-FoEl-Confirmed"/>
              <input type="radio" name="eventStatus" value="TENTATIVE">
                <xsl:if test="form/status = 'TENTATIVE'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <xsl:copy-of select="$bwStr-FoEl-Tentative"/>
              <input type="radio" name="eventStatus" value="CANCELLED">
                <xsl:if test="form/status = 'CANCELLED'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <xsl:copy-of select="$bwStr-FoEl-Canceled"/>
            </td>
          </tr> -->
          <!-- Cost -->
          <tr>
            <td class="fieldname"><em><xsl:copy-of select="$bwStr-FoEl-Cost"/></em></td>
            <td class="fieldval">
              <input type="text" name="eventCost" size="30" value="">
                <xsl:attribute name="value"><xsl:value-of select="form/cost/input/@value"/></xsl:attribute>
              </input>
              <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-CostOptional"/></span>
            </td>
          </tr>
          <!--  Link (url associated with event)  -->
          <tr>
            <td class="fieldname"><em><xsl:copy-of select="$bwStr-FoEl-EventURL"/></em></td>
            <td class="fieldval">
              <input type="text" name="eventLink" size="30" value="">
                <xsl:attribute name="value"><xsl:value-of select="form/link/input/@value"/></xsl:attribute>
              </input>
              <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-EventURLOptional"/></span>
            </td>
          </tr>
          <!-- Image Url -->
          <tr>
            <td class="fieldname"><em><xsl:copy-of select="$bwStr-FoEl-ImageURL"/></em></td>
            <td class="fieldval">
              <input type="text" name="xBwImageHolder" size="30" value="">
                <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-IMAGE']/values/text" disable-output-escaping="yes"/></xsl:attribute>
              </input>
              <xsl:text> </xsl:text>
              <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-ImageURLOptional"/></span>
            </td>
          </tr>
        </table>
      </div>

      <!-- Location tab -->
      <!-- ============== -->
      <div id="bwEventTab-Location" class="invisible">
        <div id="bwLocationUidNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-MustSelectLocation"/></div>
        <div class="mainForm">
          <span id="eventFormLocationList">
            <select name="locationUid" class="bigSelect" id="bwLocationUid">
              <option value=""><xsl:copy-of select="$bwStr-FoEl-SelectExistingLocation"/></option>
              <xsl:copy-of select="form/location/locationmenu/select/*"/>
            </select>
          </span>
        </div>
        <p class="subFormMessage">
          <xsl:copy-of select="$bwStr-FoEl-DidntFindLocation"/>
        </p>
        <div class="subForm">
          <p>
            <label for="commentLocationAddress"><xsl:copy-of select="$bwStr-FoEl-Address"/><xsl:text> </xsl:text></label>
            <input type="text" name="commentLocationAddress" id="bwCommentLocationAddress">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
          </p>
          <p>
            <label for="commentLocationSubaddress"><em><xsl:copy-of select="$bwStr-FoEl-SubAddress"/></em> </label>
            <input type="text" name="commentLocationSubaddress" id="commentLocationSubaddress">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/parameters/node()[name()='X-BEDEWORK-PARAM-SUBADDRESS']" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-Optional"/></span>
          </p>
          <p>
            <label for="commentLocationURL"><em><xsl:copy-of select="$bwStr-FoEl-URL"/></em> </label>
            <input type="text" name="commentLocationURL" id="commentLocationURL">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/parameters/node()[name()='X-BEDEWORK-PARAM-URL']" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-Optional"/></span>
          </p>
        </div>
      </div>

      <!-- Contact tab -->
      <!-- ============== -->
      <div id="bwEventTab-Contact" class="invisible">
        <div id="bwContactUidNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-MustSelectContact"/></div>
        <div class="mainForm">
          <select name="contactUid" id="bwContactUid" class="bigSelect">
            <option value="">
              <xsl:copy-of select="$bwStr-FoEl-SelectExistingContact"/>
            </option>
            <xsl:copy-of select="form/contact/all/select/*"/>
          </select>
        </div>
        <p class="subFormMessage">
          <xsl:copy-of select="$bwStr-FoEl-DidntFindContact"/>
        </p>
        <div class="subForm">
          <p>
            <label for="commentContactName"><xsl:copy-of select="$bwStr-FoEl-OrganizationName"/><xsl:text> </xsl:text> </label>
            <input type="text" name="commentContactName" id="bwCommentContactName" size="40">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-PleaseLimitContacts"/></span>
          </p>
          <p>
            <label for="commentContactPhone"><em><xsl:copy-of select="$bwStr-FoEl-Phone"/></em> </label>
            <input type="text" name="commentContactPhone">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-PHONE']" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-Optional"/></span>
          </p>
          <p>
            <label for="commentContactURL"><em><xsl:copy-of select="$bwStr-FoEl-URL"/></em> </label>
            <input type="text" name="commentContactURL">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-URL']" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-Optional"/></span>
          </p>
          <p>
            <label for="commentContactEmail"><em><xsl:copy-of select="$bwStr-FoEl-Email"/></em> </label>
            <input type="text" name="commentContactEmail">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-EMAIL']" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <span class="note"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-FoEl-Optional"/></span>
          </p>
        </div>
      </div>

      <!-- Topical areas tab -->
      <!-- ================= -->
      <div id="bwEventTab-Categories" class="invisible">
        <!-- Topical area  -->
        <!-- These are the subscriptions (aliases) where the events should show up.
             By selecting one or more of these, appropriate categories will be set on the event -->
        <ul class="aliasTree">
          <!-- hard coding the "aliases" name is not best, but will do for now -->
          <xsl:apply-templates select="form/calendars/calendar/calendar[name='aliases']" mode="showEventFormAliases">
            <xsl:with-param name="root">true</xsl:with-param>
          </xsl:apply-templates>
        </ul>
        <p class="subFormMessage">
          <xsl:copy-of select="$bwStr-FoEl-MissingTopicalArea"/>
        </p>
        <div class="subForm">
          <p>
            <label for="commentCategories"><xsl:copy-of select="$bwStr-FoEl-TypeOfEvent"/><xsl:text> </xsl:text></label>
            <input type="text" name="commentCategories" size="80">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CATEGORIES']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
          </p>
        </div>
        <!-- xsl:variable name="catCount" select="count(form/categories/all/category)"/>
        <xsl:choose>
          <xsl:when test="not(form/categories/all/category)">
            no categories defined
          </xsl:when>
          <xsl:otherwise>
            <table cellpadding="0" id="allCategoryCheckboxes">
              <tr>
                <td>
                  <xsl:for-each select="form/categories/all/category[position() &lt;= ceiling($catCount div 2)]">
                    <input type="checkbox" name="catUid">
                      <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                      <xsl:if test="uid = ../../current//category/uid"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                      <xsl:value-of select="value"/>
                    </input><br/>
                  </xsl:for-each>
                </td>
                <td>
                  <xsl:for-each select="form/categories/all/category[position() &gt; ceiling($catCount div 2)]">
                    <input type="checkbox" name="catUid">
                      <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                      <xsl:if test="uid = ../../current//category/uid"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                      <xsl:value-of select="value"/>
                    </input><br/>
                  </xsl:for-each>
                </td>
              </tr>
            </table>
          </xsl:otherwise>
        </xsl:choose>
        <p class="subFormMessage">
          Didn't find the category you want?  Suggest a new one:
        </p>
        <div class="subForm">
          <p>
            <label for="commentCategories">Category suggestion: </label>
            <input type="text" name="commentCategories" size="30">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-CATEGORIES']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
          </p>
        </div-->
      </div>

      <!--   Review tab   -->
      <!-- ============== -->
      <div id="bwEventTab-Review" class="invisible">
        <!--  <table id="bwEventSubmitReview" class="common">
          <tr>
            <th colspan="2">Event Details</th>
          </tr>-->
          <!-- the form elements will be inserted here -->
        <!-- </table>-->

        <div id="bwCommentNotes">
          <!-- holders for validation notes -->
          <div id="xBwEmailHolderNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-MustIncludeEmail"/></div>
          <div id="xBwEmailHolderInvalidNotice" class="invisible"><xsl:copy-of select="$bwStr-FoEl-InvalidEmailAddress"/></div>
          <p>
            <label for="xBwEmailHolder"><xsl:copy-of select="$bwStr-FoEl-EnterEmailAddress"/><xsl:text> </xsl:text></label><br/>
            <input type="text" name="xBwEmailHolder" id="xBwEmailHolder" size="80">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTER-EMAIL']/values/text"/></xsl:attribute>
            </input>
          </p>

          <p>
            <xsl:copy-of select="$bwStr-FoEl-FinalNotes"/><br/>
          <!-- note: don't remove the #160 from the textarea or browsers will see it as a closed tag when empty -->
           <textarea name="commentNotes" cols="60" rows="4"><!--
           --><xsl:value-of select="normalize-space(form/xproperties/node()[name()='X-BEDEWORK-SUBMIT-COMMENT']/values/text)" disable-output-escaping="yes"/><!--
           --><xsl:if test="normalize-space(form/xproperties/node()[name()='X-BEDEWORK-SUBMIT-COMMENT']/values/text) = ''"><xsl:text> </xsl:text></xsl:if><!--
           --></textarea>
          </p>
        </div>
        <div class="eventSubmitButtons">
          <input name="submit" class="submit" type="submit" value="{$bwStr-FoEl-SubmitForApproval}"/>
          <input name="cancelled" type="submit" value="{$bwStr-FoEl-Cancel}"/>
        </div>
      </div>
    </div>

    <div id="bwBottomNav">
      <div id="bwBottomNav-Details">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location'); hide('bwEventTab-Details','bwHelp-Details','bwBottomNav-Details');"
             onclick="return validateStep1();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
      </div>
      <div id="bwBottomNav-Location" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Details','bwHelp-Details','bwBottomNav-Details'); hide('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwEventTab-Contact','bwHelp-Contact','bwBottomNav-Contact'); hide('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location');"
             onclick="return validateStep2();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
      </div>
      <div id="bwBottomNav-Contact" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Location','bwHelp-Location','bwBottomNav-Location'); hide('bwHelp-Contact','bwEventTab-Contact','bwBottomNav-Contact');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwEventTab-Categories','bwHelp-Categories','bwBottomNav-Categories'); hide('bwHelp-Contact','bwEventTab-Contact','bwBottomNav-Contact');"
             onclick="return validateStep3();">
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
      </div>
      <div id="bwBottomNav-Categories" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwEventTab-Contact','bwHelp-Contact','bwBottomNav-Contact'); hide('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories');">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a> |
          <a href="javascript:show('bwHelp-Review','bwEventTab-Review','bwBottomNav-Review'); hide('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories');">
            <!-- displayReview('standardForm','bwEventSubmitReview',1) -->
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </a>
        </div>
      </div>
      <div id="bwBottomNav-Review" class="invisible">
        <div class="navButtons">
          <a href="javascript:show('bwHelp-Categories','bwEventTab-Categories','bwBottomNav-Categories'); hide('bwHelp-Review','bwEventTab-Review','bwBottomNav-Review'); ">
            <img alt="{$bwStr-FoEl-Previous}"
              src="{$resourcesRoot}/resources/arrowLeft.gif"
              width="13"
              height="13"
              border="0"/>
            <xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Previous"/>
          </a>
          <span class="hidden">
            <xsl:text> </xsl:text>|<xsl:text> </xsl:text>
            <xsl:copy-of select="$bwStr-FoEl-Next"/>
            <xsl:text> </xsl:text>
            <img alt="{$bwStr-FoEl-Next}"
              src="{$resourcesRoot}/resources/arrowRight.gif"
              width="13"
              height="13"
              border="0"/>
          </span>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="calendar" mode="showEventFormAliases">
    <xsl:param name="root">false</xsl:param>
    <li>
      <xsl:if test="$root != 'true'">
        <!-- hide the root calendar. -->
        <xsl:choose>
          <xsl:when test="calType = '7' or calType = '8'">
            <!-- we've hit an unresolvable alias; stop descending -->
            <input type="checkbox" name="forDiplayOnly" disabled="disabled"/>
            <em><xsl:value-of select="summary"/>?</em>
          </xsl:when>
          <xsl:when test="calType = '0'">
            <!-- no direct selecting of folders or folder aliases: we only want users to select the
                 underlying calendar aliases -->
            <img src="{$resourcesRoot}/resources/catIcon.gif" width="13" height="13" alt="folder" class="folderForAliasTree" border="0"/>
            <xsl:value-of select="summary"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="virtualPath"><xsl:for-each select="ancestor-or-self::calendar/name">/<xsl:value-of select="."/></xsl:for-each></xsl:variable>
            <xsl:variable name="displayName" select="summary"/>
            <input type="checkbox" name="alias" id="{generate-id(path)}" onclick="toggleBedeworkXProperty('X-BEDEWORK-SUBMIT-ALIAS','{$displayName}','{$virtualPath}',this.checked)">
              <xsl:attribute name="value"><xsl:value-of select="$virtualPath"/></xsl:attribute>
              <xsl:if test="$virtualPath = /bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text"><xsl:attribute name="checked"><xsl:value-of select="checked"/></xsl:attribute></xsl:if>
            </input>
            <label for="{generate-id(path)}">
	            <xsl:choose>
	              <xsl:when test="$virtualPath = /bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text">
	                <strong><xsl:value-of select="summary"/></strong>
	              </xsl:when>
	              <xsl:otherwise>
	                <xsl:value-of select="summary"/>
	              </xsl:otherwise>
	            </xsl:choose>
            </label>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <xsl:if test="calendar">
        <ul>
          <xsl:apply-templates select="calendar" mode="showEventFormAliases"/>
        </ul>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="val" mode="weekMonthYearNumbers">
    <xsl:if test="position() != 1 and position() = last()"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-WMYN-Next"/><xsl:text> </xsl:text></xsl:if>
    <xsl:value-of select="."/><xsl:choose>
      <xsl:when test="substring(., string-length(.)-1, 2) = '11' or
                      substring(., string-length(.)-1, 2) = '12' or
                      substring(., string-length(.)-1, 2) = '13'">th</xsl:when>
      <xsl:when test="substring(., string-length(.), 1) = '1'">st</xsl:when>
      <xsl:when test="substring(., string-length(.), 1) = '2'">nd</xsl:when>
      <xsl:when test="substring(., string-length(.), 1) = '3'">rd</xsl:when>
      <xsl:otherwise>th</xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">, </xsl:if>
  </xsl:template>

  <xsl:template name="byDayChkBoxList">
    <xsl:param name="name"/>
    <xsl:for-each select="/bedework/shortdaynames/val">
      <xsl:variable name="pos" select="position()"/>
      <input type="checkbox">
        <xsl:attribute name="value"><xsl:value-of select="/bedework/recurdayvals/val[position() = $pos]"/></xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
      </input>
      <xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="buildCheckboxList">
    <xsl:param name="current"/>
    <xsl:param name="end"/>
    <xsl:param name="name"/>
    <xsl:param name="splitter">10</xsl:param>
    <span class="chkBoxListItem">
      <input type="checkbox">
        <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$current"/></xsl:attribute>
      </input>
      <xsl:value-of select="$current"/>
    </span>
    <xsl:if test="$current mod $splitter = 0"><br/></xsl:if>
    <xsl:if test="$current = $end"><br/></xsl:if>
    <xsl:if test="$current &lt; $end">
      <xsl:call-template name="buildCheckboxList">
        <xsl:with-param name="current"><xsl:value-of select="$current + 1"/></xsl:with-param>
        <xsl:with-param name="end"><xsl:value-of select="$end"/></xsl:with-param>
        <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="recurrenceDayPosOptions">
    <option value="0"><xsl:copy-of select="$bwStr-RDPO-None"/></option>
    <option value="1"><xsl:copy-of select="$bwStr-RDPO-TheFirst"/></option>
    <option value="2"><xsl:copy-of select="$bwStr-RDPO-TheSecond"/></option>
    <option value="3"><xsl:copy-of select="$bwStr-RDPO-TheThird"/></option>
    <option value="4"><xsl:copy-of select="$bwStr-RDPO-TheFourth"/></option>
    <option value="5"><xsl:copy-of select="$bwStr-RDPO-TheFifth"/></option>
    <option value="-1"><xsl:copy-of select="$bwStr-RDPO-TheLast"/></option>
    <option value=""><xsl:copy-of select="$bwStr-RDPO-Every"/></option>
  </xsl:template>

  <xsl:template name="buildRecurFields">
    <xsl:param name="current"/>
    <xsl:param name="total"/>
    <xsl:param name="name"/>
    <div class="invisible">
      <xsl:attribute name="id"><xsl:value-of select="$name"/>RecurFields<xsl:value-of select="$current"/></xsl:attribute>
      <xsl:copy-of select="$bwStr-BReF-And"/>
      <select width="12em">
        <xsl:attribute name="name">by<xsl:value-of select="$name"/>posPos<xsl:value-of select="$current"/></xsl:attribute>
        <xsl:if test="$current != $total">
          <xsl:attribute name="onchange">changeClass('<xsl:value-of select="$name"/>RecurFields<xsl:value-of select="$current+1"/>','shown')</xsl:attribute>
        </xsl:if>
        <xsl:call-template name="recurrenceDayPosOptions"/>
      </select>
      <xsl:call-template name="byDayChkBoxList"/>
    </div>
    <xsl:if test="$current &lt; $total">
      <xsl:call-template name="buildRecurFields">
        <xsl:with-param name="current"><xsl:value-of select="$current+1"/></xsl:with-param>
        <xsl:with-param name="total"><xsl:value-of select="$total"/></xsl:with-param>
        <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="buildNumberOptions">
    <xsl:param name="current"/>
    <xsl:param name="total"/>
    <option value="{$current}"><xsl:value-of select="$current"/></option>
    <xsl:if test="$current &lt; $total">
      <xsl:call-template name="buildNumberOptions">
        <xsl:with-param name="current"><xsl:value-of select="$current+1"/></xsl:with-param>
        <xsl:with-param name="total"><xsl:value-of select="$total"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="clock">
    <div id="bwClock">
      <!-- Bedework 24-Hour Clock time selection widget
           used with resources/bwClock.js and resources/bwClock.css -->
      <xsl:variable name="hour24" select="/bedework/hour24"/><!-- true or false -->
      <div id="bwClockClock">
        <img id="clockMap" src="{$resourcesRoot}/resources/clockMap.gif" width="368" height="368" border="0" alt="bwClock" usemap="#bwClockMap" />
      </div>
      <div id="bwClockCover">
        &#160;
        <!-- this is a special effect div used simply to cover the pixelated edge
             where the clock meets the clock box title -->
      </div>
      <div id="bwClockBox">
        <h2>
          <xsl:copy-of select="$bwStr-Cloc-Bedework24HourClock"/>
        </h2>
        <div id="bwClockDateTypeIndicator">
          <xsl:copy-of select="$bwStr-Cloc-Type"/>
        </div>
        <div id="bwClockTime">
          <xsl:copy-of select="$bwStr-Cloc-SelectTime"/>
        </div>
        <div id="bwClockSwitch">
          <xsl:copy-of select="$bwStr-Cloc-Switch"/>
        </div>
        <div id="bwClockCloseText">
          <xsl:copy-of select="$bwStr-Cloc-Close"/>
        </div>
        <div id="bwClockCloseButton">
          <a href="javascript:bwClockClose();">X</a>
        </div>
      </div>
      <map name="bwClockMap" id="bwClockMap">
        <area shape="rect" alt="close clock" title="close clock" coords="160,167, 200,200" href="javascript:bwClockClose()"/>
        <area shape="poly" alt="minute 00:55" title="minute 00:55" coords="156,164, 169,155, 156,107, 123,128" href="javascript:bwClockUpdateDateTimeForm('minute','55')" />
        <area shape="poly" alt="minute 00:50" title="minute 00:50" coords="150,175, 156,164, 123,128, 103,161" href="javascript:bwClockUpdateDateTimeForm('minute','50')" />
        <area shape="poly" alt="minute 00:45" title="minute 00:45" coords="150,191, 150,175, 103,161, 103,206" href="javascript:bwClockUpdateDateTimeForm('minute','45')" />
        <area shape="poly" alt="minute 00:40" title="minute 00:40" coords="158,208, 150,191, 105,206, 123,237" href="javascript:bwClockUpdateDateTimeForm('minute','40')" />
        <area shape="poly" alt="minute 00:35" title="minute 00:35" coords="171,218, 158,208, 123,238, 158,261" href="javascript:bwClockUpdateDateTimeForm('minute','35')" />
        <area shape="poly" alt="minute 00:30" title="minute 00:30" coords="193,218, 172,218, 158,263, 209,263" href="javascript:bwClockUpdateDateTimeForm('minute','30')" />
        <area shape="poly" alt="minute 00:25" title="minute 00:25" coords="209,210, 193,218, 209,261, 241,240" href="javascript:bwClockUpdateDateTimeForm('minute','25')" />
        <area shape="poly" alt="minute 00:20" title="minute 00:20" coords="216,196, 209,210, 241,240, 261,206" href="javascript:bwClockUpdateDateTimeForm('minute','20')" />
        <area shape="poly" alt="minute 00:15" title="minute 00:15" coords="216,178, 216,196, 261,206, 261,159" href="javascript:bwClockUpdateDateTimeForm('minute','15')" />
        <area shape="poly" alt="minute 00:10" title="minute 00:10" coords="209,164, 216,178, 261,159, 240,126" href="javascript:bwClockUpdateDateTimeForm('minute','10')" />
        <area shape="poly" alt="minute 00:05" title="minute 00:05" coords="196,155, 209,164, 238,126, 206,107" href="javascript:bwClockUpdateDateTimeForm('minute','5')" />
        <area shape="poly" alt="minute 00:00" title="minute 00:00" coords="169,155, 196,155, 206,105, 156,105" href="javascript:bwClockUpdateDateTimeForm('minute','0')" />
        <area shape="poly" alt="11 PM, 2300 hour" title="11 PM, 2300 hour" coords="150,102, 172,96, 158,1, 114,14" href="javascript:bwClockUpdateDateTimeForm('hour','23',{$hour24})" />
        <area shape="poly" alt="10 PM, 2200 hour" title="10 PM, 2200 hour" coords="131,114, 150,102, 114,14, 74,36" href="javascript:bwClockUpdateDateTimeForm('hour','22',{$hour24})" />
        <area shape="poly" alt="9 PM, 2100 hour" title="9 PM, 2100 hour" coords="111,132, 131,114, 74,36, 40,69" href="javascript:bwClockUpdateDateTimeForm('hour','21',{$hour24})" />
        <area shape="poly" alt="8 PM, 2000 hour" title="8 PM, 2000 hour" coords="101,149, 111,132, 40,69, 15,113" href="javascript:bwClockUpdateDateTimeForm('hour','20',{$hour24})" />
        <area shape="poly" alt="7 PM, 1900 hour" title="7 PM, 1900 hour" coords="95,170, 101,149, 15,113, 1,159" href="javascript:bwClockUpdateDateTimeForm('hour','19',{$hour24})" />
        <area shape="poly" alt="6 PM, 1800 hour" title="6 PM, 1800 hour" coords="95,196, 95,170, 0,159, 0,204" href="javascript:bwClockUpdateDateTimeForm('hour','18',{$hour24})" />
        <area shape="poly" alt="5 PM, 1700 hour" title="5 PM, 1700 hour" coords="103,225, 95,196, 1,205, 16,256" href="javascript:bwClockUpdateDateTimeForm('hour','17',{$hour24})" />
        <area shape="poly" alt="4 PM, 1600 hour" title="4 PM, 1600 hour" coords="116,245, 103,225, 16,256, 41,298" href="javascript:bwClockUpdateDateTimeForm('hour','16',{$hour24})" />
        <area shape="poly" alt="3 PM, 1500 hour" title="3 PM, 1500 hour" coords="134,259, 117,245, 41,298, 76,332" href="javascript:bwClockUpdateDateTimeForm('hour','15',{$hour24})" />
        <area shape="poly" alt="2 PM, 1400 hour" title="2 PM, 1400 hour" coords="150,268, 134,259, 76,333, 121,355" href="javascript:bwClockUpdateDateTimeForm('hour','14',{$hour24})" />
        <area shape="poly" alt="1 PM, 1300 hour" title="1 PM, 1300 hour" coords="169,273, 150,268, 120,356, 165,365" href="javascript:bwClockUpdateDateTimeForm('hour','13',{$hour24})" />
        <area shape="poly" alt="Noon, 1200 hour" title="Noon, 1200 hour" coords="193,273, 169,273, 165,365, 210,364" href="javascript:bwClockUpdateDateTimeForm('hour','12',{$hour24})" />
        <area shape="poly" alt="11 AM, 1100 hour" title="11 AM, 1100 hour" coords="214,270, 193,273, 210,363, 252,352" href="javascript:bwClockUpdateDateTimeForm('hour','11',{$hour24})" />
        <area shape="poly" alt="10 AM, 1000 hour" title="10 AM, 1000 hour" coords="232,259, 214,270, 252,352, 291,330" href="javascript:bwClockUpdateDateTimeForm('hour','10',{$hour24})" />
        <area shape="poly" alt="9 AM, 0900 hour" title="9 AM, 0900 hour" coords="251,240, 232,258, 291,330, 323,301" href="javascript:bwClockUpdateDateTimeForm('hour','9',{$hour24})" />
        <area shape="poly" alt="8 AM, 0800 hour" title="8 AM, 0800 hour" coords="263,219, 251,239, 323,301, 349,261" href="javascript:bwClockUpdateDateTimeForm('hour','8',{$hour24})" />
        <area shape="poly" alt="7 AM, 0700 hour" title="7 AM, 0700 hour" coords="269,194, 263,219, 349,261, 363,212" href="javascript:bwClockUpdateDateTimeForm('hour','7',{$hour24})" />
        <area shape="poly" alt="6 AM, 0600 hour" title="6 AM, 0600 hour" coords="269,172, 269,193, 363,212, 363,155" href="javascript:bwClockUpdateDateTimeForm('hour','6',{$hour24})" />
        <area shape="poly" alt="5 AM, 0500 hour" title="5 AM, 0500 hour" coords="263,150, 269,172, 363,155, 351,109" href="javascript:bwClockUpdateDateTimeForm('hour','5',{$hour24})" />
        <area shape="poly" alt="4 AM, 0400 hour" title="4 AM, 0400 hour" coords="251,130, 263,150, 351,109, 325,68" href="javascript:bwClockUpdateDateTimeForm('hour','4',{$hour24})" />
        <area shape="poly" alt="3 AM, 0300 hour" title="3 AM, 0300 hour" coords="234,112, 251,130, 325,67, 295,37" href="javascript:bwClockUpdateDateTimeForm('hour','3',{$hour24})" />
        <area shape="poly" alt="2 AM, 0200 hour" title="2 AM, 0200 hour" coords="221,102, 234,112, 295,37, 247,11" href="javascript:bwClockUpdateDateTimeForm('hour','2',{$hour24})" />
        <area shape="poly" alt="1 AM, 0100 hour" title="1 AM, 0100 hour" coords="196,96, 221,102, 247,10, 209,-1, 201,61, 206,64, 205,74, 199,75" href="javascript:bwClockUpdateDateTimeForm('hour','1',{$hour24})" />
        <area shape="poly" alt="Midnight, 0000 hour" title="Midnight, 0000 hour" coords="172,96, 169,74, 161,73, 161,65, 168,63, 158,-1, 209,-1, 201,61, 200,62, 206,64, 205,74, 198,75, 196,96, 183,95" href="javascript:bwClockUpdateDateTimeForm('hour','0',{$hour24})" />
      </map>
    </div>
  </xsl:template>

  <!--++++++++++++++++++ Events ++++++++++++++++++++-->
  <xsl:template name="eventList">
    <h1><xsl:copy-of select="$bwStr-EvLs-PendingEvents"/></h1>
    <p>
      <xsl:copy-of select="$bwStr-EvLs-EventsBelowWaiting"/>
    </p>
    <xsl:call-template name="eventListCommon"/>
  </xsl:template>

  <xsl:template name="eventListCommon">
    <table id="commonListTable">
      <tr>
        <th><xsl:copy-of select="$bwStr-EvLC-Title"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-ClaimedBy"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-Start"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-End"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-TopicalAreas"/></th>
        <th><xsl:copy-of select="$bwStr-EvLC-Description"/></th>
      </tr>

      <xsl:for-each select="/bedework/events/event">
        <xsl:variable name="calPath" select="calendar/encodedPath"/>
        <xsl:variable name="guid" select="guid"/>
        <xsl:variable name="recurrenceId" select="recurrenceId"/>
        <tr>
          <td>
            <a href="{$editEvent}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
              <xsl:choose>
                <xsl:when test="summary != ''">
                  <xsl:value-of select="summary"/>
                </xsl:when>
                <xsl:otherwise>
                  <em><xsl:copy-of select="$bwStr-EvLC-NoTitle"/></em>
                </xsl:otherwise>
              </xsl:choose>
            </a>
          </td>
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
          <td class="date">
            <xsl:value-of select="start/shortdate"/>
		        <xsl:text> </xsl:text>
		        <xsl:choose>
		          <xsl:when test="start/allday = 'false'">
		            <xsl:value-of select="start/time"/>
		          </xsl:when>
		          <xsl:otherwise>
		            <xsl:copy-of select="$bwStr-FoEl-AllDay"/>
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
		            <xsl:copy-of select="$bwStr-FoEl-AllDay"/>
		          </xsl:otherwise>
		        </xsl:choose>
          </td>
          <td>
            <xsl:for-each select="xproperties/X-BEDEWORK-SUBMIT-ALIAS">
              <xsl:value-of select="parameters/X-BEDEWORK-PARAM-DISPLAYNAME"/><br/> 
            </xsl:for-each>
          </td>
          <td>
            <xsl:value-of select="description"/>
            <xsl:if test="recurring = 'true' or recurrenceId != ''">
              <div class="recurrenceEditLinks">
                <xsl:copy-of select="$bwStr-EvLC-RecurringEvent"/>
                <xsl:copy-of select="$bwStr-EvLC-Edit"/>
                <xsl:text> </xsl:text>
                <a href="{$editEvent}&amp;calPath={$calPath}&amp;guid={$guid}">
                  <xsl:copy-of select="$bwStr-EvLC-Master"/>
                </a> |
                <a href="{$editEvent}&amp;calPath={$calPath}&amp;guid={$guid}&amp;recurrenceId={$recurrenceId}">
                  <xsl:copy-of select="$bwStr-EvLC-Instance"/>
                </a>
              </div>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!--==== UPLOAD ====-->
  <xsl:template name="upload">
  <!-- The name "eventForm" is referenced by several javascript functions. Do not
    change it without modifying bedework.js -->
    <form name="eventForm" method="post" action="{$upload}" id="standardForm"  enctype="multipart/form-data">
      <h2><xsl:copy-of select="$bwStr-Upld-UploadICalFile"/></h2>
      <table class="common" cellspacing="0">
        <tr>
          <td class="fieldname">
            <xsl:copy-of select="$bwStr-Upld-Filename"/>
          </td>
          <td align="left">
            <input type="file" name="uploadFile" size="60" />
          </td>
        </tr>
        <tr>
          <td class="fieldname padMeTop">
            <xsl:copy-of select="$bwStr-Upld-IntoCalendar"/>
          </td>
          <td align="left" class="padMeTop">
            <input type="hidden" name="newCalPath" id="bwNewCalPathField" value=""/>
            <span id="bwEventCalDisplay">
              <em><xsl:copy-of select="$bwStr-Upld-DefaultCalendar"/></em>
            </span>
          </td>
        </tr>
        <tr>
          <td class="fieldname padMeTop">
            <xsl:copy-of select="$bwStr-Upld-AffectsFreeBusy"/>
          </td>
          <td align="left" class="padMeTop">
            <input type="radio" value="" name="transparency" checked="checked"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-AcceptEventsSettings"/><br/>
            <input type="radio" value="OPAQUE" name="transparency"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-Opaque"/><xsl:text> </xsl:text><span class="note"><xsl:copy-of select="$bwStr-Upld-Yes"/></span><br/>
            <input type="radio" value="TRANSPARENT" name="transparency"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-No"/><xsl:text> </xsl:text><span class="note"><xsl:copy-of select="$bwStr-Upld-Transparent"/></span><br/>
          </td>
        </tr>
        <tr>
          <td class="fieldname padMeTop">
            <xsl:copy-of select="$bwStr-Upld-Status"/>
          </td>
          <td align="left" class="padMeTop">
            <input type="radio" value="" name="status" checked="checked"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-AcceptEventsStatus"/><br/>
            <input type="radio" value="CONFIRMED" name="status"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-Confirmed"/><br/>
            <input type="radio" value="TENTATIVE" name="status"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-Tentative"/><br/>
            <input type="radio" value="CANCELLED" name="status"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-Upld-Canceled"/><br/>
          </td>
        </tr>
      </table>
      <table border="0" id="submitTable">
        <tr>
          <td>
            <input name="submit" type="submit" value="{$bwStr-Upld-Continue}"/>
            <input name="cancelled" type="submit" value="{$bwStr-Upld-Cancel}"/>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>

  <!--==== UTILITY TEMPLATES ====-->

  <!-- time formatter (should be extended as needed) -->
  <xsl:template name="timeFormatter">
    <xsl:param name="timeString"/><!-- required -->
    <xsl:param name="showMinutes">yes</xsl:param>
    <xsl:param name="showAmPm">yes</xsl:param>
    <xsl:param name="hour24">no</xsl:param>
    <xsl:variable name="hour" select="number(substring($timeString,1,2))"/>
    <xsl:variable name="minutes" select="substring($timeString,3,2)"/>
    <xsl:variable name="AmPm">
      <xsl:choose>
        <xsl:when test="$hour &lt; 12"><xsl:copy-of select="$bwStr-TiFo-AM"/></xsl:when>
        <xsl:otherwise><xsl:copy-of select="$bwStr-TiFo-PM"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="hour24 = 'yes'">
        <xsl:value-of select="$hour"/><!--
     --><xsl:if test="$showMinutes = 'yes'">:<xsl:value-of select="$minutes"/></xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$hour = 0">12</xsl:when>
          <xsl:when test="$hour &lt; 13"><xsl:value-of select="$hour"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$hour - 12"/></xsl:otherwise>
        </xsl:choose><!--
     --><xsl:if test="$showMinutes = 'yes'">:<xsl:value-of select="$minutes"/></xsl:if>
        <xsl:if test="$showAmPm = 'yes'">
          <xsl:text> </xsl:text>
          <xsl:value-of select="$AmPm"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--==== FOOTER ====-->
  <xsl:template name="footer">
    <div id="footer">
      <xsl:copy-of select="$bwStr-Foot-BasedOnThe"/><xsl:text> </xsl:text><a href="http://www.jasig.org/bedework"><xsl:copy-of select="$bwStr-Foot-BedeworkCalendarSystem"/></a>
    </div>
    <div id="subfoot">
      <a href="http://www.jasig.org/bedework"><xsl:copy-of select="$bwStr-Foot-BedeworkWebsite"/></a> |
      <a href="?noxslt=yes"><xsl:copy-of select="$bwStr-Foot-ShowXML"/></a> |
      <a href="?refreshXslt=yes"><xsl:copy-of select="$bwStr-Foot-RefreshXSLT"/></a>
    </div>
  </xsl:template>

</xsl:stylesheet>
