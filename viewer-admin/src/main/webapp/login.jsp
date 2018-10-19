<%--
Copyright (C) 2012 B3Partners B.V.

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

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="viewer_admin.login.0" /></title>
        <style type="text/css">
            .loginfield {
                width: 130px;
                margin-left: 5px;
            }
        </style>
    </head>
    <body>
        <h2><fmt:message key="viewer_admin.login.1" /></h2>
        <form method="post" action="j_security_check">
            <table>
                <tr><td><fmt:message key="viewer_admin.login.2" />:</td><td><input type="text" name="j_username" class="loginfield" /></td></tr>
                <tr><td><fmt:message key="viewer_admin.login.3" />:</td><td><input type="password" name="j_password" class="loginfield" /></td></tr>
            </table>
            <br />
            <input type="submit" name="submit" value="<fmt:message key="viewer_admin.login.4" />" />
        </form>
        <script type="text/javascript">
            window.onload = function() {
                document.forms[0].j_username.focus();
            }
        </script>
    </body>
</html>
