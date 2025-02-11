<%@ tag body-content="empty" %>
<%@ attribute name="value" required="true" type="java.time.LocalDateTime" %>
<%@ attribute name="pattern" required="false" type="java.lang.String" %>

<%
    if (value != null) {
        if (pattern == null || pattern.isEmpty()) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
        String formattedDate = value.format(formatter);
        jspContext.setAttribute("formattedDate", formattedDate);
    }
%>
${formattedDate} 