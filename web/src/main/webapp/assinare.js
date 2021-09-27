(function () {
    var ctxPath = location.origin + location.pathname.substring(0, location.pathname.lastIndexOf("/"));
    var jsessionId = document.cookie.match(/(?:(?:^|.*;\s*)JSESSIONID\s*\=\s*([^;]*).*$)/)[1];
    var langSubtags = document.documentElement.lang.split("-");

    window.asnParams = {
        assinareSignApplet: "myApplet",
        getFileUrlPrefix: ctxPath + "/GetFile?name=",
        getSignedFileUrlPrefix: ctxPath + "/GetSignedFile?name=",
        putFileUrl: ctxPath + "/PutFile",
        authCookies: "JSESSIONID=" + jsessionId + ";",
        language: langSubtags[0],
        country: langSubtags[1] || ""
    };
})();

var assinareSignInstance;

$(function () {
    var infoList = JSON.stringify({
        javaEnabled: window.navigator.javaEnabled(),
        cookieEnabled: window.navigator.cookieEnabled,
        userAgent: window.navigator.userAgent
    });

    var form = document.getElementById("iframeFormulario");
    form.src = "https://docs.google.com/forms/d/1yQT3_Jxj29L-d1XYQSlHOnjoN0gIplt4n4o1TEIi_f0/viewform?entry.758857261&entry.385421706="
            + window.encodeURIComponent(infoList);

    // force lazy load
    var params = Object.assign({}, window.asnParams, {lazy: true});
    window.assinare.sign.getInstance(
            params,
            function (err, newInstance) {
                if (err) {
                    console.error("Erro ao inicializar o Assinare", err);
                } else {
                    assinareSignInstance = newInstance;
                    appletStartCallback();
                }
            }
    );
});

function doSign() {
    var checkboxes = document
            .querySelectorAll("#origFilesTbl input[type=checkbox]");

    var signList = new Array;
    for (var i in checkboxes) {
        if (checkboxes[i].checked) {
            signList.push(checkboxes[i].value);
        }
    }

    document.getElementById('messageBox').innerHTML = '';

    assinareSignInstance.signDocuments(
            signList,
            "signingDoneMsg"
//            , {
//                reason: "I produced this document",
//                location: "Coimbra",
//                pageNumber: 5,
//                sigRenderingMode: "LOGO_CHOOSED_BY_USER",
//                logoFileURL: "http://test.assinare.eu/assinare-web/id/img/silhouette.jpg",
//                tsaUrl: "none"
//            }
    );
}

function doSignContainer() {
    var checkboxes = document
            .querySelectorAll("#origFilesTbl input[type=checkbox]");

    var signList = new Array;
    for (var i in checkboxes) {
        if (checkboxes[i].checked) {
            signList.push(checkboxes[i].value);
        }
    }

    document.getElementById('messageBox').innerHTML = '';

    assinareSignInstance.signContainer(
            signList.join(','),
            "signingDoneMsg"
//            , {
//                claimedRole: "I am the owner",
//                location: "Coimbra",
//                commitmentType: "PROOF_OF_APPROVAL",
//                tsaUrl: "none",
//                withLTA: true
//            }
            );
}

function appletStartCallback() {
    document.getElementById('signButton').disabled = false;
    document.getElementById('signContainerButton').disabled = false;
    document.getElementById('chooseLocalFiles').disabled = false;
}

