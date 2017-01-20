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

   <xsl:template match="//*[count(ancestor::*) = LEVEL][position() &lt;= START] | //*[count(ancestor::*) = LEVEL][position() &gt; SIZE]">
  </xsl:template>

</xsl:stylesheet>