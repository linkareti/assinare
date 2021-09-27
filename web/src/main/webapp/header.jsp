<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.linkare.assinare.web.Language" />

<div id="colorbar"></div>

<header class="header">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <a class="navbar-brand" href="${pageContext.request.contextPath}" title="Home" rel="home">
            <img src="${pageContext.request.contextPath}/assinarelogo.png"
                 alt="Wiki Logo" />
        </a>
        <button class="navbar-toggler" type="button" data-toggle="collapse"
                data-target="#navbarNavAltMarkup" aria-controls="navbarNavAltMarkup"
                aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNavAltMarkup">
            <div class="navbar-nav mr-auto">
                <a class="nav-item nav-link ${navbarPage eq 'sign' ? 'active' : ''}"
                   href="${pageContext.request.contextPath}">
                    <fmt:message key="navbar.signature" />
                </a>
                <a class="nav-item nav-link ${navbarPage eq 'id' ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/id">
                    <fmt:message key="navbar.dataExtraction" />
                </a>
            </div>
            <hr class="bg-secondary" />
            <div class="navbar-nav">
                <a class="nav-item nav-link" href="http://www.assinare.eu" target="_blank" rel="noopener">
                    <fmt:message key="navbar.productSite" />${' '}<span class="fas fa-external-link-alt"></span>
                </a>
            </div>
        </div>
    </nav>
</header>
