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
  
  <!--++++++++++++++++++ Add/Edit Event Form ++++++++++++++++++++-->
  <!-- templates: 
         - modEvent (the base of the add/edit event form)
         - showEventFormAliases (build the topical area listing in the event form)
         - submitEventButtons
         
         - weekMonthYearNumbers (utility template)
         - byDayChkBoxList (utility template)
         - buildCheckboxList (utility template)
         - recurrenceDayPosOptions (utility template)
         - buildRecurFields (utility template)
         - buildNumberOptions (utility template)
   -->
  
  <xsl:template match="formElements" mode="modEvent">
    <xsl:variable name="calPathEncoded" select="form/calendar/event/encodedPath"/>
    <xsl:variable name="calPath" select="form/calendar/event/path"/>
    <xsl:variable name="guid" select="guid"/>
    <xsl:variable name="recurrenceId" select="recurrenceId"/>
    <xsl:variable name="eventTitle" select="form/title/input/@value"/>
    <xsl:variable name="eventUrlPrefix"><xsl:value-of select="$publicCal"/>/event/eventView.do?guid=<xsl:value-of select="$guid"/>&amp;recurrenceId=<xsl:value-of select="$recurrenceId"/></xsl:variable>
    <xsl:variable name="userPath"><xsl:value-of select="/bedework/syspars/userPrincipalRoot"/>/<xsl:value-of select="/bedework/userInfo/user"/></xsl:variable>

    <!-- Determine if the current user can edit this event.
         If canEdit is false, we will only allow tagging by topical area,
         and other fields will be disabled. -->
    <xsl:variable name="canEdit">
      <xsl:choose>
        <xsl:when test="($userPath = creator) or (/bedework/page = 'modEventPending') or (/bedework/userInfo/superUser = 'true') or (/bedework/creating = 'true')">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <h2><xsl:copy-of select="$bwStr-AEEF-EventInfo"/></h2>

    <xsl:if test="$canEdit = 'false'">
      <p><xsl:copy-of select="$bwStr-AEEF-YouMayTag"/></p>
    </xsl:if>

    <xsl:if test="/bedework/page = 'modEventPending'">
      <!-- if a submitted event has topical areas that match with 
           those in the calendar suite, convert them -->
      <script type="text/javascript">
      $(document).ready(function() {
        $("ul.aliasTree input:checked").trigger("onclick");
      });
      </script>
    
      <!-- if a submitted event has comments, display them -->
      <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-LOCATION' or name()='X-BEDEWORK-CONTACT' or name()='X-BEDEWORK-CATEGORIES' or name()='X-BEDEWORK-SUBMIT-COMMENT']">
        <script type="text/javascript">
          bwSubmitComment = new bwSubmitComment(
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/values/text"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/parameters/node()[name()='X-BEDEWORK-PARAM-SUBADDRESS']"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-LOCATION']/parameters/node()[name()='X-BEDEWORK-PARAM-URL']"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/values/text"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-PHONE']"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-URL']"/></xsl:call-template>',
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-CONTACT']/parameters/node()[name()='X-BEDEWORK-PARAM-EMAIL']"/></xsl:call-template>',
            '<xsl:for-each select="form/xproperties/node()[name()='X-BEDEWORK-SUBMIT-ALIAS']"><xsl:call-template name="escapeApos"><xsl:with-param name="str"><xsl:value-of select="parameters/X-BEDEWORK-PARAM-DISPLAYNAME"/></xsl:with-param></xsl:call-template><br/></xsl:for-each>', 
            '<xsl:call-template name="escapeApos"><xsl:with-param name="str" select="form/xproperties/node()[name()='X-BEDEWORK-CATEGORIES']/values/text"/></xsl:call-template>',
            '<xsl:call-template name="escapeJson"><xsl:with-param name="string" select="form/xproperties/node()[name()='X-BEDEWORK-SUBMIT-COMMENT']/values/text"/></xsl:call-template>');
        </script>

        <div id="bwSubmittedEventCommentBlock">
          <div id="bwSubmittedBy">
            <xsl:copy-of select="$bwStr-AEEF-SubmittedBy"/>
            <xsl:variable name="submitterEmail" select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTER-EMAIL']/values/text"/>
            <a href="mailto:{$submitterEmail}?subject=[Event%20Submission] {$eventTitle}" title="Email {$submitterEmail}" class="submitter">
              <xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']/values/text"/>
            </a><xsl:text> </xsl:text>
            (<a href="mailto:{$submitterEmail}?subject=[Event%20Submission] {$eventTitle}" title="Email {$submitterEmail}">
              <img src="{$resourcesRoot}/images/email.gif" border="0" alt="*"/>
              <xsl:copy-of select="$bwStr-AEEF-SendMsg"/>
            </a>)
          </div>
          <h4><xsl:copy-of select="$bwStr-AEEF-CommentsFromSubmitter"/></h4>
          <a href="javascript:toggleVisibility('bwSubmittedEventComment','visible');" class="toggle"><xsl:copy-of select="$bwStr-AEEF-ShowHide"/></a>
          <a href="javascript:bwSubmitComment.launch();" class="toggle"><xsl:copy-of select="$bwStr-AEEF-PopUp"/></a>
          <div id="bwSubmittedEventComment">
            <xsl:if test="/bedework/page = 'modEvent'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
            <xsl:text> </xsl:text>
          </div>
        </div>
        <script type="text/javascript">
          bwSubmitComment.display('bwSubmittedEventComment');
        </script>
      </xsl:if>
    </xsl:if>

    <xsl:variable name="submitter">
      <xsl:choose>
        <xsl:when test="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-SUBMITTEDBY']/values/text"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="/bedework/userInfo/currentUser"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-For"/><xsl:text> </xsl:text><xsl:value-of select="/bedework/userInfo/group"/> (<xsl:value-of select="/bedework/userInfo/user"/>)</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <form name="eventForm" method="post" enctype="multipart/form-data" onsubmit="return setEventFields(this,{$portalFriendly},'{$submitter}')">
      <xsl:choose>
        <xsl:when test="/bedework/page = 'modEventPending'">
          <xsl:attribute name="action"><xsl:value-of select="$event-updatePending"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="action"><xsl:value-of select="$event-update"/></xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <!-- Set the underlying calendar; if there is more than one publishing calendar, the
           form below will test for that and allow this value to be changed.  -->
      <input type="hidden" name="newCalPath" id="newCalPath">
        <xsl:choose>
          <xsl:when test="/bedework/creating='true'">
            <xsl:attribute name="value"><xsl:value-of select="form/calendar/all/select/option/@value"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="value"><xsl:value-of select="form/calendar/all/select/option[@selected]/@value"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </input>

      <!-- Setup email notification fields -->
      <input type="hidden" id="submitNotification" name="submitNotification" value="false"/>
      <!-- "from" should be a preference: hard code it for now -->
      <input type="hidden" id="snfrom" name="snfrom" value="bedework@yoursite.edu"/>
      <input type="hidden" id="snsubject" name="snsubject" value=""/>
      <input type="hidden" id="sntext" name="sntext" value=""/>

      <xsl:call-template name="submitEventButtons">
        <xsl:with-param name="eventTitle" select="$eventTitle"/>
        <xsl:with-param name="eventUrlPrefix" select="$eventUrlPrefix"/>
        <xsl:with-param name="canEdit" select="$canEdit"/>
      </xsl:call-template>

      <table class="eventFormTable">
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Title"/>
          </td>
          <td>
            <input type="text" size="60" name="summary">
              <xsl:attribute name="value"><xsl:value-of select="form/title/input/@value"/></xsl:attribute>
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
            </input>
            <xsl:if test="$canEdit = 'false'">
              <div class="bwHighlightBox">
                <strong><xsl:value-of select="form/title/input/@value"/></strong>
              </div>
            </xsl:if>
          </td>
        </tr>
<!--
        <tr>
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Type"/>
          </td>
          <td>
            <input type="radio" name="entityType" value="1"/><xsl:copy-of select="$bwStr-AEEF-Event"/>
            <input type="radio" name="entityType" value="2"/><xsl:copy-of select="$bwStr-AEEF-Deadline"/>
          </td>
        </tr>
