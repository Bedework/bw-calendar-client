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

  <!-- Subscriptions Calendar Tree -->
  <xsl:template name="subscriptionsTree">
    <div class="secondaryColHeader">
      <h3><xsl:copy-of select="$bwStr-LCol-Calendars"/></h3>
    </div>
    <div id="subsTree">
      <ul>
        <xsl:apply-templates select="/bedework/myCalendars/calendars/calendar" mode="subsTree">
          <xsl:with-param name="isRoot">true</xsl:with-param>
        </xsl:apply-templates>
      </ul>
    </div>
    
    <script type="text/javascript">
	    $(document).ready(function(){
	      var openCals = new Array();
	      <xsl:if test="/bedework/appvar[key='opencals']">
	        var openCalsRaw = "<xsl:value-of select="/bedework/appvar[key='opencals']/value"/>";
	        openCals = openCalsRaw.split(",");
	      </xsl:if>
		    $("#subsTree .subsTreeToggle").click(function() {
		      var curItem = $(this).parent("li");
		      $(curItem).children("ul").slideToggle("fast", function(){
		        if ($(this).is(":visible")) {
		          $(this).parent("li").children("span.subsTreeToggle").html("-");
	            openCals.push($(curItem).attr("id"));
	          } else {
	            var itemIndex = $.inArray($(curItem).attr("id"),openCals);
              $(this).parent("li").children("span.subsTreeToggle").html("+");
	            openCals.splice(itemIndex,1);
	          }
	          
	          $.ajax({
	            url: '/cal/misc/async.do',
	            data: 'setappvar=opencals(' + openCals.toString() + ')',
	            dataType: 'xml'
	          });
		      });
		      
		    });
	    });
	  </script>
  </xsl:template>

  <xsl:template match="calendar" mode="subsTree">
    <xsl:param name="isRoot"/>
    <xsl:variable name="curPath"><xsl:call-template name="escapeJson"><xsl:with-param name="string"><xsl:value-of select="/bedework/selectionState/collection/virtualpath"/></xsl:with-param></xsl:call-template></xsl:variable>
    <xsl:variable name="virtualPath"><xsl:call-template name="escapeJson"><xsl:with-param name="string">/user<xsl:for-each select="ancestor-or-self::calendar/name">/<xsl:value-of select="."/></xsl:for-each></xsl:with-param></xsl:call-template></xsl:variable>
    <xsl:variable name="encVirtualPath"><xsl:call-template name="url-encode"><xsl:with-param name="str" select="$virtualPath"/></xsl:call-template></xsl:variable>
    
    <xsl:variable name="name" select="name"/>
    <xsl:variable name="summary" select="summary"/>
    <xsl:variable name="itemId"><xsl:value-of select="translate(path,'/_- ','')"/></xsl:variable>
    <xsl:variable name="folderState">
      <xsl:choose>
        <xsl:when test="contains(/bedework/appvar[key='opencals']/value,$itemId)">open</xsl:when>
        <xsl:otherwise>closed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <li id="{$itemId}">
      <xsl:if test="calendar and not($isRoot = 'true')">
	      <xsl:attribute name="class">
	        <xsl:choose>
	          <xsl:when test="$virtualPath = $curPath">hasChildren selected <xsl:value-of select="$folderState"/></xsl:when>
	          <xsl:when test="contains($curPath,$virtualPath)">hasChildren selectedPath open</xsl:when>
	          <xsl:otherwise>hasChildren <xsl:value-of select="$folderState"/></xsl:otherwise>
	        </xsl:choose>
	      </xsl:attribute>
	      <span class="subsTreeToggle">
	        <xsl:choose>
	          <xsl:when test="$folderState = 'closed'">+</xsl:when>
	          <xsl:otherwise>-</xsl:otherwise>
	        </xsl:choose>
	      </span>
	    </xsl:if>
	    <xsl:if test="not(calendar) and $virtualPath = $curPath">
	      <xsl:attribute name="class">selected</xsl:attribute>
	    </xsl:if>
	    <xsl:choose>
	      <xsl:when test="$isRoot = 'true'">
	        <xsl:attribute name="class">root</xsl:attribute>
	        <a href="{$setSelection}">
            <xsl:attribute name="href">
              <xsl:choose>
                <xsl:when test="/bedework/page = 'eventList'"><xsl:value-of select="$setSelectionList"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$setSelection"/></xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
	          <xsl:copy-of select="$bwStr-LCol-All"/>
	        </a>
	      </xsl:when>
	      <xsl:otherwise>
	        <a href="{$setSelection}">
	          <xsl:attribute name="href">
	            <xsl:choose>
	              <xsl:when test="/bedework/page = 'eventList'"><xsl:value-of select="$setSelectionList"/>&amp;virtualPath=<xsl:value-of select="$encVirtualPath"/>&amp;setappvar=curCollection(<xsl:value-of select="$name"/>)</xsl:when>
	              <xsl:otherwise><xsl:value-of select="$setSelection"/>&amp;virtualPath=<xsl:value-of select="$encVirtualPath"/>&amp;setappvar=curCollection(<xsl:value-of select="$name"/>)</xsl:otherwise>
	            </xsl:choose>
	          </xsl:attribute>
	          <xsl:value-of select="summary"/>
	        </a>
	      </xsl:otherwise>	      
	    </xsl:choose>
	    <xsl:if test="calendar[(calType &lt; 2) and (name != 'calendar')]"><!-- the test for "calendar" isn't good -->
	      <ul>
	        <xsl:apply-templates select="calendar[(calType &lt; 2) and (name != 'calendar')]" mode="subsTree">
	          <xsl:with-param name="isRoot">false</xsl:with-param>
	        </xsl:apply-templates>
	      </ul>
	    </xsl:if>
    </li>
  </xsl:template>
</xsl:stylesheet>
