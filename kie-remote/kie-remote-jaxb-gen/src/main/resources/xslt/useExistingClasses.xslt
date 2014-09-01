<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml used during 
	the integration tests. -->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:jxb="http://java.sun.com/xml/ns/jaxb" 
	version="1.0">

	<xsl:output method="xml" indent="yes" />

    <!-- delete generated classes -->
    <xsl:template match="//xs:schema/xs:simpleType[@name='status']" />
   
    <!-- add existing classes -->
    <xsl:template match="//xs:schema">
      <xs:schema 
        version="1.0" 
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:jxb="http://java.sun.com/xml/ns/jaxb" 
        xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
        jxb:extensionBindingPrefixes="xjc" 
        jxb:version="1.0">
        <!-- new class to add -->
        <xs:complexType name="status">
          <xs:annotation>
            <xs:appinfo>
              <jxb:class ref="org.kie.api.task.model.Status" />
            </xs:appinfo>
          </xs:annotation>
        </xs:complexType>
        <!-- keep what was already there -->
        <xsl:apply-templates select="@* | *" />
      </xs:schema>
    </xsl:template>
    
	<!-- Copy everything else. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
