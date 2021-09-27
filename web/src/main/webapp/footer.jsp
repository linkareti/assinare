<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.linkare.assinare.web.Language" />

<footer id="footer" class="bg-dark">
    <p class="copyright text-light">
        <fmt:message key="msg.developedBy" />${' '}
        <a href="http://www.linkare.com" target="_blank">
            <img src="${pageContext.request.contextPath}/linkarelogo.png" alt="Linkare" />
        </a>.
        <br />
        <fmt:message key="msg.version_allRightsReserved">
            <fmt:param value="${initParam['project.version']}" />
        </fmt:message>
    </p>
</footer>
