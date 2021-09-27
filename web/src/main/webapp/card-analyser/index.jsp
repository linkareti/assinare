<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<fmt:setBundle basename="com.linkare.assinare.web.Language" />

<!DOCTYPE html>
<html lang="${pageContext.request.locale}">
    <head>
        <fmt:message key="site.title" var="siteTitle" />
        <title>${siteTitle}</title>
        <meta charset="utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="description" content="${siteTitle}" />
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />

        <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon" />

        <%-- Stylesheets --%>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
              integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
              crossorigin="anonymous" />
        <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.0.13/css/all.css"
              integrity="sha384-DNOHZ68U8hZfKXOrtjWvjxusGo9WQnrNx2sqG0tfsghAvtVlRW3tvkXWZh58N9jp"
              crossorigin="anonymous" />
        <link rel="stylesheet" href="../assinareStyles.css" />

        <%-- POLYFILLS --%>
        <script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=es2015,es2016,es2017,fetch,Element.prototype.remove"></script>

        <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
                integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
        crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
                integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
        crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"
                integrity="sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy"
        crossorigin="anonymous"></script>
    </head>
    <body>
        <jsp:directive.include file="../header.jsp" />

        <main class="container">
            <div class="row justify-content-center">
                <div class="col-md-12">
                    <h1 class="text-dark">
                        <fmt:message key="title.cardAnalyserDownload" />
                    </h1>
                </div>
            </div>
            <div class="row justify-content-center">
                <a href="./assinare-card-analyser-${initParam['project.version']}-dist.zip"
                   role="button" class="btn btn-primary btn-lg">
                    <fmt:message key="action.download" /> v${initParam['project.version']}
                </a>
            </div>
        </main>

        <jsp:directive.include file="../footer.jsp" />
    </body>
</html>
