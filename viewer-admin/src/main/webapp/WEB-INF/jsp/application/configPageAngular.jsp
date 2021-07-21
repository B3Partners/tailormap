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

<stripes:layout-render name="/WEB-INF/jsp/templates/angular.jsp">

    <stripes:layout-component name="head">
        <title><fmt:message key="viewer_admin.configpage.0" /></title>
    </stripes:layout-component>

    <stripes:layout-component name="header">
    </stripes:layout-component>


    <stripes:layout-component name="body"><%--@elvariable id="actionBean" type="nl.tailormap.viewer.admin.stripes.LayoutManagerActionBean"--%>
        <stripes:form beanclass="nl.tailormap.viewer.admin.stripes.LayoutManagerActionBean" id="configForm">
            <input type="hidden" name="component" value="${actionBean.component.id}"/>
            <stripes:hidden name="className" value="${actionBean.className}"/>
            <stripes:hidden name="name" value="${actionBean.name}"/>
            <stripes:hidden id="componentLayout" name="componentLayout"/>
            <stripes:hidden name="configObject" id="configObject"/>
            <stripes:hidden name="saveComponentConfig" value="Opslaan" />
            <stripes:hidden name="currentRegion" value="${param.currentRegion}" />
        </stripes:form>
        <app-root></app-root>
        <div id="component-container"></div>
        <button id="save-button">Opslaan</button>
        <script type="text/javascript">
            window.addEventListener('DOMContentLoaded',function () {
                <c:set var="configObject"><js:quote>${actionBean.component.config}</js:quote></c:set>
                <c:if test="${!empty actionBean.component.config}">
                <c:set var="configObject"><js:quote>${actionBean.component.config}</js:quote></c:set>
                </c:if>
                const configEl = document.createElement('${actionBean.ngConfigComponent}');
                configEl.setAttribute('application-id', '${actionBean.application.id}');
                configEl.setAttribute('config', ${configObject});
                configEl.addEventListener('configChanged', function(config) {
                    console.log(config);
                    document.querySelector('#configObject').value = JSON.stringify(config);
                });
                document.querySelector('#component-container').appendChild(configEl);
                document.querySelector('#save-button').addEventListener('click', function() {
                    document.querySelector('#configForm').submit();
                });
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>
