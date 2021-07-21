<%--
Copyright (C) 2011-2013 B3Partners B.V.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/taglibs.jsp"%>

<stripes:layout-definition>

<!doctype html>
<html>
    <head>
        <meta charset="UTF-8">

        <c:set var="tailorMapComponentsConfigPath" value="${contextPath}/tailormap-components-config"/>
        <c:if test="${param.debug == true}">
            <c:set var="tailorMapComponentsConfigPath" value="http://localhost:3201"/>
        </c:if>
        <script type="text/javascript">
            var tailorMapComponentsConfigPath = <js:quote value="${tailorMapComponentsConfigPath}"/>;
            var contextPath = <js:quote value="${contextPath}"/>;
        </script>
        <link rel="stylesheet" type="text/css" href="${tailorMapComponentsConfigPath}/styles.css">
        <script src="${tailorMapComponentsConfigPath}/vendor.js" type="module"></script>
        <script src="${tailorMapComponentsConfigPath}/runtime.js" type="module"></script>
        <script src="${tailorMapComponentsConfigPath}/polyfills.js" type="module"></script>
        <script src="${tailorMapComponentsConfigPath}/main.js" type="module"></script>

        <stripes:layout-component name="head"/>
    </head>
    <body>
        <stripes:layout-component name="header" />
        <stripes:layout-component name="body"/>
    </body>
</html>

</stripes:layout-definition>
