<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<jsp:useBean class="com.linkare.assinare.web.FilesBean" id="files" scope="application" />

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
        <link rel="stylesheet" href="assinareStyles.css" />

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

        <script type="module" src="./assinareAPI.es6.min.js"></script>
        <script nomodule src="./assinareAPI.es5.min.js"></script>
        <script src="assinare.js"></script>
    </head>
    <body>
        <script src="https://www.java.com/js/deployJava.js"></script>

        <div id="formulario" class="modal" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title">
                            <fmt:message key="title.assinareQuestionnaire" />
                        </h1>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <iframe id="iframeFormulario" sandbox="allow-forms"
                                src="https://docs.google.com/forms/d/1yQT3_Jxj29L-d1XYQSlHOnjoN0gIplt4n4o1TEIi_f0/viewform?embedded=true">
                            ${' '}<fmt:message key="msg.loadingQuestionnaire" />${' '}
                        </iframe>
                    </div>
                </div>
            </div>
        </div>

        <c:set var="navbarPage" value="sign" scope="request"/>
        <jsp:directive.include file="header.jsp" />

        <c:set var="hasDocAccess" value="true" scope="session"/>

        <main class="container">
            <div class="row justify-content-center">
                <div class="col-md-12">
                    <h1 class="text-dark">
                        <fmt:message key="title.signatureDemonstrationPage" />
                    </h1>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <h4 class="unsigned bg-dark text-light">
                        <span class="fa fa-lock-open"></span>
                        <fmt:message key="title.originalDocuments" />
                    </h4>
                    <table class="table table-striped table-hover" id="origFilesTbl">
                        <thead>
                            <tr>
                                <th></th>
                                <th><fmt:message key="label.file" /></th>
                                <th><fmt:message key="label.size" /></th>
                                <th><fmt:message key="label.modified" /></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${files.originalFiles}" var="file">
                                <tr>
                                    <td><input type="checkbox" value="${file.name}" /></td>
                                    <td>
                                        <c:url value="GetFile" var="fileUrl">
                                            <c:param name="name" value="${file.name}" />
                                        </c:url>
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
                                    <td><fmt:formatDate value="${file.lastModified}" dateStyle="SHORT" /></td>
                                    <td></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="btn-toolbar mb-2" role="toolbar">
                        <div class="btn-group flex-grow-1" role="group">
                            <button id="chooseLocalFiles" class="btn btn-outline-secondary"
                                    onclick="chooseLocalFiles();" disabled="true">
                                <fmt:message key="action.chooseLocalFiles" />
                            </button>
                        </div>
                        <div class="btn-group ml-2" role="group">
                            <button id="signButton" class="btn btn-success float-right"
                                    onclick="doSign();" disabled="true">
                                <fmt:message key="action.signPdf" />
                            </button>
                        </div>
                        <div class="btn-group ml-2" role="group">
                            <button id="signContainerButton" class="btn btn-success float-right"
                                    onclick="doSignContainer();" disabled="true">
                                <fmt:message key="action.signAsic" />
                            </button>
                        </div>
                    </div>

                    <div>
                        <object id="myApplet" type="application/x-java-applet" width="78"
                                height="35">
                            <param name="code" value="dummy" />
                            <param name="jnlp_href" value="assinare.jnlp" />
                            <%-- writing param's in this fashion has not been fully tested --%>
                            <%-- if it fails, we can always write the entire object tag with document.write() --%>
                            <script>
                                document.write('<param name="get_file_url_prefix" value="'
                                        + asnParams.getFileUrlPrefix + '" />');
                                document.write('<param name="get_signed_file_url_prefix" value="'
                                        + asnParams.getSignedFileUrlPrefix + '" />');
                                document.write('<param name="put_file_url" value="' + asnParams.putFileUrl + '" />');
                                document.write('<param name="auth_cookies" value="' + asnParams.authCookies + '" />');
                                document.write('<param name="language" value="' + asnParams.language + '" />');
                                if (asnParams.country) {
                                    document.write('<param name="country" value="' + asnParams.country + '" />');
                                }
                            </script>
                            <param name="separate_jvm" value="true" />
                            <param name="java_status_events" value="true" />
                            <div class="alert alert-warning" role="alert">
                                <fmt:message key="msg.javaPluginNotFound" />
                                ${' '}
                                <a class="btn btn-sm btn-java"
                                   href="javascript:deployJava.launch('./assinareDaemon.jnlp');">
                                    <span class="fab fa-java"></span>${' '}<fmt:message key="action.launch" />
                                </a>
                            </div>
                        </object>
                    </div>
                </div>
                <div class="col-md-6">
                    <h4 class="signed bg-success text-light">
                        <span class="fa fa-lock"></span>
                        <fmt:message key="title.signedDocuments" />
                    </h4>
                    <jsp:include page="signed.jsp" />

                    <div class="btn-toolbar justify-content-end mb-2" role="toolbar">
                        <div class="btn-group ml-2" role="group">
                            <a class="btn btn-warning float-right"
                               id="download"
                               href="${initParam['web-start-launcher.url']}/?context=${initParam['assinare-web.url']}"
                               target="_blank"><fmt:message key="action.checkCompatibility" /></a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12" id="messageBox"></div>
            </div>

            <div class="row">
                <div class="col-md-12" id="infoBrowser"></div>
            </div>
        </main>

        <jsp:directive.include file="footer.jsp" />
    </body>
</html>
