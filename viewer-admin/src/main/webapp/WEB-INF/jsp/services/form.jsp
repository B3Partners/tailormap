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

<stripes:layout-render name="/WEB-INF/jsp/templates/ext.jsp">
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
            <div id="form-container" class="forms">

                <stripes:form beanclass="nl.b3p.viewer.admin.stripes.FormActionBean">
                <select name="featureType" id="featureType">
                    <option value="-">-- Selecteer --</option>
                    <c:forEach var="ft" items="${actionBean.featureTypes}">
                        <option value="${ft}"> <c:out value="${ft}"/></option>
                    </c:forEach>
                </select>
                <div id="grid-container" class="attribute"></div>

                <stripes:submit name="add" value="add">Voeg toe</stripes:submit>
                </stripes:form>
            </div>
            <div id="form-container" class="form">
                <iframe src="<stripes:url beanclass="nl.b3p.viewer.admin.stripes.FormActionBean" event="cancel"/>" id="editFrame" frameborder="0"></iframe>
            </div>
        </div>
        <script type="text/javascript" src="${contextPath}/resources/js/services/form.js"></script>
        <script type="text/javascript">
            var forms = ${actionBean.forms};

            Ext.onReady(function() {
                // Expose vieweradmin_components_Bookmark to global scope to be able to access the component from the iframe
                Ext.create('vieweradmin.components.Form', {
                    forms:forms,
                    gridurl: '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.FormActionBean" event="getGridData"/>',
                   <%-- editurl: '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.FormActionBean" event="edit"/>',
                    deleteurl: '<stripes:url beanclass="nl.b3p.viewer.admin.stripes.FormActionBean" event="delete"/>',--%>

                });
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>
