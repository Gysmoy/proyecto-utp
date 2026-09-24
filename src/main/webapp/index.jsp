<%--
    Document   : index
    Created on : Sep 15, 2026, 9:38:05 AM
    Author     : Christiam Calero
--%>

<%-- El login ahora lo atiende LoginServlet (/login) --%>
<% response.sendRedirect(request.getContextPath() + "/login"); %>
