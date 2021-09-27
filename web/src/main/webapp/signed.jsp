<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.linkare.assinare.web.Language" />

<table class="table table-striped" id="tabelaAssinados">
    <thead>
        <tr>
            <th></th>
            <th><fmt:message key="label.file"/></th>
            <th><fmt:message key="label.size"/></th>
            <th><fmt:message key="label.modified"/></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${files.getSignedFiles(pageContext.session.id)}" var="file">
            <tr>
                <td><span class="fa fa-lock fa-lg text-success"></span></td>
                    <c:url value="GetSignedFile" var="fileUrl">
                        <c:param name="name" value="${file.name}" />
                    </c:url>
                <td>
                    <a href="${fileUrl}">
                        <c:if test="${not empty file.shortName}">
                            <abbr title="${file.name}">${file.shortName}</abbr>
                        </c:if>
                        <c:if test="${empty file.shortName}">
                            ${file.name}
                        </c:if>
                    </a>
                </td>
                <td><fmt:formatNumber value="${file.lengthBytes div 1024}" pattern="0.0Kb" /></td>
                <td><fmt:formatDate value="${file.lastModified}" pattern="dd/MM HH:mm:ss" type="both" /></td>
            </tr>
        </c:forEach>
    </tbody>
</table>
