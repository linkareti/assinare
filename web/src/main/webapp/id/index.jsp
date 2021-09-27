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
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css"
              integrity="sha384-9gVQ4dYFwwWSjIDZnLEWnxCjeSWFphJiwGPXr1jddIhOegiu1FwO5qRGvFXOdJZ4"
              crossorigin="anonymous" />
        <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.0.10/css/all.css"
              integrity="sha384-+d0P83n9kaQMCwj8F4RJB66tzIwOKmrdb46+porD/OvrJ+37WqIM7UoBtwHO6Nlg"
              crossorigin="anonymous" />
        <link rel="stylesheet" href="../assinareStyles.css" />

        <%-- POLYFILLS --%>
        <script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=es2015,es2016,es2017,fetch"></script>

        <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
                integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
        crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.0/umd/popper.min.js"
                integrity="sha384-cs/chFZiN24E4KMATLdqdvsezGxaGsi4hLGOzlXwp5UZB1LY//20VyM2taTB4QvJ"
        crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js"
                integrity="sha384-uefMccjFJAIv6A+rW+L4AHf99KvxDjWSu1z9VI8SKNVmz4sk7buKt/6v9KI65qnm"
        crossorigin="anonymous"></script>
        <script type="module" src="../assinareAPI.es6.min.js"></script>
        <script nomodule src="../assinareAPI.es5.min.js"></script>
    </head>
    <body>
        <div class="blocker d-none"></div>
        <script src="https://www.java.com/js/deployJava.js"></script>

        <c:set var="navbarPage" value="id" scope="request"/>
        <jsp:directive.include file="../header.jsp" />

        <main class="container">

            <div class="row justify-content-center">
                <div class="col-md-12">
                    <h1 class="text-dark">
                        <fmt:message key="title.dataRetrievalPage" />
                    </h1>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <object id="assinareIdApplet" type="application/x-java-applet"
                            width="78" height="37">
                        <param name="code" value="dummy" />
                        <param name="jnlp_href" value="../assinareId.jnlp" />
                        <param name="auth_cookies"
                               value="JSESSIONID=${pageContext.session.id};" />
                        <param name="java_status_events" value="true" />
                        <div class="alert alert-warning" role="alert">
                            <fmt:message key="msg.javaPluginNotFound" />
                            ${' '}
                            <a class="btn btn-sm btn-java"
                               href="javascript:deployJava.launch('../assinareDaemon.jnlp');">
                                <span class="fab fa-java"></span>${' '}<fmt:message key="action.launch" />
                            </a>
                        </div>
                    </object>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="btn-toolbar justify-content-end mb-2" role="toolbar">
                        <div class="btn-group ml-2" role="group">
                            <button data-reenable="true" id="citizenDataButton"
                                    class="btn btn-primary" onclick="getCitizenData();"
                                    disabled="true" autocomplete="off">
                                <fmt:message key="action.id" />
                            </button>
                        </div>
                        <div class="btn-group ml-2" role="group">
                            <button data-reenable="true" id="citizenPictureButton"
                                    class="btn btn-primary" onclick="getCitizenPicture();"
                                    disabled="true" autocomplete="off">
                                <fmt:message key="action.photo" />
                            </button>
                        </div>
                        <div class="btn-group ml-2" role="group">
                            <button data-reenable="true" id="citizenAddressButton"
                                    class="btn btn-warning" onclick="getCitizenAddress();"
                                    disabled="true" autocomplete="off">
                                <span class="fa fa-unlock"></span>${' '}<fmt:message key="action.address" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div id="cardId"></div>
                </div>
            </div>

            <div class="row">
                <div id="fake-cc-form" class="col-md-12">

                    <%-- forced to use <nav> notation since <ul> notation isn't working properly with BS 4.1 --%>
                    <nav>
                        <div class="nav nav-tabs" role="tablist">
                            <a class="nav-item nav-link active" href="#identificacaoCivil" data-toggle="tab" role="tab">
                                <fmt:message key="title.identity" />
                            </a>
                            <a class="nav-item nav-link" href="#outrosDados" data-toggle="tab" role="tab">
                                <fmt:message key="title.otherData" />
                            </a>
                            <a class="nav-item nav-link" href="#morada" data-toggle="tab" role="tab">
                                <fmt:message key="title.address" />
                            </a>
                        </div>
                    </nav>

                    <div class="tab-content">
                        <div id="identificacaoCivil" class="tab-pane fade show active" role="tabpanel">
                            <div class="row">
                                <div class="col-md-9">
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label>
                                                <fmt:message key="label.surnames" />
                                            </label>
                                            <p id="name">-</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label>
                                                <fmt:message key="label.givenNames" />
                                            </label>
                                            <p id="firstname">-</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-2">
                                            <label>
                                                <fmt:message key="label.gender" />
                                            </label>
                                            <p id="sex">-</p>
                                        </div>
                                        <div class="col-md-2">
                                            <label>
                                                <fmt:message key="label.height" />
                                            </label>
                                            <p id="height">-</p>
                                        </div>
                                        <div class="col-md-4">
                                            <label>
                                                <fmt:message key="label.nationality" />
                                            </label>
                                            <p id="nationality">-</p>
                                        </div>
                                        <div class="col-md-4">
                                            <label>
                                                <fmt:message key="label.dateOfBirth" />
                                            </label>
                                            <p id="birthDate">-</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <label>
                                                <fmt:message key="label.documentNumber" />
                                            </label>
                                            <p id="cardNumber">-</p>
                                        </div>
                                        <div class="col-md-6">
                                            <label>
                                                <fmt:message key="label.validityDate" />
                                            </label>
                                            <p id="validityDate">-</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label>
                                                <fmt:message key="label.country" />
                                            </label>
                                            <p id="country">-</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label>
                                                <fmt:message key="label.affiliation" />
                                            </label>
                                            <p>
                                                <span id="firstnameFather" class="mr-1">-</span><span id="nameFather"></span>
                                            </p>
                                            <p>
                                                <span id="firstnameMother" class="mr-1">-</span><span id="nameMother"></span>
                                            </p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label>
                                                <fmt:message key="label.notes" />
                                            </label>
                                            <p id="notes">-</p>
                                        </div>
                                    </div>
                                </div>
                                <fmt:message key="label.userPicture" var="lblUserPicture" />
                                <div class="col-md-3">
                                    <a id="picture" download="photo.jp2"title="${lblUserPicture}">
                                        <img src="img/silhouette.jpg" alt="${lblUserPicture}" class="user-photo" />
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div id="outrosDados" class="tab-pane fade" role="tabpanel">
                            <div class="row">
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.vatIdentificationNo" />
                                    </label>
                                    <p id="numNIF">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.socialSecurityNo" />
                                    </label>
                                    <p id="numSS">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.nationalHealthSystemNo" />
                                    </label>
                                    <p id="numSNS">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.cardVersion" />
                                    </label>
                                    <p id="cardVersion">-</p>
                                </div>
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.deliveryDate" />
                                    </label>
                                    <p id="deliveryDate">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.deliveryEntity" />
                                    </label>
                                    <p id="deliveryEntity">-</p>
                                </div>
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.documentType" />
                                    </label>
                                    <p id="documentType">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <label>
                                        <fmt:message key="label.deliveryLocation" />
                                    </label>
                                    <p id="locale">-</p>
                                </div>
                            </div>
                        </div>

                        <div id="morada" class="tab-pane fade" role="tabpanel">
                            <div class="row">
                                <div class="col-md-12">
                                    <label>
                                        <fmt:message key="label.district" />
                                    </label>
                                    <p id="morada.districtDesc">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <label>
                                        <fmt:message key="label.municipality" />
                                    </label>
                                    <p id="morada.municipalityDesc">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <label>
                                        <fmt:message key="label.civilParish" />
                                    </label>
                                    <p id="morada.freguesiaDesc">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-2">
                                    <label>
                                        <fmt:message key="label.streetType" />
                                    </label>
                                    <p id="morada.streettype">-</p>
                                </div>
                                <div class="col-md-10">
                                    <label>
                                        <fmt:message key="label.streetName" />
                                    </label>
                                    <p id="morada.street">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.streetTypeAbbr" />
                                    </label>
                                    <p id="morada.streettypeAbbr">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.buildingAbbr" />
                                    </label>
                                    <p id="morada.buildingAbbr">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.building" />
                                    </label>
                                    <p id="morada.building">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.houseBuildingNo" />
                                    </label>
                                    <p id="morada.door">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.floor" />
                                    </label>
                                    <p id="morada.floor">-</p>
                                </div>
                                <div class="col-md-4">
                                    <label>
                                        <fmt:message key="label.side" />
                                    </label>
                                    <p id="morada.side">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.place" />
                                    </label>
                                    <p id="morada.place">-</p>
                                </div>
                                <div class="col-md-6">
                                    <label>
                                        <fmt:message key="label.locality" />
                                    </label>
                                    <p id="morada.locality">-</p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-3">
                                    <label>
                                        <fmt:message key="label.zipCode" />
                                    </label>
                                    <p id="morada.cp">-</p>
                                </div>
                                <div class="col-md-9">
                                    <label>
                                        <fmt:message key="label.postalLocality" />
                                    </label>
                                    <p id="morada.postal">-</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div id="infoBrowser"></div>
                </div>
            </div>
        </main>

        <jsp:directive.include file="../footer.jsp" />
        <script src="assinareId.js"></script>
    </body>
</html>
