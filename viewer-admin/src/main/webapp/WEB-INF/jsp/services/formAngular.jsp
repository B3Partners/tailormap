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

<stripes:layout-render name="/WEB-INF/jsp/templates/angularStandardpage.jsp">
    <stripes:layout-component name="head">
        <title><fmt:message key="viewer_admin.form.title" /></title>
    </stripes:layout-component>
    <stripes:layout-component name="header">
        <jsp:include page="/WEB-INF/jsp/header.jsp"/>
    </stripes:layout-component>
    <stripes:layout-component name="body">
        <div id="content">
            <stripes:errors/>
            <stripes:messages/>
            <h1><fmt:message key="viewer_admin.form.header" /><a href="#Soorten_Applicaties_Help" title="<fmt:message key="viewer_admin.form.helptext" />" class="helplink"></a></h1>
            <br/>
            <app-root></app-root>

        <script type="text/javascript">
            vieweradmin.components.Menu.setActiveLink('menu_forms');
        </script>
    </stripes:layout-component>
</stripes:layout-render>
