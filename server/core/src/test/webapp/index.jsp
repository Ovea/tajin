<%--

    Copyright (C) 2011 Ovea <dev@ovea.com>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head><title>JSP Test</title></head>
<body bgcolor="white">
<form name="localeForm" action="index.jsp" method="post">
    <span style="font-weight: bold;">Languages:</span>
    <select name=locale>
        <%
            for (Locale locale : Locale.getAvailableLocales()) {
        %>
        <option value="<%=locale%>"><%=locale.getDisplayLanguage(locale)%>
        </option>
        <%
            }
        %>
    </select>
</form>
</body>
</html>
