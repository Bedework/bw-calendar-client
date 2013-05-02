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
  
  <!--==== Head Section ====-->
  <xsl:template name="head">
      <head>
        <title><xsl:copy-of select="$bwStr-Root-PageTitle"/></title>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
        <link rel="stylesheet" href="{$resourcesRoot}/css/default.css"/>
        <link rel="stylesheet" href="/bedework-common/default/default/subColors.css"/>
        <!-- set globals that must be passed in from the XSLT -->
        <script type="text/javascript">
          <xsl:comment>
          var defaultTzid = "<xsl:value-of select="/bedework/now/defaultTzid"/>";
          var startTzid = "<xsl:value-of select="/bedework/formElements/form/start/tzid"/>";
          var endTzid = "<xsl:value-of select="/bedework/formElements/form/end/dateTime/tzid"/>";
          var resourcesRoot = "<xsl:value-of select="$resourcesRoot"/>";
          var imagesRoot = resourcesRoot + "/images";
          </xsl:comment>
        </script>
        <!-- load jQuery  -->
        <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-1.3.2.min.js">&#160;</script>
        <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-ui-1.7.1.custom.min.js">&#160;</script>
        <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/jquery-ui-1.7.1.custom.css"/>
        <link rel="stylesheet" href="/bedework-common/javascript/jquery/css/custom-theme/bedeworkJquery.css"/>
        <!-- Global Javascript (every page): -->
        <script type="text/javascript">
          <xsl:comment>
            $(document).ready(function(){
              // focus first visible,enabled form element:
              $(':input[type=text]:visible:enabled:first:not(.noFocus)').focus();
            });
          </xsl:comment>
        </script>
        <!-- conditional javascript and css -->
        <xsl:if test="/bedework/page='modEvent' or /bedework/page='modEventPending'">
          <!-- import the internationalized strings for the javascript widgets -->
          <xsl:call-template name="bedeworkEventJsStrings"/>
          
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedework.js">&#160;</script>
          <script type="text/javascript" src="/bedework-common/javascript/bedework/bwClock.js">&#160;</script>
          <link rel="stylesheet" href="/bedework-common/javascript/bedework/bwClock.css"/>
          <xsl:choose>
            <xsl:when test="$portalFriendly = 'true'">
              <script type="text/javascript" src="{$resourcesRoot}/javascript/dynCalendarWidget.js">&#160;</script>
              <link rel="stylesheet" href="{$resourcesRoot}/css/dynCalendarWidget.css"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- include the localized jQuery datepicker defaults -->
              <xsl:call-template name="jqueryDatepickerDefaults"/>
            
              <!-- now setup date and time pickers -->  
              <script type="text/javascript">
                <xsl:comment>
                function bwSetupDatePickers() {
                  // startdate
                  $("#bwEventWidgetStartDate").datepicker({
                    <xsl:if test="/bedework/formElements/eventregAdminToken != '' and (/bedework/creating = 'true' or (translate(/bedework/formElements/form/start/rfc3339DateTime,'-:','') = /bedework/formElements/form/xproperties/X-BEDEWORK-REGISTRATION-END/values/text))">altField: "#xBwRegistrationClosesDate",</xsl:if><!-- 
                 -->defaultDate: new Date(<xsl:value-of select="/bedework/formElements/form/start/yearText/input/@value"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="/bedework/formElements/form/start/day/select/option[@selected = 'selected']/@value"/>)
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

                  // recurrence until
                  $("#bwEventWidgetUntilDate").datepicker({
                    <xsl:choose>
                      <xsl:when test="/bedework/formElements/form/recurrence/until">
                        defaultDate: new Date(<xsl:value-of select="substring(/bedework/formElements/form/recurrence/until,1,4)"/>, <xsl:value-of select="number(substring(/bedework/formElements/form/recurrence/until,5,2)) - 1"/>, <xsl:value-of select="substring(/bedework/formElements/form/recurrence/until,7,2)"/>),
                      </xsl:when>
                      <xsl:otherwise>
                        defaultDate: new Date(<xsl:value-of select="/bedework/formElements/form/start/yearText/input/@value"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="/bedework/formElements/form/start/day/select/option[@selected = 'selected']/@value"/>),
                      </xsl:otherwise>
                    </xsl:choose>
                    altField: "#bwEventUntilDate",
                    altFormat: "yymmdd"
                  }).attr("readonly", "readonly");
                  $("#bwEventWidgetUntilDate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/start/rfc3339DateTime,'T')"/>');

                  // rdates and xdates
                  $("#bwEventWidgetRdate").datepicker({
                    defaultDate: new Date(<xsl:value-of select="/bedework/formElements/form/start/yearText/input/@value"/>, <xsl:value-of select="number(/bedework/formElements/form/start/month/select/option[@selected = 'selected']/@value) - 1"/>, <xsl:value-of select="/bedework/formElements/form/start/day/select/option[@selected = 'selected']/@value"/>),
                    dateFormat: "yymmdd"
                  }).attr("readonly", "readonly");
                  $("#bwEventWidgetRdate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/start/rfc3339DateTime,'T')"/>');
                  
                  // rdates and xdates times
                  $("#bwRecExcClock").bwTimePicker({
                    hour24: true,
                    withPadding: true,
                    attachToId: "rdateTimeFields",
                    hourIds: ["eventRdateHour"],
                    minuteIds: ["eventRdateMinute"],
                    hourLabel: "<xsl:value-of select="$bwStr-Cloc-Hour"/>",
                    minuteLabel: "<xsl:value-of select="$bwStr-Cloc-Minute"/>",
                    amLabel: "<xsl:value-of select="$bwStr-Cloc-AM"/>",
                    pmLabel: "<xsl:value-of select="$bwStr-Cloc-PM"/>"
                  });
                  
                  <xsl:if test="/bedework/formElements/eventregAdminToken != ''">
                    // registration open dates
                    $("#xBwRegistrationOpensDate").datepicker().attr("readonly", "readonly");
                    $("#xBwRegistrationOpensDate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/start/rfc3339DateTime,'T')"/>');
                    
                    // registration open time
                    $("#xBwRegistrationOpensClock").bwTimePicker({
                      hour24: <xsl:value-of select="/bedework/hour24"/>,
                      attachToId: "xBwRegistrationOpensTimeFields",
                      hourIds: ["xBwRegistrationOpensHour"],
                      minuteIds: ["xBwRegistrationOpensMinute"],
                      ampmIds: ["xBwRegistrationOpensAmpm"],
                      hourLabel: "<xsl:value-of select="$bwStr-Cloc-Hour"/>",
                      minuteLabel: "<xsl:value-of select="$bwStr-Cloc-Minute"/>",
                      amLabel: "<xsl:value-of select="$bwStr-Cloc-AM"/>",
                      pmLabel: "<xsl:value-of select="$bwStr-Cloc-PM"/>"
                    });
  
                    // registration close dates
                    $("#xBwRegistrationClosesDate").datepicker().attr("readonly", "readonly");
                    $("#xBwRegistrationClosesDate").val('<xsl:value-of select="substring-before(/bedework/formElements/form/start/rfc3339DateTime,'T')"/>');
                    
                    // registration close time
                    $("#xBwRegistrationClosesClock").bwTimePicker({
                      hour24: <xsl:value-of select="/bedework/hour24"/>,
                      attachToId: "xBwRegistrationClosesTimeFields",
                      hourIds: ["xBwRegistrationClosesHour"],
                      minuteIds: ["xBwRegistrationClosesMinute"],
                      ampmIds: ["xBwRegistrationClosesAmpm"],
                      hourLabel: "<xsl:value-of select="$bwStr-Cloc-Hour"/>",
                      minuteLabel: "<xsl:value-of select="$bwStr-Cloc-Minute"/>",
                      amLabel: "<xsl:value-of select="$bwStr-Cloc-AM"/>",
                      pmLabel: "<xsl:value-of select="$bwStr-Cloc-PM"/>"
                    });
                  </xsl:if>
                }
                </xsl:comment>
              </script>
            </xsl:otherwise>
          </xsl:choose>
          <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkUtil.js">&#160;</script>
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedeworkEventForm.js">&#160;</script>
          <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkXProperties.js">&#160;</script>
          <script type="text/javascript">
            <xsl:comment>
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
              <xsl:for-each select="/bedework/formElements/form/xproperties/node()">
                bwXProps.init("<xsl:value-of select="name()"/>",[<xsl:for-each select="parameters/node()">["<xsl:value-of select="name()"/>","<xsl:value-of select="node()"/>"]<xsl:if test="position() != last()">,</xsl:if></xsl:for-each>],"<xsl:call-template name="escapeJson"><xsl:with-param name="string"><xsl:value-of select="values/text"/></xsl:with-param></xsl:call-template>");
              </xsl:for-each>
            }

            $(document).ready(function(){

              <xsl:if test="/bedework/formElements/recurrenceId = ''">
                initRXDates();
              </xsl:if>
              
              <xsl:if test="/bedework/page='modEvent' or /bedework/page='modEventPending'">
                initXProperties();
                bwSetupDatePickers();
                
                // trim the event description:
                $("#description").val($.trim($("#description").val()));
                
                // limit the event description to maxPublicDescriptionLength as configured in cal.options.xml
                $("#description").keyup(function(){  
                  var maxDescLength = parseInt(<xsl:value-of select="/bedework/formElements/form/descLength"/>);  
                  var desc = $(this).val();  
                  var remainingChars = maxDescLength - desc.length;
                  if (remainingChars &lt; 0) {
                    remainingChars = 0;
                  }
                  $("#remainingChars").html(remainingChars + " <xsl:value-of select="$bwStr-AEEF-CharsRemaining"/>"); 
                  if(desc.length > maxDescLength){  
                    var truncDesc = desc.substr(0, maxDescLength);  
                    $(this).val(truncDesc); 
                  };  
                });  
                
              </xsl:if>
              
              <xsl:if test="/bedework/page='listEvents'">
                bwSetupListDatePicker();
              </xsl:if>
                            
              // If you wish to collapse specific topical areas, you can specify them here:
              // (note that this will be managed from the admin client in time)
              // $("ul.aliasTree > li:eq(4) > ul").hide();          
              // $("ul.aliasTree > li:eq(11) > ul").hide();         
              // $("ul.aliasTree > li:eq(13) > ul").hide(); 
              $("ul.aliasTree > li > img.folderForAliasTree").attr("src", '<xsl:value-of select="$resourcesRoot"/>/images/catExpander.gif');
              $("ul.aliasTree > li > img.folderForAliasTree").css("cursor","pointer");
              $("ul.aliasTree > li > img.folderForAliasTree").click(function(){
                $(this).next("ul.aliasTree > li > ul").slideToggle("slow");
              });

            });
            </xsl:comment>
          </script>
        </xsl:if>
        <xsl:if test="/bedework/page='eventList'">
          <!-- include the localized jQuery datepicker defaults -->
          <xsl:call-template name="jqueryDatepickerDefaults"/>
          
          <!-- now setup date and time pickers -->  
          <script type="text/javascript">
            <xsl:comment>
            $(document).ready(function(){
              // startdate for list
              $("#bwListWidgetStartDate").datepicker({
                defaultDate: new Date(<xsl:value-of select="substring(/bedework/now/date,1,4)"/>, <xsl:value-of select="number(substring(/bedework/now/date,5,2)) - 1"/>, <xsl:value-of select="substring(/bedework/now/date,7,2)"/>)
              });
              $("#bwListWidgetStartDate").val('<xsl:value-of select="$curListDate"/>');
            });
            </xsl:comment>
          </script>
        </xsl:if>
        <xsl:if test="/bedework/page='modCalendar' or
                      /bedework/page='modCalSuite' or
                      /bedework/page='modSubscription'">
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedework.js">&#160;</script>
          <link rel="stylesheet" href="/bedework-common/default/default/bedeworkAccess.css"/>
          <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkAccess.js">&#160;</script>
          <xsl:call-template name="localeAccessStringsJsInclude"></xsl:call-template>
          
          <!-- initialize calendar acls, if present -->
          <xsl:if test="/bedework/currentCalendar/acl/ace">
            <script type="text/javascript">
              <xsl:apply-templates select="/bedework/currentCalendar/acl/ace" mode="initJS"/>
            </script>
          </xsl:if>
          <xsl:if test="/bedework/calSuite/acl/ace">
            <script type="text/javascript">
              <xsl:apply-templates select="/bedework/calSuite/acl/ace" mode="initJS"/>
            </script>
          </xsl:if>
        </xsl:if>
        <xsl:if test="/bedework/page='calSuitePrefs'">
          <script type="text/javascript" src="/bedework-common/javascript/jquery/jquery-1.3.2.min.js">&#160;</script>
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedework.js">&#160;</script>
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedeworkPrefs.js">&#160;</script>
        </xsl:if>
        <xsl:if test="/bedework/page='upload' or
                      /bedework/page='selectCalForEvent' or
                      /bedework/page='deleteEventConfirmPending' or
                      /bedework/page='addFilter' or
                      /bedework/page='calSuitePrefs' or
                      /bedework/page='eventList'">
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedework.js">&#160;</script>
          <script type="text/javascript" src="{$resourcesRoot}/javascript/bedeworkEventForm.js">&#160;</script>
          <script type="text/javascript" src="/bedework-common/javascript/bedework/bedeworkUtil.js">&#160;</script>
        </xsl:if>
        <xsl:if test="/bedework/page='calendarDescriptions' or /bedework/page='displayCalendar'">
          <link rel="stylesheet" href="{$resourcesRoot}/css/calendarDescriptions.css"/>
        </xsl:if>
        <xsl:if test="/bedework/page='modResource'">
          <script type="text/javascript" src="{$resourcesRoot}/javascript/modResources.js">&#160;</script>
          <link rel="stylesheet" href="{$resourcesRoot}/css/featuredEventsForm.css"/>
        </xsl:if>
        <link rel="icon" type="image/ico" href="{$resourcesRoot}/images/bedework.ico" />
      </head>
  </xsl:template>
  
</xsl:stylesheet>