function signingDoneMsg(error, data) {
    var message = document.createElement("div");
    if (!error) {
        console.info(data);
        if (data.signatureStatus === "0") {
            message.className = "alert alert-success";
            message.textContent = "Processo de assinatura de " + data.docName
                    + " terminado com sucesso.";
        } else if (data.signatureStatus === "1") {
            message.className = "alert alert-success";
            message.textContent = "Processo Assinare terminado para : "
                    + data.docName;
            if (sessionStorage.getItem("formularioApresentado") !== "true") {
                $("#formulario").modal("show");
                sessionStorage.setItem("formularioApresentado", "true");
            }
        }
    } else {
        console.error(error);
        if (error.signatureStatus === "2") {
            message.className = "alert alert-danger";
            message.textContent = "Processo de assinatura do documento "
                    + error.docName + " terminado com erro: " + error.message;
        } else if (error.signatureStatus === "3") {
            message.className = "alert alert-danger";
            message.textContent = "Processo de lote de assinaturas terminado com erro: "
                    + error.message;
        } else if (error.signatureStatus === "5") {
            message.className = "alert alert-danger";
            message.textContent = "Erro no download do documento a assinar: "
                    + error.docName + " terminado com HTTP status code: "
                    + error.http.status + " message: " + error.http.message;
        } else if (error.signatureStatus === "6") {
            message.className = "alert alert-danger";
            message.textContent = "Erro no upload do documento assinado: "
                    + error.docName + " terminado com HTTP status code: "
                    + error.http.status + " e response message:  "
                    + error.http.message;
        } else if (error.signatureStatus === "7") {
            message.className = "alert alert-danger";
            message.textContent = error.message + " na assinatura do documento:"
                    + error.docName;
        } else {
            message.className = "alert alert-danger";
            message.textContent = "Processo de assinatura terminado com erro: "
                    + error.message;
        }
    }

    document.getElementById('messageBox').appendChild(message);

    fetch("signed.jsp").then(
            function (response) {
                return response.text();
            }
    ).then(
            function (data) {
                var parsedData = $(data);
                var tabelaAssinados = parsedData.is("#tabelaAssinados") ? parsedData : parsedData.find("#tabelaAssinados");
                tabelaAssinados.replaceAll($("#tabelaAssinados"));
            }
    );

    clearLocalFileRows();
}

function clearLocalFileRows() {
    $('.local').remove();
    $('.local-title').remove();
}

function removeLocalFile(element) {
    element.parentNode.parentNode.remove();
    if ($('.local').length === 0) {
        clearLocalFileRows();
    }
}

function chooseLocalFiles() {
    assinareSignInstance.chooseLocalFiles().then(
            function (localFiles) {
                var tbody = $("table#origFilesTbl tbody");

                if (localFiles.length !== 0
                        && !$('.local').length) {
                    var titleLine = $(document.createElement("tr"));

                    titleLine.attr("class", "local-title");
                    titleLine.append(
                            "<td colspan='5' style='text-align: center; font-weight: bold;'> &#8211; Ficheiros no sistema de ficheiros local &#8211;</td>"
                            );

                    tbody.append(titleLine);
                }

                localFiles.forEach(
                        function (filePath) {
                            var line = $(document.createElement("tr"));
                            line.attr("class", "local");

                            var checkboxTd = $('<td><input checked="checked" type="checkbox" /></td>');
                            // set value separately since file path may contain reserved characters
                            checkboxTd.children().first().val(filePath);
                            line.append(checkboxTd);

                            var filenameTd = $('<td><a></a></td>');
                            // same as above
                            if (filePath.startsWith("file:///")) { // Linux-like file path
                                filenameTd.children().first().text(filePath.substr(filePath.lastIndexOf("/") + 1));
                            } else {
                                filenameTd.children().first().text(filePath.substr(filePath.lastIndexOf("\\") + 1));
                            }
                            line.append(filenameTd);

                            line.append("<td>-</td>");
                            line.append("<td>-</td>");
                            // remove the last element from the list - I don't care which
                            // one was removed, as long as I keep the count of files
                            // accurate
                            line.append(
                                    "<td><button title='Remover ficheiro da lista' class='btn btn-light btn-sm' onclick='removeLocalFile(this);'><span class='far fa-trash-alt'></span></button></td>"
                                    );

                            tbody.append(line);
                        }
                );
            }
    ).catch(
            signingDoneMsg
            );
}