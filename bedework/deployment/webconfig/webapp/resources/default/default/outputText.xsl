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
  <!-- ==================================================================== -->
  <!-- ==================================================================== -->
  <!--                           BEDEWORK LUWAK                             -->
  <!-- ==================================================================== -->
  <!-- ==================================================================== -->
  <xsl:output method="text" indent="yes" media-type="text/text" standalone="yes"/>
  <xsl:variable name="appRoot" select="/bedeworkconfig/appRoot"/>
  <xsl:variable name="urlPrefix" select="/bedeworkconfig/urlPrefix"/>

  <xsl:template match="/">
    <xsl:for-each select="/bedeworkconfig/propertyGroups/propertyGroup">
#
# <xsl:value-of select="@name"/>
#
<xsl:for-each select="property">
<xsl:value-of select="@name"/>=<xsl:value-of select="."/><xsl:text>
</xsl:text>
</xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>




