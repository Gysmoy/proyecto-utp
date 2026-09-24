<%--
    Document   : home
    Created on : Sep 15, 2026, 9:43:19 AM
    Author     : Christiam Calero
--%>

<%-- El modulo interno ahora es /accesos (protegido por AuthFilter) --%>
<% response.sendRedirect(request.getContextPath() + "/accesos"); %>
