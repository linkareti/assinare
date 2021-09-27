(function () {
    "use strict";

    var assinareIdInstance;

    $(function () {
        window.assinare.id.getInstance(
                {
                    assinareIdApplet: "assinareIdApplet",
                    lazy: true
                }
        ).then(
                function (instance) {
                    assinareIdInstance = instance;
                    $("[data-reenable]").prop('disabled', false);
                }
        ).catch(
                function (err) {
                    printError(
                            {
                                error: "Erro de inicializaçao do Assinare",
                                errorMessage: err
                            }
                    );
                }
        );
    });

    function processData(err, data) {
        if (err) {
            console.error(err);

            var errorMessage = {
                "error": "Não foi possível ler os dados de identificação do cartão.",
                "errorMessage": err.message
            };

            printError(errorMessage);
        } else {
            console.log(data);

            for (var key in data) {
                if (data[key]) {
                    $("#fake-cc-form #" + key).text(data[key]);
                } else {
                    $("#fake-cc-form #" + key).text("<vazio>");
                }
            }
        }
        hideBlocker();
    }

    function processAddress(err, data) {
        if (err) {
            console.error(err);

            var errorMessage = {
                "error": "Não foi possível ler os dados de identificação do cartão.",
                "errorMessage": err.message
            };

            printError(errorMessage);
        } else {
            console.log(data);

            for (var key in data) {
                if (data[key]) {
                    $("#fake-cc-form #morada\\." + key).text(data[key]);
                } else {
                    $("#fake-cc-form #morada\\." + key).text("<vazio>");
                }
            }

            var cpField = $("#fake-cc-form #morada\\.cp");
            if (data.cp3 && data.cp4) {
                cpField.text(data.cp4 + "-" + data.cp3);
            } else if (data.cp4) {
                cpField.text(data.cp4);
            } else {
                cpField.text("<vazio>");
            }
        }
        hideBlocker();
    }

    function processPicture(err, data) {
        if (err) {
            console.error(err);

            var errorMessage = {
                "error": "Não foi possível ler os dados de identificação do cartão.",
                "errorMessage": err.message
            };

            printError(errorMessage);
        } else {
            console.log(data);

            // for debugging
            window.picData = data;
            var s = "data:image/jp2;base64," + data.picture;

            var anchor = document.getElementById("picture");
            anchor.href = s;
            anchor.click();
        }
        hideBlocker();
    }

    window.getCitizenData = function () {
        var data;
        try {
            clearCardId();
            showBlocker();

            // promises used in a very basic way so we can test them with minimal changes
            assinareIdInstance.getCitizenData().then(
                    function (data) {
                        processData(false, data);
                    }
            ).catch(
                    function (err) {
                        processData(err, null);
                    });

        } catch (ex) {
            console.error(ex);
            data = {
                "error": "Ocorreu um erro ao invocar AssinareID applet.",
                "errorMessage": ex
            };

            printError(data);
            hideBlocker();
        }
    };

    window.getCitizenAddress = function () {
        var data;
        try {
            clearCardId();
            showBlocker();

            assinareIdInstance.getCitizenAddress(processAddress);

        } catch (ex) {
            console.error(ex);
            data = {
                "error": "Ocorreu um erro ao invocar AssinareID applet.",
                "errorMessage": ex
            };

            printError(data);
            hideBlocker();
        }
    };

    window.getCitizenPicture = function () {
        var data;
        try {
            clearCardId();
            showBlocker();

            assinareIdInstance.getCitizenPicture(processPicture);

        } catch (ex) {
            console.error(ex);
            data = {
                "error": "Ocorreu um erro ao invocar AssinareID applet.",
                "errorMessage": ex
            };

            printError(data);
            hideBlocker();
        }
    };

    window.onunload = function () {
        console.log("onunload");
        if (assinareIdInstance && assinareIdInstance.shutdown) {
            assinareIdInstance.shutdown();
        }
    };

    function showBlocker() {
        $("body > .blocker").removeClass("d-none");
    }

    function hideBlocker() {
        $("body > .blocker").addClass("d-none");
    }

    function printCardData(data, className) {
        console.info(data);
        var message = document.createElement("div");
        message.className = className;

        var list = document.createElement("ul");
        message.appendChild(list);

        for (var key in data) {
            var listItem = document.createElement("li");
            listItem.textContent += key + ': ' + data[key];
            list.appendChild(listItem);
        }

        var cardId = document.getElementById('cardId');
        while (cardId.firstChild) {
            cardId.removeChild(cardId.firstChild);
        }
        cardId.appendChild(message);
    }

    function clearCardId() {
        var cardId = document.getElementById('cardId');
        while (cardId.firstChild) {
            cardId.removeChild(cardId.firstChild);
        }
    }

    function printError(data) {
        var alert = document.createElement("div"),
                alertTitle = document.createElement("h4"),
                alertContent = document.createElement("p");

        alert.className = "alert alert-danger";
        alertTitle.textContent = "Ocorreu um erro";
        alertTitle.className = "alert-heading";
        alertContent.innerHTML = data.error + "<br/>Detalhes: " + data.errorMessage;

        alert.appendChild(alertTitle);
        alert.appendChild(document.createElement("hr"));
        alert.appendChild(alertContent);

        document.getElementById('cardId').appendChild(alert);
    }

}());