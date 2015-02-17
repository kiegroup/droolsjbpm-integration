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

    <!-- 1. delete generated classes -->
    <xsl:template match="//xs:schema/xs:simpleType[@name='status']" />
    <xsl:template match="//xs:schema/xs:simpleType[@name='subTasksStrategy']" />
    <xsl:template match="//xs:schema/xs:complexType[@name='stringKeyObjectValueMap']" />
    <xsl:template match="//xs:schema/xs:complexType[@name='stringKeyObjectValueEntry']" />
    <xsl:template match="//xs:schema/xs:complexType[@name='jaxbStringObjectPair']" />
   
    <!-- 2. add existing classes -->
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
        <xs:complexType name="subTasksStrategy">
          <xs:annotation>
            <xs:appinfo>
              <jxb:class ref="org.kie.internal.task.api.model.SubTasksStrategy" />
            </xs:appinfo>
          </xs:annotation>
        </xs:complexType>
        <xs:complexType name="stringKeyObjectValueMap">
          <xs:annotation>
            <xs:appinfo>
              <jxb:class ref="org.kie.internal.jaxb.StringKeyObjectValueMap" />
            </xs:appinfo>
          </xs:annotation>
        </xs:complexType>
        <xs:complexType name="stringKeyObjectValueEntry">
          <xs:annotation>
            <xs:appinfo>
              <jxb:class ref="org.kie.internal.jaxb.StringKeyObjectValueMap" />
            </xs:appinfo>
          </xs:annotation>
        </xs:complexType>
        <xs:complexType name="jaxbStringObjectPair">
          <xs:annotation>
            <xs:appinfo>
              <jxb:class ref="org.kie.remote.jaxb.gen.util.JaxbStringObjectPair" />
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
