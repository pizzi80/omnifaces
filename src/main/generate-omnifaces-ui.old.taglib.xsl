<!--

    Copyright OmniFaces

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
    the License. You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
    specific language governing permissions and limitations under the License.

-->
<xsl:stylesheet xmlns:jakartaee="https://jakarta.ee/xml/ns/jakartaee" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:template match="node()|@*" name="identity">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="jakartaee:facelet-taglib/jakartaee:description/text()">
        This is a copy of omnifaces.taglib.xml into omnifaces-ui.taglib.xml with old namespace for sake of backwards compatibility.
        This will be removed in a future OmniFaces version.
        So, you need to migrate XML namespace from xmlns:o="http://omnifaces.org/ui" to xmlns:o="omnifaces" namespace as soon as possible.
    </xsl:template>

    <xsl:template match="jakartaee:namespace/text()">http://omnifaces.org/ui</xsl:template>

    <xsl:template match="jakartaee:facelet-taglib/jakartaee:tag/jakartaee:description/text()">
        Please migrate XML namespace from xmlns:o="http://omnifaces.org/ui" to xmlns:o="omnifaces" as soon as possible.
    </xsl:template>

    <xsl:template match="jakartaee:facelet-taglib/jakartaee:tag/jakartaee:attribute/jakartaee:description" />

    <xsl:template match="jakartaee:facelet-taglib/jakartaee:function" />
</xsl:stylesheet>