-->
        <xsl:if test="count(form/calendar/all/select/option) &gt; 1 and
                      not(starts-with(form/calendar/path,$submissionsRootUnencoded))">
        <!-- check to see if we have more than one publishing calendar
             but disallow directly setting for pending events -->
          <tr>
            <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-Calendar"/>
            </td>
            <td>
              <xsl:if test="form/calendar/preferred/select/option">
                <!-- Display the preferred calendars by default if they exist -->
                <select name="bwPreferredCalendars" id="bwPreferredCalendars" onchange="this.form.newCalPath.value = this.value">

                  <option value="">
                    <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                  </option>
                  <xsl:for-each select="form/calendar/preferred/select/option">
                    <xsl:sort select="." order="ascending"/>
                    <option>
                      <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
                      <xsl:if test="@selected"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                      <xsl:choose>
                        <xsl:when test="starts-with(node(),/bedework/submissionsRoot/unencoded)">
                          <xsl:copy-of select="$bwStr-AEEF-SubmittedEvents"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="substring-after(node(),'/public/')"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </option>
                  </xsl:for-each>
                </select>
              </xsl:if>
              <!-- hide the listing of all calendars if preferred calendars exist, otherwise show them -->
              <select name="bwAllCalendars" id="bwAllCalendars" onchange="this.form.newCalPath.value = this.value;">

                <xsl:if test="form/calendar/preferred/select/option">
                  <xsl:attribute name="class">invisible</xsl:attribute>
                </xsl:if>
                <option value="">
                  <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                </option>
                <xsl:for-each select="form/calendar/all/select/option">
                  <xsl:sort select="." order="ascending"/>
                  <option>
                    <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
                    <xsl:if test="@selected"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                    <xsl:choose>
                      <xsl:when test="starts-with(node(),/bedework/submissionsRoot/unencoded)">
                        <xsl:copy-of select="$bwStr-AEEF-SubmittedEvents"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="substring-after(node(),'/public/')"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </option>
                </xsl:for-each>
              </select>
              <xsl:text> </xsl:text>
              <!-- allow for toggling between the preferred and all calendars listings if preferred
                   calendars exist -->
              <xsl:if test="form/calendar/preferred/select/option">
                <input type="radio" name="toggleCalendarLists" id="toggleCalendarListsPreferred" value="preferred" checked="checked" onclick="changeClass('bwPreferredCalendars','shown');changeClass('bwAllCalendars','invisible');this.form.newCalPath.value = this.form.bwPreferredCalendars.value;"/>
                <label for="toggleCalendarListsPreferred"><xsl:copy-of select="$bwStr-AEEF-Preferred"/></label>
                <input type="radio" name="toggleCalendarLists" id="toggleCalendarListsAll" value="all" onclick="changeClass('bwPreferredCalendars','invisible');changeClass('bwAllCalendars','shown');this.form.newCalPath.value = this.form.bwAllCalendars.value;"/>
                <label for="toggleCalendarListsAll"><xsl:copy-of select="$bwStr-AEEF-All"/></label>
              </xsl:if>
            </td>
          </tr>
        </xsl:if>

        <tr>
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-DateAndTime"/>
          </td>
          <td>
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
              <xsl:copy-of select="$bwStr-AEEF-AllDay"/>
            </label>

            <!-- floating event: no timezone (and not UTC) -->
            <!-- let's hide it completely unless it comes in checked
                 (e.g. from import); to restore this field, remove the if  -->
            <xsl:if test="form/floating/input/@checked='checked'">
              <input type="checkbox" name="floatingFlag" id="floatingFlag" onclick="swapFloatingTime(this)" value="off">
                <xsl:if test="form/floating/input/@checked='checked'">
                  <xsl:attribute name="checked">checked</xsl:attribute>
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <input type="hidden" name="eventStartDate.floating" value="off" id="startFloating">
                <xsl:if test="form/floating/input/@checked='checked'">
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <input type="hidden" name="eventEndDate.floating" value="off" id="endFloating">
                <xsl:if test="form/floating/input/@checked='checked'">
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <label for="floatingFlag">
                <xsl:copy-of select="$bwStr-AEEF-Floating"/>
              </label>
            </xsl:if>

            <!-- store time as coordinated universal time (UTC) -->
            <!-- like floating time, let's hide UTC completely unless an
                 event comes in checked; (e.g. from import);
                 to restore this field, remove the if -->
            <xsl:if test="form/storeUTC/input/@checked='checked'">
              <input type="checkbox" name="storeUTCFlag" id="storeUTCFlag" onclick="swapStoreUTC(this)" value="off">
                <xsl:if test="form/storeUTC/input/@checked='checked'">
                  <xsl:attribute name="checked">checked</xsl:attribute>
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <input type="hidden" name="eventStartDate.storeUTC" value="off" id="startStoreUTC">
                <xsl:if test="form/storeUTC/input/@checked='checked'">
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <input type="hidden" name="eventEndDate.storeUTC" value="off" id="endStoreUTC">
                <xsl:if test="form/storeUTC/input/@checked='checked'">
                  <xsl:attribute name="value">on</xsl:attribute>
                </xsl:if>
              </input>
              <xsl:copy-of select="$bwStr-AEEF-StoreAsUTC"/>
            </xsl:if>

            <br/>
            <div class="dateStartEndBox">
              <strong><xsl:copy-of select="$bwStr-AEEF-Start"/></strong>
              <div class="dateFields">
                <span class="startDateLabel"><xsl:copy-of select="$bwStr-AEEF-Date"/><xsl:text> </xsl:text></span>
                <xsl:choose>
                  <xsl:when test="$portalFriendly = 'true'">
                    <xsl:copy-of select="form/start/month/*"/>
                    <xsl:copy-of select="form/start/day/*"/>
                    <xsl:choose>
                      <xsl:when test="/bedework/creating = 'true'">
                        <xsl:copy-of select="form/start/year/*"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:copy-of select="form/start/yearText/*"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <script language="JavaScript" type="text/javascript">
                      <xsl:comment>
                      startDateDynCalWidget = new dynCalendar('startDateDynCalWidget', <xsl:value-of select="number(form/start/yearText/input/@value)"/>, <xsl:value-of select="number(form/start/month/select/option[@selected='selected']/@value)-1"/>, <xsl:value-of select="number(form/start/day/select/option[@selected='selected']/@value)"/>, 'startDateCalWidgetCallback',true,'<xsl:value-of select="$resourcesRoot"/>/resources/');
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
                      $("#bwEventWidgetStartDate").val('<xsl:value-of select="substring-before(form/start/rfc3339DateTime,'T')"/>');*/
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
                  <img src="{$resourcesRoot}/images/clockIcon.gif" width="16" height="15" border="0" id="bwStartClock" alt="*"/>

                  <select name="eventStartDate.tzid" id="startTzid" class="timezones">
                    <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                    <option value="-1"><xsl:copy-of select="$bwStr-AEEF-SelectTimezone"/></option>
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
              <strong><xsl:copy-of select="$bwStr-AEEF-End"/></strong>
              <xsl:choose>
                <xsl:when test="form/end/type='E'">
                  <input type="radio" name="eventEndType" id="bwEndDateTimeButton" value="E" checked="checked" onclick="changeClass('endDateTime','shown');changeClass('endDuration','invisible');"/>
                </xsl:when>
                <xsl:otherwise>
                  <input type="radio" name="eventEndType" id="bwEndDateTimeButton" value="E" onclick="changeClass('endDateTime','shown');changeClass('endDuration','invisible');"/>
                </xsl:otherwise>
              </xsl:choose>
              <label for="bwEndDateTimeButton">
                <xsl:copy-of select="$bwStr-AEEF-Date"/>
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
                      <xsl:copy-of select="form/end/dateTime/month/*"/>
                      <xsl:copy-of select="form/end/dateTime/day/*"/>
                      <xsl:choose>
                        <xsl:when test="/bedework/creating = 'true'">
                          <xsl:copy-of select="form/end/dateTime/year/*"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:copy-of select="form/end/dateTime/yearText/*"/>
                        </xsl:otherwise>
                      </xsl:choose>
                      <script language="JavaScript" type="text/javascript">
                        <xsl:comment>
                        endDateDynCalWidget = new dynCalendar('endDateDynCalWidget', <xsl:value-of select="number(form/start/yearText/input/@value)"/>, <xsl:value-of select="number(form/start/month/select/option[@selected='selected']/@value)-1"/>, <xsl:value-of select="number(form/start/day/select/option[@selected='selected']/@value)"/>, 'endDateCalWidgetCallback',true,'<xsl:value-of select="$resourcesRoot"/>/resources/');
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
                        $("#bwEventWidgetEndDate").val('<xsl:value-of select="substring-before(form/end/rfc3339DateTime,'T')"/>');*/
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
                    <img src="{$resourcesRoot}/images/clockIcon.gif" width="16" height="15" border="0" id="bwEndClock" alt="*"/>

                    <select name="eventEndDate.tzid" id="endTzid" class="timezones">
                      <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                      <option value="-1"><xsl:copy-of select="$bwStr-AEEF-SelectTimezone"/></option>
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
              </div>
              <br/>
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
                  <xsl:copy-of select="$bwStr-AEEF-Duration"/>
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
                        <input type="text" name="eventDuration.daysStr" size="2" id="durationDays">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/duration/days/input/@value"/></xsl:attribute>
                        </input><xsl:copy-of select="$bwStr-AEEF-Days"/>
                        <span id="durationHrMin" class="{$durationHrMinClass}">
                          <input type="text" name="eventDuration.hoursStr" size="2" id="durationHours">
                            <xsl:attribute name="value"><xsl:value-of select="form/end/duration/hours/input/@value"/></xsl:attribute>
                          </input><xsl:copy-of select="$bwStr-AEEF-Hours"/>
                          <input type="text" name="eventDuration.minutesStr" size="2" id="durationMinutes">
                            <xsl:attribute name="value"><xsl:value-of select="form/end/duration/minutes/input/@value"/></xsl:attribute>
                          </input><xsl:copy-of select="$bwStr-AEEF-Minutes"/>
                        </span>
                      </div>
                      <span class="durationSpacerText"><xsl:copy-of select="$bwStr-AEEF-Or"/></span>
                      <div class="durationBox">
                        <input type="radio" name="eventDuration.type" value="weeks" onclick="swapDurationType('week')"/>
                        <input type="text" name="eventDuration.weeksStr" size="2" id="durationWeeks" disabled="disabled">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/duration/weeks/input/@value"/></xsl:attribute>
                        </input><xsl:copy-of select="$bwStr-AEEF-Weeks"/>
                      </div>
                    </xsl:when>
                    <xsl:otherwise>
                      <!-- we are using week format -->
                      <div class="durationBox">
                        <input type="radio" name="eventDuration.type" value="daytime" onclick="swapDurationType('daytime')"/>
                        <xsl:variable name="daysStr" select="form/end/duration/days/input/@value"/>
                        <input type="text" name="eventDuration.daysStr" size="2" value="{$daysStr}" id="durationDays" disabled="disabled"/><xsl:copy-of select="$bwStr-AEEF-Days"/>
                        <span id="durationHrMin" class="{$durationHrMinClass}">
                          <xsl:variable name="hoursStr" select="form/end/duration/hours/input/@value"/>
                          <input type="text" name="eventDuration.hoursStr" size="2" value="{$hoursStr}" id="durationHours" disabled="disabled"/><xsl:copy-of select="$bwStr-AEEF-Hours"/>
                          <xsl:variable name="minutesStr" select="form/end/duration/minutes/input/@value"/>
                          <input type="text" name="eventDuration.minutesStr" size="2" value="{$minutesStr}" id="durationMinutes" disabled="disabled"/><xsl:copy-of select="$bwStr-AEEF-Minutes"/>
                        </span>
                      </div>
                      <span class="durationSpacerText">or</span>
                      <div class="durationBox">
                        <input type="radio" name="eventDuration.type" value="weeks" onclick="swapDurationType('week')" checked="checked"/>
                        <input type="text" name="eventDuration.weeksStr" size="2" id="durationWeeks">
                          <xsl:attribute name="value"><xsl:value-of select="form/end/duration/weeks/input/@value"/></xsl:attribute>
                        </input><xsl:copy-of select="$bwStr-AEEF-Weeks"/>
                      </div>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
              </div>
              <br/>
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
                  <xsl:copy-of select="$bwStr-AEEF-ThisEventHasNoDurationEndDate"/>
                </label>
              </div>
            </div>
          </td>
        </tr>
        <xsl:if test="$canEdit = 'false'">
          <!-- admin user can't edit this, so just dispaly some useful information -->
          <tr>
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-DateAndTime"/>
            </td>
            <td>
              <xsl:value-of select="form/start/month/select/option[@selected]"/><xsl:text> </xsl:text>
              <xsl:value-of select="form/start/day/select/option[@selected]"/><xsl:text> </xsl:text>
              <xsl:value-of select="form/start/yearText/input/@value"/><xsl:text>, </xsl:text>
              <xsl:value-of select="form/start/hour/select/option[@selected]"/>:<xsl:value-of select="form/start/minute/select/option[@selected]"/><xsl:text> </xsl:text>
              <xsl:value-of select="form/start/ampm/select/option[@selected]"/><xsl:text> </xsl:text>
              <xsl:if test="form/allDay/input/@checked='checked'">(all day)<xsl:text> </xsl:text></xsl:if>
              <xsl:if test="form/start/tzid != form/end/dateTime/tzid"><xsl:value-of select="form/start/tzid"/></xsl:if>
              <xsl:if test="form/start/rfc3339DateTime != form/end/rfc3339DateTime">
                -
                <xsl:if test="substring(form/start/rfc3339DateTime,1,10) != substring(form/end/rfc3339DateTime,1,10)">
                  <xsl:value-of select="form/end/dateTime/month/select/option[@selected]"/><xsl:text> </xsl:text>
                  <xsl:value-of select="form/end/dateTime/day/select/option[@selected]"/><xsl:text> </xsl:text>
                  <xsl:value-of select="form/end/dateTime/yearText/input/@value"/><xsl:text>, </xsl:text>
                </xsl:if>
                <xsl:if test="substring(form/start/rfc3339DateTime,12,16) != substring(form/end/rfc3339DateTime,12,16)">
                  <xsl:value-of select="form/end/dateTime/hour/select/option[@selected]"/>:<xsl:value-of select="form/end/dateTime/minute/select/option[@selected]"/><xsl:text> </xsl:text>
                  <xsl:value-of select="form/end/dateTime/ampm/select/option[@selected]"/><xsl:text> </xsl:text>
                  <xsl:value-of select="form/end/dateTime/tzid"/>
                </xsl:if>
              </xsl:if>
            </td>
          </tr>
        </xsl:if>


        <!-- Recurrence fields -->
        <!-- ================= -->
        <tr>
          <xsl:if test="($canEdit = 'false') and (form/recurringEntity = 'false') and (recurrenceId = '')"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-RECURRANCE"/>
          </td>
          <td>
            <xsl:choose>
              <xsl:when test="recurrenceId != ''">
                <!-- recurrence instances can not themselves recur,
                     so provide access to master event -->
                <em><xsl:copy-of select="$bwStr-AEEF-ThisEventRecurrenceInstance"/></em><br/>
                <xsl:choose>
                  <xsl:when test="starts-with(form/calendar/event/path,$submissionsRootUnencoded)">
                    <a href="{$event-fetchForUpdatePending}&amp;calPath={$calPath}&amp;guid={$guid}" title="{$bwStr-AEEF-EditMaster}"><xsl:copy-of select="$bwStr-AEEF-EditPendingMasterEvent"/></a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{$event-fetchForUpdate}&amp;calPath={$calPath}&amp;guid={$guid}" title="{$bwStr-AEEF-EditMaster}"><xsl:copy-of select="$bwStr-AEEF-EditMasterEvent"/></a>
                  </xsl:otherwise>
                </xsl:choose>
                
              </xsl:when>
              <xsl:otherwise>
                <!-- has recurrenceId, so is master -->

                <xsl:choose>
                  <xsl:when test="form/recurringEntity = 'false'">
                    <!-- the switch is required to turn recurrence on - maybe we can infer this instead? -->
                    <div id="recurringSwitch">
                      <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                      <!-- set or remove "recurring" and show or hide all recurrence fields: -->
                      <input type="radio" name="recurring" id="bwRecurringOnButton" value="true" onclick="swapRecurrence(this)">
                        <xsl:if test="form/recurringEntity = 'true'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                      </input>
                      <label for="bwRecurringOnButton">
                        <xsl:copy-of select="$bwStr-AEEF-EventRecurs"/>
                      </label>
                      <input type="radio" name="recurring" id="bwRecurringOffButton" value="false" onclick="swapRecurrence(this)">
                        <xsl:if test="form/recurringEntity = 'false'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                      </input>
                      <label for="bwRecurringOffButton">
                        <xsl:copy-of select="$bwStr-AEEF-EventDoesNotRecur"/>
                      </label>
                    </div>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- is a recurring event; once created as such, it can no longer be made non-recurring. -->
                    <input type="hidden" name="recurring" value="true"/>
                  </xsl:otherwise>
                </xsl:choose>

                <!-- wrapper for all recurrence fields (rrules and rdates): -->
                <div id="recurrenceFields" class="invisible">
                  <xsl:if test="form/recurringEntity = 'true'"><xsl:attribute name="class">visible</xsl:attribute></xsl:if>

                  <h4>
                    <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                    <xsl:copy-of select="$bwStr-AEEF-RecurrenceRules"/>
                  </h4>
                  <!-- show or hide rrules fields when editing: -->
                  <xsl:if test="form/recurrence">
                    <span id="rrulesSwitch">
                      <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                      <input type="checkbox" name="rrulesFlag" onclick="swapRrules(this)" value="on">
                        <xsl:if test="$canEdit = 'false'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                      </input>
                      <xsl:copy-of select="$bwStr-AEEF-ChangeRecurrenceRules"/>
                    </span>
                  </xsl:if>
                  <span id="rrulesUiSwitch">
                    <xsl:if test="form/recurrence">
                      <xsl:attribute name="class">invisible</xsl:attribute>
                    </xsl:if>
                    <input type="checkbox" name="rrulesUiSwitch" id="bwRrulesAdvancedButton" value="advanced" onchange="swapVisible(this,'advancedRrules')"/>
                    <label for="bwRrulesAdvancedButton">
                      <xsl:copy-of select="$bwStr-AEEF-ShowAdvancedRecurrenceRules"/>
                    </label>
                  </span>

                  <xsl:if test="form/recurrence">
                    <!-- Output descriptive recurrence rules information.  Probably not
                         complete yet. Replace all strings so can be
                         more easily internationalized. -->
                    <div id="recurrenceInfo">
                      <xsl:copy-of select="$bwStr-AEEF-Every"/><xsl:text> </xsl:text>
                      <xsl:choose>
                        <xsl:when test="form/recurrence/interval &gt; 1">
                          <xsl:value-of select="form/recurrence/interval"/>
                        </xsl:when>
                      </xsl:choose>
                      <xsl:text> </xsl:text>
                      <xsl:choose>
                        <xsl:when test="form/recurrence/freq = 'HOURLY'"><xsl:copy-of select="$bwStr-AEEF-Hour"/><xsl:text> </xsl:text></xsl:when>
                        <xsl:when test="form/recurrence/freq = 'DAILY'"><xsl:copy-of select="$bwStr-AEEF-Day"/><xsl:text> </xsl:text></xsl:when>
                        <xsl:when test="form/recurrence/freq = 'WEEKLY'"><xsl:copy-of select="$bwStr-AEEF-Week"/><xsl:text> </xsl:text></xsl:when>
                        <xsl:when test="form/recurrence/freq = 'MONTHLY'"><xsl:copy-of select="$bwStr-AEEF-Month"/><xsl:text> </xsl:text></xsl:when>
                        <xsl:when test="form/recurrence/freq = 'YEARLY'"><xsl:copy-of select="$bwStr-AEEF-Year"/><xsl:text> </xsl:text></xsl:when>
                      </xsl:choose>
                      <xsl:text> </xsl:text>

                      <xsl:if test="form/recurrence/byday">
                        <xsl:for-each select="form/recurrence/byday/pos">
                          <xsl:if test="position() != 1"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-And"/><xsl:text> </xsl:text></xsl:if>
                          <xsl:copy-of select="$bwStr-AEEF-On"/>
                          <xsl:text> </xsl:text>
                          <xsl:choose>
                            <xsl:when test="@val='1'">
                              <xsl:copy-of select="$bwStr-AEEF-TheFirst"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:when test="@val='2'">
                              <xsl:copy-of select="$bwStr-AEEF-TheSecond"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:when test="@val='3'">
                              <xsl:copy-of select="$bwStr-AEEF-TheThird"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:when test="@val='4'">
                              <xsl:copy-of select="$bwStr-AEEF-TheFourth"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:when test="@val='5'">
                              <xsl:copy-of select="$bwStr-AEEF-TheFifth"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:when test="@val='-1'">
                              <xsl:copy-of select="$bwStr-AEEF-TheLast"/><xsl:text> </xsl:text>
                            </xsl:when>
                            <!-- don't output "every" -->
                            <!--<xsl:otherwise>
                              every
                            </xsl:otherwise>-->
                          </xsl:choose>
                          <xsl:for-each select="day">
                            <xsl:if test="position() != 1 and position() = last()"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-And"/><xsl:text> </xsl:text></xsl:if>
                            <xsl:variable name="dayVal" select="."/>
                            <xsl:variable name="dayPos">
                              <xsl:for-each select="/bedework/recurdayvals/val">
                                <xsl:if test="node() = $dayVal"><xsl:value-of select="position()"/></xsl:if>
                              </xsl:for-each>
                            </xsl:variable>
                            <xsl:value-of select="/bedework/shortdaynames/val[position() = $dayPos]"/>
                            <xsl:if test="position() != last()">, </xsl:if>
                          </xsl:for-each>
                        </xsl:for-each>
                      </xsl:if>

                      <xsl:if test="form/recurrence/bymonth">
                        <xsl:copy-of select="$bwStr-AEEF-In"/>
                        <xsl:for-each select="form/recurrence/bymonth/val">
                          <xsl:if test="position() != 1 and position() = last()"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-And"/><xsl:text> </xsl:text></xsl:if>
                          <xsl:variable name="monthNum" select="number(.)"/>
                          <xsl:value-of select="/bedework/monthlabels/val[position() = $monthNum]"/>
                          <xsl:if test="position() != last()">, </xsl:if>
                        </xsl:for-each>
                      </xsl:if>

                      <xsl:if test="form/recurrence/bymonthday">
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-OnThe"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="form/recurrence/bymonthday/val" mode="weekMonthYearNumbers"/>
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-DayOfTheMonth"/>
                        <xsl:text> </xsl:text>
                      </xsl:if>

                      <xsl:if test="form/recurrence/byyearday">
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-OnThe"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="form/recurrence/byyearday/val" mode="weekMonthYearNumbers"/>
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-DayOfTheYear"/>
                        <xsl:text> </xsl:text>
                      </xsl:if>

                      <xsl:if test="form/recurrence/byweekno">
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-InThe"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="form/recurrence/byweekno/val" mode="weekMonthYearNumbers"/>
                        <xsl:text> </xsl:text>
                        <xsl:copy-of select="$bwStr-AEEF-WeekOfTheYear"/>
                        <xsl:text> </xsl:text>
                      </xsl:if>

                      <xsl:copy-of select="$bwStr-AEEF-Repeating"/>
                      <xsl:choose>
                        <xsl:when test="form/recurrence/count = '-1'"><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-Forever"/></xsl:when>
                        <xsl:when test="form/recurrence/until">
                          <xsl:text> </xsl:text>
                          <xsl:copy-of select="$bwStr-AEEF-Until"/>
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="substring(form/recurrence/until,1,4)"/>-<xsl:value-of select="substring(form/recurrence/until,5,2)"/>-<xsl:value-of select="substring(form/recurrence/until,7,2)"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="form/recurrence/count"/>
                          <xsl:text> </xsl:text>
                          <xsl:copy-of select="$bwStr-AEEF-Times"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </div>
                  </xsl:if>

                  <!-- set these dynamically when form is submitted -->
                  <input type="hidden" name="interval" value=""/>
                  <input type="hidden" name="count" value=""/>
                  <input type="hidden" name="until" value=""/>
                  <input type="hidden" name="byday" value=""/>
                  <input type="hidden" name="bymonthday" value=""/>
                  <input type="hidden" name="bymonth" value=""/>
                  <input type="hidden" name="byweekno" value=""/>
                  <input type="hidden" name="byyearday" value=""/>
                  <input type="hidden" name="wkst" value=""/>
                  <input type="hidden" name="setpos" value=""/>

                  <!-- wrapper for rrules: -->
                  <table id="rrulesTable" cellspacing="0">
                  <xsl:if test="form/recurrence">
                    <xsl:attribute name="class">invisible</xsl:attribute>
                  </xsl:if>
                    <tr>
                      <td id="recurrenceFrequency" rowspan="2">
                        <em><xsl:copy-of select="$bwStr-AEEF-Frequency"/></em><br/>
                        <input type="radio" name="freq" id="bwFreqNone" value="NONE" onclick="showRrules(this.value)" checked="checked"/>
                        <label for="bwFreqNone">
                          <xsl:copy-of select="$bwStr-AEEF-None"/>
                        </label><br/>
                        <!--<input type="radio" name="freq" value="HOURLY" onclick="showRrules(this.value)"/>hourly<br/>-->
                        <input type="radio" name="freq" id="bwFreqDaily" value="DAILY" onclick="showRrules(this.value)"/>
                        <label for="bwFreqDaily">
                          <xsl:copy-of select="$bwStr-AEEF-Daily"/>
                        </label><br/>
                        <input type="radio" name="freq" id="bwFreqWeekly" value="WEEKLY" onclick="showRrules(this.value)"/>
                        <label for="bwFreqWeekly">
                          <xsl:copy-of select="$bwStr-AEEF-Weekly"/>
                        </label><br/>
                        <input type="radio" name="freq" id="bwFreqMonthly" value="MONTHLY" onclick="showRrules(this.value)"/>
                        <label for="bwFreqMonthly">
                          <xsl:copy-of select="$bwStr-AEEF-Monthly"/>
                        </label><br/>
                        <input type="radio" name="freq" id="bwFreqYearly" value="YEARLY" onclick="showRrules(this.value)"/>
                        <label for="bwFreqYearly">
                          <xsl:copy-of select="$bwStr-AEEF-Yearly"/>
                        </label>
                      </td>
                      <!-- recurrence count, until, forever -->
                      <td id="recurrenceUntil">
                        <div id="noneRecurrenceRules">
                          <xsl:copy-of select="$bwStr-AEEF-NoRecurrenceRules"/>
                        </div>
                        <div id="recurrenceUntilRules" class="invisible">
                          <em><xsl:copy-of select="$bwStr-AEEF-Repeat"/></em>
                          <p>
                            <input type="radio" name="recurCountUntil" value="forever">
                              <xsl:if test="not(form/recurring) or form/recurring/count = '-1'">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Forever"/>
                            <input type="radio" name="recurCountUntil" value="count" id="recurCount">
                              <xsl:if test="form/recurring/count != '-1'">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                              </xsl:if>
                            </input>
                            <input type="text" value="1" size="2" name="countHolder"  onfocus="selectRecurCountUntil('recurCount')">
                              <xsl:if test="form/recurring/count and form/recurring/count != '-1'">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurring/count"/></xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Time"/>
                            <input type="radio" name="recurCountUntil" value="until" id="recurUntil">
                              <xsl:if test="form/recurring/until">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Until"/>
                            <span id="untilHolder">
                              <input type="hidden" name="bwEventUntilDate" id="bwEventUntilDate" size="10"/>
                              <input type="text" name="bwEventWidgetUntilDate" id="bwEventWidgetUntilDate" size="10" onfocus="selectRecurCountUntil('recurUntil')"/>
                              <script language="JavaScript" type="text/javascript">
                                <xsl:comment>
                                /*$("#bwEventWidgetUntilDate").datepicker({
                                  <xsl:choose>
                                    <xsl:when test="form/recurrence/until">
                                      defaultDate: new Date(<xsl:value-of select="substring(form/recurrence/until,1,4)"/>, <xsl:value-of select="number(substring(form/recurrence/until,5,2)) - 1"/>, <xsl:value-of select="substring(form/recurrence/until,7,2)"/>),
                                    </xsl:when>
                                    <xsl:otherwise>
                                      defaultDate: new Date(<xsl:value-of select="form/start/yearText/input/@value"/>, <xsl:value-of select="number(form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="form/start/day/select/option[@selected = 'selected']/@value"/>),
                                    </xsl:otherwise>
                                  </xsl:choose>
                                  altField: "#bwEventUntilDate",
                                  altFormat: "yymmdd"
                                }).attr("readonly", "readonly");
                                $("#bwEventWidgetUntilDate").val('<xsl:value-of select="substring-before(form/start/rfc3339DateTime,'T')"/>');*/
                                </xsl:comment>
                              </script>
                            </span>
                          </p>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td id="advancedRrules" class="invisible">
                        <!-- hourly -->
                        <div id="hourlyRecurrenceRules" class="invisible">
                          <p>
                            <em><xsl:copy-of select="$bwStr-AEEF-Interval"/><xsl:text> </xsl:text></em>
                            <xsl:copy-of select="$bwStr-AEEF-Every"/>
                            <input type="text" name="hourlyInterval" size="2" value="1">
                              <xsl:if test="form/recurrence/interval">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurrence/interval"/></xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Hour"/>
                          </p>
                        </div>
                        <!-- daily -->
                        <div id="dailyRecurrenceRules" class="invisible">
                          <p>
                            <em><xsl:copy-of select="$bwStr-AEEF-Interval"/><xsl:text> </xsl:text></em>
                            <xsl:copy-of select="$bwStr-AEEF-Every"/>
                            <input type="text" name="dailyInterval" size="2" value="1">
                              <xsl:if test="form/recurrence/interval">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurrence/interval"/></xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Day"/>
                          </p>
                          <p>
                            <input type="checkbox" name="swapDayMonthCheckBoxList" value="" onclick="swapVisible(this,'dayMonthCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-InTheseMonths"/>
                            <div id="dayMonthCheckBoxList" class="invisible">
                              <xsl:for-each select="/bedework/monthlabels/val">
                                <xsl:variable name="pos"><xsl:value-of select="position()"/></xsl:variable>
                                <span class="chkBoxListItem">
                                  <input type="checkbox" name="dayMonths">
                                    <xsl:attribute name="value"><xsl:value-of select="/bedework/monthvalues/val[position() = $pos]"/></xsl:attribute>
                                  </input>
                                  <xsl:value-of select="."/>
                                </span>
                                <xsl:if test="$pos mod 6 = 0"><br/></xsl:if>
                              </xsl:for-each>
                            </div>
                          </p>
                          <!--<p>
                            <input type="checkbox" name="swapDaySetPos" value="" onclick="swapVisible(this,'daySetPos')"/>
                            limit to:
                            <div id="daySetPos" class="invisible">
                            </div>
                          </p>-->
                        </div>
                        <!-- weekly -->
                        <div id="weeklyRecurrenceRules" class="invisible">
                          <p>
                            <em><xsl:copy-of select="$bwStr-AEEF-Interval"/></em>
                            <xsl:copy-of select="$bwStr-AEEF-Every"/>
                            <input type="text" name="weeklyInterval" size="2" value="1">
                              <xsl:if test="form/recurrence/interval">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurrence/interval"/></xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-WeekOn"/>
                          </p>
                          <div id="weekRecurFields">
                            <xsl:call-template name="byDayChkBoxList">
                              <xsl:with-param name="name">byDayWeek</xsl:with-param>
                            </xsl:call-template>
                          </div>
                          <p class="weekRecurLinks">
                            <a href="javascript:recurSelectWeekdays('weekRecurFields')"><xsl:copy-of select="$bwStr-AEEF-SelectWeekdays"/></a> |
                            <a href="javascript:recurSelectWeekends('weekRecurFields')"><xsl:copy-of select="$bwStr-AEEF-SelectWeekends"/></a>
                          </p>
                          <p>
                            <xsl:copy-of select="$bwStr-AEEF-WeekStart"/>
                            <select name="weekWkst">
                              <xsl:for-each select="/bedework/shortdaynames/val">
                                <xsl:variable name="pos" select="position()"/>
                                <option>
                                  <xsl:attribute name="value"><xsl:value-of select="/bedework/recurdayvals/val[position() = $pos]"/></xsl:attribute>
                                  <xsl:value-of select="."/>
                                </option>
                              </xsl:for-each>
                            </select>
                          </p>
                        </div>
                        <!-- monthly -->
                        <div id="monthlyRecurrenceRules" class="invisible">
                          <p>
                            <em><xsl:copy-of select="$bwStr-AEEF-Interval"/></em>
                            <xsl:copy-of select="$bwStr-AEEF-Every"/>
                            <input type="text" name="monthlyInterval" size="2" value="1">
                              <xsl:if test="form/recurrence/interval">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurrence/interval"/></xsl:attribute>
                              </xsl:if>
                            </input>
                            <xsl:copy-of select="$bwStr-AEEF-Month"/>
                          </p>
                          <div id="monthRecurFields">
                            <div id="monthRecurFields1">
                              <xsl:copy-of select="$bwStr-AEEF-On"/>
                              <select name="bymonthposPos1" onchange="changeClass('monthRecurFields2','shown')">
                                <xsl:call-template name="recurrenceDayPosOptions"/>
                              </select>
                              <xsl:call-template name="byDayChkBoxList"/>
                            </div>
                            <xsl:call-template name="buildRecurFields">
                              <xsl:with-param name="current">2</xsl:with-param>
                              <xsl:with-param name="total">10</xsl:with-param>
                              <xsl:with-param name="name">month</xsl:with-param>
                            </xsl:call-template>
                          </div>
                          <p>
                            <input type="checkbox" name="swapMonthDaysCheckBoxList" value="" onclick="swapVisible(this,'monthDaysCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-OnTheseDays"/><br/>
                            <div id="monthDaysCheckBoxList" class="invisible">
                              <xsl:call-template name="buildCheckboxList">
                                <xsl:with-param name="current">1</xsl:with-param>
                                <xsl:with-param name="end">31</xsl:with-param>
                                <xsl:with-param name="name">monthDayBoxes</xsl:with-param>
                              </xsl:call-template>
                            </div>
                          </p>
                        </div>
                        <!-- yearly -->
                        <div id="yearlyRecurrenceRules" class="invisible">
                          <p>
                            <em><xsl:copy-of select="$bwStr-AEEF-Interval"/></em>
                            <xsl:copy-of select="$bwStr-AEEF-Every"/>
                            <input type="text" name="yearlyInterval" size="2" value="1">
                              <xsl:if test="form/recurrence/interval">
                                <xsl:attribute name="value"><xsl:value-of select="form/recurrence/interval"/></xsl:attribute>
                              </xsl:if>
                            </input>
                           <xsl:copy-of select="$bwStr-AEEF-Year"/>
                          </p>
                          <div id="yearRecurFields">
                            <div id="yearRecurFields1">
                              <xsl:copy-of select="$bwStr-AEEF-On"/>
                              <select name="byyearposPos1" onchange="changeClass('yearRecurFields2','shown')">
                                <xsl:call-template name="recurrenceDayPosOptions"/>
                              </select>
                              <xsl:call-template name="byDayChkBoxList"/>
                            </div>
                            <xsl:call-template name="buildRecurFields">
                              <xsl:with-param name="current">2</xsl:with-param>
                              <xsl:with-param name="total">10</xsl:with-param>
                              <xsl:with-param name="name">year</xsl:with-param>
                            </xsl:call-template>
                          </div>
                          <p>
                            <input type="checkbox" name="swapYearMonthCheckBoxList" value="" onclick="swapVisible(this,'yearMonthCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-InTheseMonths"/>
                            <div id="yearMonthCheckBoxList" class="invisible">
                              <xsl:for-each select="/bedework/monthlabels/val">
                                <xsl:variable name="pos"><xsl:value-of select="position()"/></xsl:variable>
                                <span class="chkBoxListItem">
                                  <input type="checkbox" name="yearMonths">
                                    <xsl:attribute name="value"><xsl:value-of select="/bedework/monthvalues/val[position() = $pos]"/></xsl:attribute>
                                  </input>
                                  <xsl:value-of select="."/>
                                </span>
                                <xsl:if test="$pos mod 6 = 0"><br/></xsl:if>
                              </xsl:for-each>
                            </div>
                          </p>
                          <p>
                            <input type="checkbox" name="swapYearMonthDaysCheckBoxList" value="" onclick="swapVisible(this,'yearMonthDaysCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-OnTheseDaysOfTheMonth"/><br/>
                            <div id="yearMonthDaysCheckBoxList" class="invisible">
                              <xsl:call-template name="buildCheckboxList">
                                <xsl:with-param name="current">1</xsl:with-param>
                                <xsl:with-param name="end">31</xsl:with-param>
                                <xsl:with-param name="name">yearMonthDayBoxes</xsl:with-param>
                              </xsl:call-template>
                            </div>
                          </p>
                          <p>
                            <input type="checkbox" name="swapYearWeeksCheckBoxList" value="" onclick="swapVisible(this,'yearWeeksCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-InTheseWeeksOfTheYear"/><br/>
                            <div id="yearWeeksCheckBoxList" class="invisible">
                              <xsl:call-template name="buildCheckboxList">
                                <xsl:with-param name="current">1</xsl:with-param>
                                <xsl:with-param name="end">53</xsl:with-param>
                                <xsl:with-param name="name">yearWeekBoxes</xsl:with-param>
                              </xsl:call-template>
                            </div>
                          </p>
                          <p>
                            <input type="checkbox" name="swapYearDaysCheckBoxList" value="" onclick="swapVisible(this,'yearDaysCheckBoxList')"/>
                            <xsl:copy-of select="$bwStr-AEEF-OnTheseDaysOfTheYear"/><br/>
                            <div id="yearDaysCheckBoxList" class="invisible">
                              <xsl:call-template name="buildCheckboxList">
                                <xsl:with-param name="current">1</xsl:with-param>
                                <xsl:with-param name="end">366</xsl:with-param>
                                <xsl:with-param name="name">yearDayBoxes</xsl:with-param>
                              </xsl:call-template>
                            </div>
                          </p>
                          <p>
                            <xsl:copy-of select="$bwStr-AEEF-WeekStart"/>
                            <select name="yearWkst">
                              <xsl:for-each select="/bedework/shortdaynames/val">
                                <xsl:variable name="pos" select="position()"/>
                                <option>
                                  <xsl:attribute name="value"><xsl:value-of select="/bedework/recurdayvals/val[position() = $pos]"/></xsl:attribute>
                                  <xsl:value-of select="."/>
                                </option>
                              </xsl:for-each>
                            </select>
                          </p>
                        </div>
                      </td>
                    </tr>
                  </table>
                  <h4>
                    <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                    <xsl:copy-of select="$bwStr-AEEF-RecurrenceAndExceptionDates"/>
                  </h4>
                  <div id="raContent">
                      <div class="dateStartEndBox" id="rdatesFormFields">
                        <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                        <!--
                        <input type="checkbox" name="dateOnly" id="rdateDateOnly" onclick="swapRdateAllDay(this)" value="true"/>
                        all day
                        <input type="checkbox" name="floating" id="rdateFloating" onclick="swapRdateFloatingTime(this)" value="true"/>
                        floating
                        store time as coordinated universal time (UTC)
                        <input type="checkbox" name="storeUTC" id="rdateStoreUTC" onclick="swapRdateStoreUTC(this)" value="true"/>
                        store as UTC<br/>-->
                        <div class="dateFields">
                          <!-- input name="eventRdate.date"
                                 dojoType="dropdowndatepicker"
                                 formatLength="medium"
                                 value="today"
                                 saveFormat="yyyyMMdd"
                                 id="bwEventWidgeRdate"
                                 iconURL="{$resourcesRoot}/images/calIcon.gif"/-->
                          <input type="text" name="eventRdate.date" id="bwEventWidgetRdate" size="10"/>
                          <script language="JavaScript" type="text/javascript">
                            <xsl:comment>
                            /*$("#bwEventWidgetRdate").datepicker({
                              defaultDate: new Date(<xsl:value-of select="form/start/yearText/input/@value"/>, <xsl:value-of select="number(form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="form/start/day/select/option[@selected = 'selected']/@value"/>),
                              dateFormat: "yymmdd"
                            }).attr("readonly", "readonly");
                            $("#bwEventWidgetRdate").val('<xsl:value-of select="substring-before(form/start/rfc3339DateTime,'T')"/>');*/
                            </xsl:comment>
                          </script>
                        </div>
                        <div id="rdateTimeFields" class="timeFields">
                         <select name="eventRdate.hour" id="eventRdateHour">
                            <option value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <option value="10">10</option>
                            <option value="11">11</option>
                            <option value="12" selected="selected">12</option>
                            <option value="13">13</option>
                            <option value="14">14</option>
                            <option value="15">15</option>
                            <option value="16">16</option>
                            <option value="17">17</option>
                            <option value="18">18</option>
                            <option value="19">19</option>
                            <option value="20">20</option>
                            <option value="21">21</option>
                            <option value="22">22</option>
                            <option value="23">23</option>
                          </select>
                          <select name="eventRdate.minute" id="eventRdateMinute">
                            <option value="00" selected="selected">00</option>
                            <option value="05">05</option>
                            <option value="10">10</option>
                            <option value="15">15</option>
                            <option value="20">20</option>
                            <option value="25">25</option>
                            <option value="30">30</option>
                            <option value="35">35</option>
                            <option value="40">40</option>
                            <option value="45">45</option>
                            <option value="50">50</option>
                            <option value="55">55</option>
                          </select>
                         <xsl:text> </xsl:text>
                         <img src="{$resourcesRoot}/images/clockIcon.gif" width="16" height="15" border="0" id="bwRecExcClock" alt="*"/>

                        <select name="tzid" id="rdateTzid" class="timezones">
                          <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                          <option value=""><xsl:copy-of select="$bwStr-AEEF-SelectTimezone"/></option>
                          <xsl:variable name="rdateTzId" select="/bedework/now/defaultTzid"/>
                          <xsl:for-each select="/bedework/timezones/timezone">
                            <option>
                              <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
                              <xsl:if test="$rdateTzId = id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                              <xsl:value-of select="name"/>
                            </option>
                          </xsl:for-each>
                        </select>
                      </div>
                      <xsl:text> </xsl:text>
                      <!--bwRdates.update() accepts: date, time, allDay, floating, utc, tzid-->
                      <span>
                        <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
                        <input type="button" name="rdate" value="{$bwStr-AEEF-AddRecurance}" onclick="bwRdates.update(this.form['eventRdate.date'].value,this.form['eventRdate.hour'].value + this.form['eventRdate.minute'].value,false,false,false,this.form.tzid.value)"/>
                        <!-- input type="button" name="exdate" value="add exception" onclick="bwExdates.update(this.form['eventRdate.date'].value,this.form['eventRdate.hour'].value + this.form['eventRdate.minute'].value,false,false,false,this.form.tzid.value)"/-->
                      </span>
                      <br class="clear"/>

                      <input type="hidden" name="rdates" value="" id="bwRdatesField" />
                      <!-- if there are no recurrence dates, the following table will show -->
                      <table cellspacing="0" class="invisible" id="bwCurrentRdatesNone">
                        <tr><th><xsl:copy-of select="$bwStr-AEEF-RecurrenceDates"/></th></tr>
                        <tr><td><xsl:copy-of select="$bwStr-AEEF-NoRecurrenceDates"/></td></tr>
                      </table>

                      <!-- if there are recurrence dates, the following table will show -->
                      <table cellspacing="0" class="invisible" id="bwCurrentRdates">
                        <tr>
                          <th colspan="4"><xsl:copy-of select="$bwStr-AEEF-RecurrenceDates"/></th>
                        </tr>
                        <tr class="colNames">
                          <td><xsl:copy-of select="$bwStr-AEEF-Date"/></td>
                          <td><xsl:copy-of select="$bwStr-AEEF-TIME"/></td>
                          <td><xsl:copy-of select="$bwStr-AEEF-TZid"/></td>
                          <td></td>
                        </tr>
                      </table>

                      <input type="hidden" name="exdates" value="" id="bwExdatesField" />
                      <!-- if there are no exception dates, the following table will show -->
                      <table cellspacing="0" class="invisible" id="bwCurrentExdatesNone">
                        <tr><th><xsl:copy-of select="$bwStr-AEEF-ExceptionDates"/></th></tr>
                        <tr><td><xsl:copy-of select="$bwStr-AEEF-NoExceptionDates"/></td></tr>
                      </table>

                      <!-- if there are exception dates, the following table will show -->
                      <table cellspacing="0" class="invisible" id="bwCurrentExdates">
                        <tr>
                          <th colspan="4"><xsl:copy-of select="$bwStr-AEEF-NoExceptionDates"/></th>
                        </tr>
                        <tr class="colNames">
                          <td><xsl:copy-of select="$bwStr-AEEF-Date"/></td>
                        <td><xsl:copy-of select="$bwStr-AEEF-TIME"/></td>
                        <td><xsl:copy-of select="$bwStr-AEEF-TZid"/></td>
                          <td></td>
                        </tr>
                      </table>
                      <p>
                        <xsl:copy-of select="$bwStr-AEEF-ExceptionDatesMayBeCreated"/>
                      </p>
                    </div>
                  </div>
                </div>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
        <!--  Status  -->
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Status"/>
          </td>
          <td>
            <span>
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
              <input type="radio" name="eventStatus" id="bwStatusConfirmedButton" value="CONFIRMED" checked="checked">
                <xsl:if test="form/status = 'CONFIRMED'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <label for="bwStatusConfirmedButton">
                <xsl:copy-of select="$bwStr-AEEF-Confirmed"/>
              </label>
              <input type="radio" name="eventStatus" id="bwStatusTentativeButton" value="TENTATIVE">
                <xsl:if test="form/status = 'TENTATIVE'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <label for="bwStatusTentativeButton">
                <xsl:copy-of select="$bwStr-AEEF-Tentative"/>
              </label>
              <input type="radio" name="eventStatus" id="bwStatusCancelledButton" value="CANCELLED">
                <xsl:if test="form/status = 'CANCELLED'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
              </input>
              <label for="bwStatusCancelledButton">
                <xsl:copy-of select="$bwStr-AEEF-Canceled"/>
              </label>
            </span>
            <xsl:if test="$canEdit = 'false'">
              <xsl:value-of select="form/status"/>
            </xsl:if>
          </td>
        </tr>
        <!--  Transparency  -->
        <!-- let's not set this in the public client, and let the defaults hold
        <tr>
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-AffectsFreeBusy"/>
          </td>
          <td align="left" class="padMeTop">
            <input type="radio" value="OPAQUE" name="transparency">
              <xsl:if test="form/transparency = 'OPAQUE' or not(form/transparency)"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            <xsl:copy-of select="$bwStr-AEEF-YesOpaque"/>

            <input type="radio" value="TRANSPARENT" name="transparency">
              <xsl:if test="form/transparency = 'TRANSPARENT'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            <xsl:copy-of select="$bwStr-AEEF-NoTransparent"/>
          </td>
        </tr> -->
        <!--  Description  -->
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Description"/>
          </td>
          <td>
            <textarea name="description" id="description" cols="80" rows="8" placeholder="{$bwStr-AEEF-EnterPertientInfo}">
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
              <xsl:value-of select="form/desc/textarea"/>
              <xsl:if test="form/desc/textarea = ''"><xsl:text> </xsl:text></xsl:if>
            </textarea>
            <div class="fieldInfo">
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
              <span class="maxCharNotice"><xsl:value-of select="form/descLength"/><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-CharsMax"/></span>
              <span id="remainingChars">&#160;</span>
            </div>
            <xsl:if test="$canEdit = 'false'">
              <div class="bwHighlightBox">
                <xsl:value-of select="form/desc/textarea"/>
              </div>
            </xsl:if>
          </td>
        </tr>
        <!-- Cost -->
        <tr class="optional">
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Cost"/>
          </td>
          <td>
            <input type="text" size="80" name="eventCost" placeholder="{$bwStr-AEEF-OptionalPlaceToPurchaseTicks}">
              <xsl:attribute name="value"><xsl:value-of select="form/cost/input/@value"/></xsl:attribute>
            </input>
          </td>
        </tr>
        <!-- Url -->
        <tr class="optional">
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-EventURL"/>
          </td>
          <td>
            <input type="text" name="eventLink" size="80" placeholder="{$bwStr-AEEF-OptionalMoreEventInfo}">
              <xsl:attribute name="value"><xsl:value-of select="form/link/input/@value"/></xsl:attribute>
              <!-- xsl:if test="$canEdit = 'false'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if-->
            </input>
          </td>
        </tr>
        <!-- Image Url -->
        <tr class="optional" id="bwImageUrl">
          <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Image"/>
          </td>
          <td>
            <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-IMAGE'] or form/xproperties/node()[name()='X-BEDEWORK-THUMB-IMAGE']">
              <xsl:variable name="imgPrefix">
                <xsl:choose>
                  <xsl:when test="starts-with(form/xproperties/node()[name()='X-BEDEWORK-IMAGE'],'http')"></xsl:when>
                  <xsl:otherwise><xsl:value-of select="$bwEventImagePrefix"/></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="imgThumbPrefix">
                <xsl:choose>
                  <xsl:when test="starts-with(form/xproperties/node()[name()='X-BEDEWORK-THUMB-IMAGE'],'http')"></xsl:when>
                  <xsl:otherwise><xsl:value-of select="$bwEventImagePrefix"/></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <div id="eventFormImage">
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-IMAGE']">
                  <img>
                    <xsl:attribute name="src"><xsl:value-of select="$imgPrefix"/><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-IMAGE']"/></xsl:attribute>
                    <xsl:attribute name="alt"><xsl:value-of select="form/title/input/@value"/></xsl:attribute>
                  </img>
                </xsl:if>
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-THUMB-IMAGE']">
                  <img>
                    <xsl:attribute name="src"><xsl:value-of select="$imgThumbPrefix"/><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-THUMB-IMAGE']"/></xsl:attribute>
                    <xsl:attribute name="alt"><xsl:value-of select="form/title/input/@value"/></xsl:attribute>
                  </img>
                </xsl:if>
              </div>
            </xsl:if>
            <label class="interiorLabel" for="xBwImageHolder">
              <xsl:copy-of select="$bwStr-AEEF-ImageURL"/>
            </label>
            <xsl:text> </xsl:text>
            <input type="text" name="xBwImageHolder" id="xBwImageHolder" value="" size="60" placeholder="{$bwStr-AEEF-OptionalEventImage}">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-IMAGE']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <br/>
            <label class="interiorLabel" for="xBwImageThumbHolder">
              <xsl:copy-of select="$bwStr-AEEF-ImageThumbURL"/>
            </label>
            <xsl:text> </xsl:text>
            <input type="text" name="xBwImageThumbHolder" id="xBwImageThumbHolder" value="" size="60" placeholder="{$bwStr-AEEF-OptionalEventThumbImage}">
              <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-THUMB-IMAGE']/values/text" disable-output-escaping="yes"/></xsl:attribute>
            </input>
            <xsl:if test="/bedework/imageUploadDirectory">
              <br/>
              <label class="interiorLabel" for="eventImageUpload">
                <xsl:copy-of select="$bwStr-AEEF-ImageUpload"/>
              </label>
              <xsl:text> </xsl:text>
              <input type="file" name="eventImageUpload" id="eventImageUpload" size="45"/>
              <input type="checkbox" name="replaceImage" id="replaceImage" value="true"/><label for="replaceImage"><xsl:copy-of select="$bwStr-AEEF-Overwrite"/></label>
              <!-- button name="eventImageUseExisting" id="eventImageUseExisting"><xsl:copy-of select="$bwStr-AEEF-UseExisting"/></button--><br/>
              <div class="fieldInfoAlone">
                <xsl:copy-of select="$bwStr-AEEF-OptionalImageUpload"/>
              </div>
            </xsl:if>
            <xsl:if test="/bedework/creating = 'false' and form/xproperties/node()[name()='X-BEDEWORK-IMAGE']">
              <div class="fieldInfoAlone">
                <button id="eventImageRemoveButton" onclick="removeEventImage(this.form.xBwImageHolder,this.form.xBwImageThumbHolder);return false;"><xsl:copy-of select="$bwStr-AEEF-RemoveImages"/></button>
              </div>
            </xsl:if>
          </td>
        </tr>
        <!-- Location -->
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Location"/>
          </td>
          <td>
            <span>
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
              <xsl:if test="form/location/preferred/select/option">
                <select name="prefLocationId" id="bwPreferredLocationList">
                  <xsl:if test="form/location/all/select/option/@selected and not(form/location/preferred/select/option/@selected)">
                    <xsl:attribute name="class">invisible</xsl:attribute>
                  </xsl:if>
                  <option value="">
                    <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                  </option>
                  <xsl:copy-of select="form/location/preferred/select/*"/>
                </select>
              </xsl:if>
              <select name="allLocationId" id="bwAllLocationList">
                <xsl:if test="form/location/preferred/select/option and not(form/location/all/select/option/@selected and not(form/location/preferred/select/option/@selected))">
                  <xsl:attribute name="class">invisible</xsl:attribute>
                </xsl:if>
                <option value="">
                  <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                </option>
                <xsl:copy-of select="form/location/all/select/*"/>
              </select>
              <xsl:text> </xsl:text>
              <!-- allow for toggling between the preferred and all location listings if preferred
                   locations exist -->
              <xsl:if test="form/location/preferred/select/option">
                <input type="radio" name="toggleLocationLists" id="bwLocationPreferredButton" value="preferred" onclick="changeClass('bwPreferredLocationList','shown');changeClass('bwAllLocationList','invisible');">
                  <xsl:if test="form/location/preferred/select/option and not(form/location/all/select/option/@selected and not(form/location/preferred/select/option/@selected))">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                </input>
                <label for="bwLocationPreferredButton">
                  <xsl:copy-of select="$bwStr-AEEF-Preferred"/>
                </label>
                <input type="radio" name="toggleLocationLists" id="bwLocationAllButton" value="all" onclick="changeClass('bwPreferredLocationList','invisible');changeClass('bwAllLocationList','shown');">
                  <xsl:if test="form/location/all/select/option/@selected and not(form/location/preferred/select/option/@selected)">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                </input>
                <label for="bwLocationAllButton">
                  <xsl:copy-of select="$bwStr-AEEF-All"/>
                </label>
              </xsl:if>
            </span>
            <xsl:if test="$canEdit = 'false'">
              <xsl:value-of select="form/location/all/select/option[@selected]"/>
            </xsl:if>
          </td>
        </tr>

        <xsl:if test="form/location/address and $canEdit = 'true'">
          <tr>
            <td class="fieldName" colspan="2">
              <span class="std-text">
                <span class="bold"><xsl:copy-of select="$bwStr-AEEF-Or"/></span><xsl:text> </xsl:text><xsl:copy-of select="$bwStr-AEEF-Add"/></span>
            </td>
          </tr>
          <tr>
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-Address"/>
            </td>
            <td>
              <xsl:variable name="addressFieldName" select="form/location/address/input/@name"/>
              <xsl:variable name="calLocations">
                <xsl:for-each select="form/location/all/select/option">"<xsl:value-of select="."/>"<xsl:if test="position()!=last()">,</xsl:if>
                </xsl:for-each>
              </xsl:variable>
              <input type="text" size="30" name="{$addressFieldName}" autocomplete="off" onfocus="autoComplete(this,event,new Array({$calLocations}));"/>
              <div class="fieldInfo">
                <xsl:copy-of select="$bwStr-AEEF-IncludeRoom"/>
              </div>
            </td>
          </tr>
          <tr class="optional">
            <td>
              <span class="std-text"><xsl:copy-of select="$bwStr-AEEF-LocationURL"/></span>
            </td>
            <td>
              <xsl:copy-of select="form/location/link/*"/>
              <xsl:text> </xsl:text>
              <span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-OptionalLocaleInfo"/></span>
            </td>
          </tr>
        </xsl:if>

        <!-- Contact -->
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-Contact"/>
          </td>
          <td>
            <span>
              <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
              <xsl:if test="form/contact/preferred/select/option">
                <select name="prefContactId" id="bwPreferredContactList">
                  <xsl:if test="form/contact/all/select/option/@selected and not(form/contact/preferred/select/option/@selected)">
                    <xsl:attribute name="class">invisible</xsl:attribute>
                  </xsl:if>
                  <option value="">
                    <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                  </option>
                  <xsl:copy-of select="form/contact/preferred/select/*"/>
                </select>
              </xsl:if>
              <select name="allContactId" id="bwAllContactList">
                <xsl:if test="form/contact/preferred/select/option and not(form/contact/all/select/option/@selected and not(form/contact/preferred/select/option/@selected))">
                  <xsl:attribute name="class">invisible</xsl:attribute>
                </xsl:if>
                <option value="">
                  <xsl:copy-of select="$bwStr-AEEF-SelectColon"/>
                </option>
                <xsl:copy-of select="form/contact/all/select/*"/>
              </select>
              <xsl:text> </xsl:text>
              <!-- allow for toggling between the preferred and all contacts listings if preferred
                   contacts exist -->
              <xsl:if test="form/contact/preferred/select/option">
                <input type="radio" name="toggleContactLists" id="bwContactPreferredButton" value="preferred" onclick="changeClass('bwPreferredContactList','shown');changeClass('bwAllContactList','invisible');">
                  <xsl:if test="form/contact/preferred/select/option and not(form/contact/all/select/option/@selected and not(form/contact/preferred/select/option/@selected))">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                </input>
                <label for="bwContactPreferredButton">
                  <xsl:copy-of select="$bwStr-AEEF-Preferred"/>
                </label>
                <input type="radio" name="toggleContactLists" id="bwContactAllButton" value="all" onclick="changeClass('bwPreferredContactList','invisible');changeClass('bwAllContactList','shown');">
                  <xsl:if test="form/contact/all/select/option/@selected and not(form/contact/preferred/select/option/@selected)">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                </input>
                <label for="bwContactAllButton">
                  <xsl:copy-of select="$bwStr-AEEF-All"/>
                </label>
              </xsl:if>
            </span>
            <xsl:if test="$canEdit = 'false'">
              <xsl:value-of select="form/contact/all/select/option[@selected]"/>
            </xsl:if>
          </td>
        </tr>

        <xsl:if test="$canEdit = 'false'">
          <tr>
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-Creator"/>
            </td>
            <td>
              <xsl:call-template name="substring-afterLastInstanceOf">
                <xsl:with-param name="string" select="creator"/>
                <xsl:with-param name="char">/</xsl:with-param>
              </xsl:call-template>
            </td>
          </tr>
        </xsl:if>
        
        <!-- Registration settings -->
        <!-- Display and use only if we've set an event reg admin token in the admin web client's system parameters -->
        <xsl:if test="eventregAdminToken != ''">
          <tr class="optional">
            <xsl:if test="$canEdit = 'false'"><xsl:attribute name="class">invisible</xsl:attribute></xsl:if>
            <td class="fieldName"><xsl:copy-of select="$bwStr-AEEF-Registration"/></td>
            <td>
              <input type="checkbox" id="bwIsRegisterableEvent" name="bwIsRegisterableEvent" onclick="showRegistrationFields(this);">
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS']">
                  <xsl:attribute name="checked">checked</xsl:attribute>
                  <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
              </input> 
              <label for="bwIsRegisterableEvent"><xsl:copy-of select="$bwStr-AEEF-UsersMayRegister"/></label>
              
              <div id="bwRegistrationFields" class="invisible">
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS']"><xsl:attribute name="class">visible</xsl:attribute></xsl:if>
                
                <label for="xBwMaxTicketsHolder" class="interiorLabel"><xsl:copy-of select="$bwStr-AEEF-MaxTickets"/></label> 
                <input type="text" name="xBwMaxTicketsHolder" id="xBwMaxTicketsHolder" size="3">
                  <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS']">
                    <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS']/values/text"/></xsl:attribute>
                  </xsl:if>
                </input> 
                <xsl:text> </xsl:text><span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-MaxTicketsInfo"/></span><br/>
                
                <label for="xBwMaxTicketsPerUserHolder" class="interiorLabel"><xsl:copy-of select="$bwStr-AEEF-TicketsAllowed"/></label> 
                <input type="text"  name="xBwMaxTicketsPerUserHolder" id="xBwMaxTicketsPerUserHolder" value="1" size="3">
                  <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS-PER-USER']">
                    <xsl:attribute name="value"><xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-MAX-TICKETS-PER-USER']/values/text"/></xsl:attribute>
                  </xsl:if>
                </input> 
                <xsl:text> </xsl:text><span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-TicketsAllowedInfo"/></span><br/>
                
                <label for="xBwRegistrationOpensDate" class="interiorLabel"><xsl:copy-of select="$bwStr-AEEF-RegistrationOpens"/></label>  
                <div class="dateFields">
                   <input type="text" name="xBwRegistrationOpensDate" id="xBwRegistrationOpensDate" size="10"/>
                </div>
                <div class="timeFields" id="xBwRegistrationOpensTimeFields">
                   <select name="xBwRegistrationOpens.hour" id="xBwRegistrationOpensHour">
                     <xsl:copy-of select="form/start/hour/select/*"/>
                   </select>
                   <select name="xBwRegistrationOpens.minute" id="xBwRegistrationOpensMinute">
                     <xsl:copy-of select="form/start/minute/select/*"/>
                   </select>
                   <xsl:if test="form/start/ampm">
                     <select name="xBwRegistrationOpens.ampm" id="xBwRegistrationOpensAmpm">
                       <xsl:copy-of select="form/start/ampm/select/*"/>
                     </select>
                   </xsl:if>
                   <xsl:text> </xsl:text>
                   <img src="{$resourcesRoot}/images/clockIcon.gif" width="16" height="15" border="0" id="xBwRegistrationOpensClock" alt="*"/>
  
                   <select name="xBwRegistrationOpens.tzid" id="xBwRegistrationOpensTzid" class="timezones">
                     <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                     <option value="-1"><xsl:copy-of select="$bwStr-AEEF-SelectTimezone"/></option>
                     <xsl:variable name="xBwRegistrationOpensTzId" select="form/start/tzid"/>
                     <xsl:for-each select="/bedework/timezones/timezone">
                       <option>
                         <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
                         <xsl:if test="$xBwRegistrationOpensTzId = id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                         <xsl:value-of select="name"/>
                       </option>
                     </xsl:for-each>
                   </select>
                </div>
                <xsl:text> </xsl:text><span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-RegistrationOpensInfo"/></span><br/>
                <!-- Set the registration start date/time fields if populated  -->
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']">
                  <script type="text/javascript">
                    $(document).ready(function() {
                       $("#xBwRegistrationOpensDate").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,1,4)"/>-<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,5,2)"/>-<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,7,2)"/>");
                       <xsl:choose>
                         <xsl:when test="form/start/ampm"><!-- we're in am/pm mode -->
                           $("#xBwRegistrationOpensHour").val(hour24ToAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,10,2)"/>"));
                           $("#xBwRegistrationOpensMinute").val(hour24ToAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,12,2)"/>"));
                           $("#xBwRegistrationOpensAmpm").val(hour24GetAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,10,2)"/>"));
                         </xsl:when>
                         <xsl:otherwise>
                           $("#xBwRegistrationOpensHour").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,10,2)"/>");
                           $("#xBwRegistrationOpensMinute").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/values/text,12,2)"/>");
                         </xsl:otherwise>
                       </xsl:choose>
                       $("#xBwRegistrationOpensTzid").val("<xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-START']/parameters/TZID"/>");
                    });
                  </script>
                </xsl:if>
                
                <label for="xBwRegistrationClosesDate" class="interiorLabel"><xsl:copy-of select="$bwStr-AEEF-RegistrationCloses"/></label>
                <div class="dateFields">
                  <input type="text" name="xBwRegistrationClosesDate" id="xBwRegistrationClosesDate" size="10"/>
                </div>
                <div class="timeFields" id="xBwRegistrationClosesTimeFields">
                  <select name="xBwRegistrationCloses.hour" id="xBwRegistrationClosesHour">
                    <xsl:copy-of select="form/start/hour/select/*"/>
                  </select>
                  <select name="xBwRegistrationCloses.minute" id="xBwRegistrationClosesMinute">
                    <xsl:copy-of select="form/start/minute/select/*"/>
                  </select>
                  <xsl:if test="form/start/ampm">
                    <select name="xBwRegistrationCloses.ampm" id="xBwRegistrationClosesAmpm">
                      <xsl:copy-of select="form/start/ampm/select/*"/>
                    </select>
                  </xsl:if>
                  <xsl:text> </xsl:text>
                  <img src="{$resourcesRoot}/images/clockIcon.gif" width="16" height="15" border="0" id="xBwRegistrationClosesClock" alt="*"/>
  
                  <select name="xBwRegistrationCloses.tzid" id="xBwRegistrationClosesTzid" class="timezones">
                    <xsl:if test="form/floating/input/@checked='checked'"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                    <option value="-1"><xsl:copy-of select="$bwStr-AEEF-SelectTimezone"/></option>
                    <xsl:variable name="xBwRegistrationClosesTzId" select="form/start/tzid"/>
                    <xsl:for-each select="/bedework/timezones/timezone">
                      <option>
                        <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
                        <xsl:if test="$xBwRegistrationClosesTzId = id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                        <xsl:value-of select="name"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </div>
                <xsl:text> </xsl:text><span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-RegistrationClosesInfo"/></span>
                <!-- Set the registration end date/time fields if populated  -->
                <xsl:if test="form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']">
                  <script type="text/javascript">
                    $(document).ready(function() {
                       $("#xBwRegistrationClosesDate").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,1,4)"/>-<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,5,2)"/>-<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,7,2)"/>");
                       <xsl:choose>
                         <xsl:when test="form/start/ampm"><!-- we're in am/pm mode -->
                           $("#xBwRegistrationClosesHour").val(hour24ToAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,10,2)"/>"));
                           $("#xBwRegistrationClosesMinute").val(hour24ToAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,12,2)"/>"));
                           $("#xBwRegistrationClosesAmpm").val(hour24GetAmpm("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,10,2)"/>"));
                         </xsl:when>
                         <xsl:otherwise>
                           $("#xBwRegistrationClosesHour").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,10,2)"/>");
                           $("#xBwRegistrationClosesMinute").val("<xsl:value-of select="substring(form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/values/text,12,2)"/>");
                         </xsl:otherwise>
                       </xsl:choose>
                       $("#xBwRegistrationClosesTzid").val("<xsl:value-of select="form/xproperties/node()[name()='X-BEDEWORK-REGISTRATION-END']/parameters/TZID"/>");
                    });
                  </script>
                </xsl:if>
                <xsl:if test="/bedework/creating = 'false'">
                  <p>
                    <xsl:variable name="registrationsHref"><xsl:value-of select="$bwRegistrationRoot"/>/admin/adminAgenda.do?href=<xsl:value-of select="form/calendar/event/encodedPath"/>/<xsl:value-of select="name"/>&amp;atkn=<xsl:value-of select="eventregAdminToken"/></xsl:variable>
                    <xsl:variable name="registrationsDownloadHref"><xsl:value-of select="$bwRegistrationRoot"/>/admin/download.do?href=<xsl:value-of select="form/calendar/event/encodedPath"/>/<xsl:value-of select="name"/>&amp;atkn=<xsl:value-of select="eventregAdminToken"/></xsl:variable>
                    <button onclick="launchSizedWindow('{$registrationsHref}', '1000', '600');return false;"><xsl:copy-of select="$bwStr-AEEF-ViewRegistrations"/></button>
                    <xsl:text> </xsl:text>
                    <!--<button onclick="location.href='{$registrationsDownloadHref}';return false;"><xsl:copy-of select="$bwStr-AEEF-DownloadRegistrations"/></button>-->
                  </p>
                </xsl:if>
              </div>
            </td>
          </tr>
        </xsl:if>

        <!-- Topical area  -->
        <!-- By selecting one or more of these, appropriate categories will be set on the event -->
        <tr>
          <td class="fieldName">
            <xsl:copy-of select="$bwStr-AEEF-TopicalArea"/>
          </td>
          <td>
            <ul class="aliasTree">
              <xsl:apply-templates select="form/subscriptions/calsuite/calendars/calendar/calendar[isTopicalArea = 'true']" mode="showEventFormAliases">
                <xsl:with-param name="root">false</xsl:with-param>
              </xsl:apply-templates>
            </ul>
          </td>
        </tr>

        <!--  Category  -->
        <!--
          direct setting of categories is deprecated; if you want to reenable, uncomment this block - but
          be forwarned that this will have peculiar consequences if using the submissions client
          -->
        <!--
        <tr>
          <td class="fieldName">
            Categories:
          </td>
          <td>
            <a href="javascript:toggleVisibility('bwEventCategories','visible')">
              show/hide categories
            </a>
            <div id="bwEventCategories" class="invisible">
              <xsl:if test="form/categories/preferred/category and /bedework/creating='true'">
                <input type="radio" name="categoryCheckboxes" value="preferred" checked="checked" onclick="changeClass('preferredCategoryCheckboxes','shown');changeClass('allCategoryCheckboxes','invisible');"/>preferred
                <input type="radio" name="categoryCheckboxes" value="all" onclick="changeClass('preferredCategoryCheckboxes','invisible');changeClass('allCategoryCheckboxes','shown')"/>all<br/>
                <table cellpadding="0" id="preferredCategoryCheckboxes">
                  <tr>
                    <xsl:variable name="catCount" select="count(form/categories/preferred/category)"/>
                    <td>
                      <xsl:for-each select="form/categories/preferred/category[position() &lt;= ceiling($catCount div 2)]">
                        <xsl:sort select="value" order="ascending"/>
                        <input type="checkbox" name="catUid">
                          <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="id">pref-<xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="onchange">setCatChBx('pref-<xsl:value-of select="uid"/>','all-<xsl:value-of select="uid"/>')</xsl:attribute>
                          <xsl:if test="uid = ../../current//category/uid"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                          <xsl:if test="uid = /bedework/currentCalSuite/defaultCategories//category/uid">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                          </xsl:if>
                          <xsl:value-of select="value"/>
                        </input><br/>
                      </xsl:for-each>
                    </td>
                    <td>
                      <xsl:for-each select="form/categories/preferred/category[position() &gt; ceiling($catCount div 2)]">
                        <xsl:sort select="value" order="ascending"/>
                        <input type="checkbox" name="catUid">
                          <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="id">pref-<xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="onchange">setCatChBx('pref-<xsl:value-of select="uid"/>','all-<xsl:value-of select="uid"/>')</xsl:attribute>
                          <xsl:if test="uid = ../../current//category/uid"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                          <xsl:if test="uid = /bedework/currentCalSuite/defaultCategories//category/uid">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                          </xsl:if>
                          <xsl:value-of select="value"/>
                        </input><br/>
                      </xsl:for-each>
                    </td>
                  </tr>
                </table>
              </xsl:if>
              <table cellpadding="0" id="allCategoryCheckboxes">
                <xsl:if test="form/categories/preferred/category and /bedework/creating='true'">
                  <xsl:attribute name="class">invisible</xsl:attribute>
                </xsl:if>
                <tr>
                  <xsl:variable name="catCount" select="count(form/categories/all/category)"/>
                  <td>
                    <xsl:for-each select="form/categories/all/category[position() &lt;= ceiling($catCount div 2)]">
                      <input type="checkbox" name="catUid">
                        <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                        <xsl:if test="/bedework/creating='true'">
                          <xsl:attribute name="id">all-<xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="onchange">setCatChBx('all-<xsl:value-of select="uid"/>','pref-<xsl:value-of select="uid"/>')</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="uid = ../../current//category/uid">
                          <xsl:attribute name="checked">checked</xsl:attribute>
                          <xsl:if test="uid = /bedework/currentCalSuite/defaultCategories//category/uid">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                          </xsl:if>
                        </xsl:if>
                        <xsl:value-of select="value"/>
                      </input><br/>
                    </xsl:for-each>
                  </td>
                  <td>
                    <xsl:for-each select="form/categories/all/category[position() &gt; ceiling($catCount div 2)]">
                      <input type="checkbox" name="catUid">
                        <xsl:attribute name="value"><xsl:value-of select="uid"/></xsl:attribute>
                        <xsl:if test="/bedework/creating='true'">
                          <xsl:attribute name="id">all-<xsl:value-of select="uid"/></xsl:attribute>
                          <xsl:attribute name="onchange">setCatChBx('all-<xsl:value-of select="uid"/>','pref-<xsl:value-of select="uid"/>')</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="uid = ../../current//category/uid">
                          <xsl:attribute name="checked">checked</xsl:attribute>
                          <xsl:if test="uid = /bedework/currentCalSuite/defaultCategories//category/uid">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                          </xsl:if>
                        </xsl:if>
                        <xsl:value-of select="value"/>
                      </input><br/>
                    </xsl:for-each>
                  </td>
                </tr>
              </table>
            </div>
          </td>
        </tr>
        -->
        <!-- note -->
        <!-- let's shut this off for now - needs rewriting if we keep it at all
        <tr>
          <td colspan="2" style="padding-top: 1em;">
            <span class="fieldInfo">
              <strong>If "preferred values" are enabled</strong>
              by your administrator, the calendar, category, location, and contact lists will
              contain only those value you've used previously.  If you don't find the value
              you need in one of these lists, use the "all" list adjacent to each
              of these fields.  The event you select from the "all" list will be added
              to your preferred list from that point on.  <strong>Note: if you don't
              find a location or contact at all, you can add a new one from the
              <a href="{$setup}">main menu</a>.</strong>
              Only administrators can create calendars, however.
              To make sure you've used the
              correct calendar, please see the
              <a href="" target="_blank">Calendar Definitions</a>
            </span>
          </td>
        </tr> -->

        <xsl:if test="form/contact/name and $canEdit = 'true'">
          <tr>
            <td class="fieldName" colspan="2">
              <span class="std-text">
                <span class="bold">or</span> add</span>
            </td>
          </tr>
          <tr>
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-ContactName"/>
            </td>
            <td>
              <xsl:copy-of select="form/contact/name/*"/>
            </td>
          </tr>
          <tr class="optional">
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-ContactPhone"/>
            </td>
            <td>
              <xsl:copy-of select="form/contact/phone/*"/>
              <xsl:text> </xsl:text>
              <span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-Optional"/></span>
            </td>
          </tr>
          <tr class="optional">
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-ContactURL"/>
            </td>
            <td>
              <xsl:copy-of select="form/contact/link/*"/>
              <xsl:text> </xsl:text>
              <span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-Optional"/></span>
            </td>
          </tr>
          <tr class="optional">
            <td class="fieldName">
              <xsl:copy-of select="$bwStr-AEEF-ContactEmail"/>
            </td>
            <td>
              <xsl:copy-of select="form/contact/email/*"/>
              <xsl:text> </xsl:text>
              <span class="fieldInfo"><xsl:copy-of select="$bwStr-AEEF-Optional"/></span> test
              <div id="contactEmailAlert">&#160;</div> <!-- space for email warning -->
            </td>
          </tr>
        </xsl:if>
      </table>
      <xsl:if test="not(starts-with(form/calendar/event/path,$submissionsRootUnencoded))">
        <!-- don't create two instances of the submit buttons on pending events;
             the publishing buttons require numerous unique ids -->
        <xsl:call-template name="submitEventButtons">
          <xsl:with-param name="eventTitle" select="$eventTitle"/>
          <xsl:with-param name="eventUrlPrefix" select="$eventUrlPrefix"/>
          <xsl:with-param name="canEdit" select="$canEdit"/>
        </xsl:call-template>
      </xsl:if>
    </form>
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
            <img src="{$resourcesRoot}/images/catIcon.gif" width="13" height="13" alt="folder" class="folderForAliasTree" border="0"/>
            <xsl:value-of select="summary"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="virtualPath">/user<xsl:for-each select="ancestor-or-self::calendar/name">/<xsl:value-of select="."/></xsl:for-each></xsl:variable>
            <xsl:variable name="displayName" select="summary"/>
            <input type="checkbox" name="alias" id="{generate-id(path)}" onclick="toggleBedeworkXProperty('X-BEDEWORK-ALIAS','{$displayName}','{$virtualPath}',this.checked)">
              <xsl:attribute name="value"><xsl:value-of select="$virtualPath"/></xsl:attribute>
              <xsl:if test="$virtualPath = /bedework/formElements/form/xproperties//X-BEDEWORK-ALIAS/values/text"><xsl:attribute name="checked"><xsl:value-of select="checked"/></xsl:attribute></xsl:if>
              <xsl:if test="path = /bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text"><xsl:attribute name="checked"><xsl:value-of select="checked"/></xsl:attribute></xsl:if>
              <xsl:if test="/bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text = substring-after(aliasUri,'bwcal://')"><xsl:attribute name="checked"><xsl:value-of select="checked"/></xsl:attribute></xsl:if>
            </input>
            <label for="{generate-id(path)}">
            <xsl:choose>
              <xsl:when test="$virtualPath = /bedework/formElements/form/xproperties//X-BEDEWORK-ALIAS/values/text">
                <strong><xsl:value-of select="summary"/></strong>
              </xsl:when>
              <xsl:when test="path = /bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text">
                <strong><xsl:value-of select="summary"/></strong>
              </xsl:when>
              <xsl:when test="/bedework/formElements/form/xproperties//X-BEDEWORK-SUBMIT-ALIAS/values/text = substring-after(aliasUri,'bwcal://')">
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

      <!-- Return topical areas and all underlying calendars.
           Check for topical areas only if the subscription is owned by the calendar suite:
           If the subscription points out to a calendar or folder in another tree,
           return the branch regardless of the topical area setting.  -->
      <xsl:if test="calendar[(isSubscription = 'true' or calType = '0') and ((isTopicalArea = 'true' and  starts-with(path,/bedework/currentCalSuite/resourcesHome)) or not(starts-with(path,/bedework/currentCalSuite/resourcesHome)))]">
        <ul>
          <xsl:apply-templates select="calendar[(isSubscription = 'true' or calType = '0') and ((isTopicalArea = 'true' and  starts-with(path,/bedework/currentCalSuite/resourcesHome)) or not(starts-with(path,/bedework/currentCalSuite/resourcesHome)))]" mode="showEventFormAliases"/>
        </ul>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template name="submitEventButtons">
    <xsl:param name="eventTitle"/>
    <xsl:param name="eventUrlPrefix"/>
    <xsl:param name="canEdit"/>
    
    <xsl:variable name="escapedTitle"><xsl:call-template name="escapeJson"><xsl:with-param name="string" select="$eventTitle"/></xsl:call-template></xsl:variable>
    <div class="submitBox">
      <xsl:choose>
        <!-- xsl:when test="starts-with(form/calendar/event/path,$submissionsRootUnencoded)"-->
        <xsl:when test="/bedework/page = 'modEventPending'">
          <div class="right">
            <input type="submit" name="delete" value="{$bwStr-SEBu-DeleteEvent}"/>
          </div>
          <!-- no need for a publish box in the single calendar model unless we have more than one calendar; -->
          <xsl:choose>
            <xsl:when test="count(form/calendar/all/select/option) &gt; 1"><!-- test for the presence of more than one publishing calendar -->
              <div id="publishBox" class="invisible">
                <div id="publishBoxCloseButton">
                  <a href="javascript:resetPublishBox('calendarId')">
                    <img src="{$resourcesRoot}/images/closeIcon.gif" width="20" height="20" alt="close" border="0"/>
                  </a>
                </div>
                <strong><xsl:copy-of select="$bwStr-SEBu-SelectPublishCalendar"/></strong><br/>
                <select name="calendarId" id="calendarId" onchange="this.form.newCalPath.value = this.value;">
                  <option>
                    <xsl:attribute name="value"><xsl:value-of select="form/calendar/path"/></xsl:attribute>
                    <xsl:copy-of select="$bwStr-SEBu-Select"/>
                  </option>
                  <xsl:for-each select="form/calendar/all/select/option">
                    <xsl:sort select="." order="ascending"/>
                    <option>
                      <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
                      <xsl:if test="@selected"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                      <xsl:choose>
                        <xsl:when test="starts-with(node(),/bedework/submissionsRoot/unencoded)">
                          <xsl:copy-of select="$bwStr-SEBu-SubmittedEvents"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="substring-after(node(),'/public/')"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </option>
                  </xsl:for-each>
                </select>
                <input type="submit" name="publishEvent" value="{$bwStr-SEBu-PublishEvent}">
                  <xsl:attribute name="onclick">doPublishEvent(this.form.newCalPath.value,"<xsl:value-of select="$escapedTitle"/>","<xsl:value-of select="$eventUrlPrefix"/>",this.form);changeClass('publishBox','invisible');</xsl:attribute>
                </input>
                <xsl:if test="$portalFriendly = 'false'">
                  <br/>
                  <span id="calDescriptionsLink">
                    <a href="javascript:launchSimpleWindow('{$calendar-fetchDescriptions}')"><xsl:copy-of select="$bwStr-SEBu-CalendarDescriptions"/></a>
                  </span>
                </xsl:if>
              </div>
              <input type="submit" name="updateSubmitEvent" value="{$bwStr-SEBu-UpdateEvent}"/>
              <input type="button" name="publishEvent" value="{$bwStr-SEBu-PublishEvent}" onclick="changeClass('publishBox','visible')"/>
              <input type="submit" name="cancelled" value="{$bwStr-SEBu-Cancel}"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- we are using the single calendar model for public events -->
              <input type="submit" name="updateSubmitEvent" value="{$bwStr-SEBu-UpdateEvent}"/>
              <input type="submit" name="publishEvent" value="{$bwStr-SEBu-PublishEvent}">
                <xsl:attribute name="onclick">doPublishEvent("<xsl:value-of select="form/calendar/all/select/option/@value"/>","<xsl:value-of select="$escapedTitle"/>","<xsl:value-of select="$eventUrlPrefix"/>",this.form);</xsl:attribute>
              </input>
              <input type="submit" name="cancelled" value="{$bwStr-SEBu-Cancel}"/>
            </xsl:otherwise>
          </xsl:choose>
          <span class="claimButtons">
            <xsl:choose>
              <xsl:when test="form/xproperties/X-BEDEWORK-SUBMISSION-CLAIMANT/values/text = /bedework/userInfo/group">
                <input type="submit" name="updateSubmitEvent" value="{$bwStr-SEBu-ReleaseEvent}" onclick="releasePendingEvent();"/>
              </xsl:when>
              <xsl:otherwise>
                <input type="submit" name="updateSubmitEvent" value="{$bwStr-SEBu-ClaimEvent}">
                  <xsl:attribute name="onclick">claimPendingEvent('<xsl:value-of select="/bedework/userInfo/group"/>','<xsl:value-of select="/bedework/userInfo/currentUser"/>');</xsl:attribute>
                </input>
              </xsl:otherwise>
            </xsl:choose>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="/bedework/creating='true'">
              <input type="submit" name="addEvent" value="{$bwStr-SEBu-AddEvent}"/>
              <input type="submit" name="cancelled" value="{$bwStr-SEBu-Cancel}"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="$canEdit = 'true'">
                <div class="right">
                  <input type="submit" name="delete" value="{$bwStr-SEBu-DeleteEvent}"/>
                </div>
              </xsl:if>
              <input type="submit" name="updateEvent" value="{$bwStr-SEBu-UpdateEvent}"/>
              <xsl:if test="form/recurringEntity != 'true' and recurrenceId = '' and $canEdit = 'true'">
                <!-- cannot duplicate recurring events for now -->
                <input type="submit" name="copy" value="{$bwStr-SEBu-CopyEvent}"/>
              </xsl:if>
              <input type="submit" name="cancelled" value="{$bwStr-SEBu-ReturnToList}"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template match="val" mode="weekMonthYearNumbers">
    <xsl:if test="position() != 1 and position() = last()"> and </xsl:if>
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
    <option value="0"><xsl:copy-of select="$bwStr-RCPO-None"/></option>
    <option value="1"><xsl:copy-of select="$bwStr-RCPO-TheFirst"/></option>
    <option value="2"><xsl:copy-of select="$bwStr-RCPO-TheSecond"/></option>
    <option value="3"><xsl:copy-of select="$bwStr-RCPO-TheThird"/></option>
    <option value="4"><xsl:copy-of select="$bwStr-RCPO-TheFourth"/></option>
    <option value="5"><xsl:copy-of select="$bwStr-RCPO-TheFifth"/></option>
    <option value="-1"><xsl:copy-of select="$bwStr-RCPO-TheLast"/></option>
    <option value=""><xsl:copy-of select="$bwStr-RCPO-Every"/></option>
  </xsl:template>

  <xsl:template name="buildRecurFields">
    <xsl:param name="current"/>
    <xsl:param name="total"/>
    <xsl:param name="name"/>
    <div class="invisible">
      <xsl:attribute name="id"><xsl:value-of select="$name"/>RecurFields<xsl:value-of select="$current"/></xsl:attribute>
      <xsl:copy-of select="$bwStr-BuRF-And"/>
      <select>
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
  
</xsl:stylesheet>