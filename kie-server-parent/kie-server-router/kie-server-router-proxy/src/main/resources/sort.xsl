<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output omit-xml-declaration="yes" indent="yes" />

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="KIE_ROOT">
    <xsl:copy>
      <xsl:apply-templates select="//*[count(ancestor::*) = LEVEL]">
        <xsl:sort select="KIE_SORT_BY" order="KIE_ORDER" />
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>