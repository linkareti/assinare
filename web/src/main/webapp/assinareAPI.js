/////////////////////////////////////////////////////////////////////////
//
//Please do not modify this file. This is a part of Assinare and any 
//change might cause it not to work.
//
//If for some reason you need to change it, please contact LINKARE.
//
//Thank you.
//Linkare
/////////////////////////////////////////////////////////////////////////


const expectedPrefix = "Assinare Daemon/",
        expectedVersionRegex = /^2\.[0-7]\.\d{1,2}(?:-SNAPSHOT)?$/; // 2.[0-7].x-SNAPSHOT

function checkFetchErrors(response) {
    var serverHeader = response.headers.get("Server");
    if (serverHeader && serverHeader.startsWith(expectedPrefix)) {
        var serverVersion = serverHeader.substring(expectedPrefix.length);
        if (!expectedVersionRegex.test(serverVersion)) {
            throw new Error("Versão incompatível do Assinare Daemon detectada");
        } else {
            if (!response.ok) {
                if (response.status === 500) {
                    return response.json().then(
                            errorData => {
                                console.log(errorData);
                                var err = new Error(errorData.message);
                                Object.assign(err, errorData);
                                throw err;
                            }
                    );
                } else if (response.statusText) {
                    throw new Error(`Erro HTTP: ${response.status} - ${response.statusText}`);
                } else {
                    throw new Error(`Erro HTTP: ${response.status}`);
                }
            } else {
                return response;
            }
        }
    } else {
        throw new Error("Assinare Daemon não detectado");
    }
}

function safeFetch(input, init) {
    return fetch(input, init).catch(
            error => {
                throw new Error("Falha de comunicação com o Assinare Daemon");
            }
    ).then(
            checkFetchErrors
            );
}

function safeFetchJson(input, init) {
    return safeFetch(input, init).then(
            (response) => response.json()
    );
}

class AssinareProxy {
    constructor(params) {
        this.host = "localhost";
        this.port = 20666;
        this.urlBase = `https://${this.host}:${this.port}`;
    }

    _genericPOST(urlPath, data, callbackParam) {
        var callback;
        if (callbackParam) {
            if (typeof callbackParam === "function") {
                callback = callbackParam;
            } else {
                callback = window[callbackParam];
                if (typeof callback !== "function") {
                    throw new Error(`Function "${callbackParam.toString()}" not found on Window`);
                }
            }
        }

        var fetchPromise = safeFetchJson(this.urlBase + urlPath, {
            method: "POST",
            body: JSON.stringify(data),
            headers: {
                "content-type": "application/json"
            }
        });

        if (!callbackParam) {
            return fetchPromise;
        } else {
            fetchPromise.then(
                    result => {
                        console.log(result);
                        callback(false, result);
                    }
            ).catch(
                    error => {
                        console.error(error);
                        callback(error, null);
                    }
            );
        }
    }

    getDaemonInfo() {
        return safeFetchJson(this.urlBase + "/info");
    }

    checkDaemonInfo() {
        return this.getDaemonInfo().then(
                info => {
                    // TODO: check info
                    return true;
                }
        );
    }
}

class AssinareIdProxy extends AssinareProxy {
    constructor(params) {
        super(params);
    }

    getCitizenData(proccessFunctionName) {
        return this._genericPOST("/id/data", {a: 1}, proccessFunctionName);
    }

    getCitizenAddress(proccessFunctionName) {
        return this._genericPOST("/id/address", {a: 1}, proccessFunctionName);
    }

    getCitizenPicture(proccessFunctionName) {
        return this._genericPOST("/id/picture", {a: 1}, proccessFunctionName);
    }

    shutdown(proccessFunctionName) {
        return this._genericPOST("/shutdown", {a: 1}, proccessFunctionName);
    }
}

class AssinareSignProxy extends AssinareProxy {
    constructor(params) {
        super(params);
        this.getFileUrlPrefix = params.getFileUrlPrefix;
        this.getSignedFileUrlPrefix = params.getSignedFileUrlPrefix;
        this.putFileUrl = params.putFileUrl;
        this.authCookies = params.authCookies;
        this.language = params.language;
        this.country = params.country;
    }

    signDocuments(docs, proccessFunctionName, signatureParams) {
        return this._genericPOST(
                "/sign/pdf",
                {
                    docs: this.parseDocs(docs),
                    getFileUrlPrefix: this.getFileUrlPrefix,
                    getSignedFileUrlPrefix: this.getSignedFileUrlPrefix,
                    putFileUrl: this.putFileUrl,
                    authCookies: this.authCookies,
                    language: this.language,
                    country: this.country,
                    signatureParams: signatureParams
                },
                proccessFunctionName ? proccessFunctionName : "signingDoneMsg"
                );
    }

    signContainer(docs, proccessFunctionName, signatureParams) {
        return this._genericPOST(
                "/sign/container",
                {
                    docs: this.parseDocs(docs),
                    getFileUrlPrefix: this.getFileUrlPrefix,
                    getSignedFileUrlPrefix: this.getSignedFileUrlPrefix,
                    putFileUrl: this.putFileUrl,
                    authCookies: this.authCookies,
                    language: this.language,
                    country: this.country,
                    signatureParams: signatureParams
                },
                proccessFunctionName ? proccessFunctionName : "signingDoneMsg"
                );
    }

    parseDocs(docs) {
        if (typeof docs === "string") {
            if (docs.trim().length > 0) {
                return docs.split(",");
            } else {
                return [];
            }
        } else {
            return docs;
        }
    }

    chooseLocalFiles() {
        return safeFetchJson(this.urlBase + "/sign/localFiles", {
            method: "POST",
            headers: {
                "content-type": "application/json"
            }
        });
    }
}

function registerAppletStateHandler(applet) {
    return new Promise(
            (resolve, reject) => {
        var READY = 2; // Magic number taken from Oracle's tutorial

        // register onLoad handler if applet has
        // not loaded yet
        if (applet.status < READY) {
            applet.onLoad = resolve;
        } else if (applet.status == READY) {
            // applet has already loaded 
            resolve();
        } else {
            // there was an error
            let errMsg = "Applet event handler not registered because applet status is: "
                    + applet.status;

            reject(new Error(errMsg));
        }
    }
    );
}

function getInstance(appletIdParam, ProxyClass, params, startCallback) {
    if (startCallback && typeof startCallback !== "function") {
        throw new Error("Instantiation callback is not a function");
    }

    const appletId = params[appletIdParam],
            applet = document.getElementById(appletId);
    let instancePromise;
    if (appletId && window.navigator.javaEnabled && window.navigator.javaEnabled() && applet) {
        instancePromise = registerAppletStateHandler(applet).then(
                () => applet
        );
    } else {
        instancePromise = Promise.reject(false);
    }

    instancePromise = instancePromise.catch(
            (err) => {
        if (err) {
            console.warn(err);
        }

        var proxy = new ProxyClass(params);
        if (params.lazy) {
            return proxy;
        } else {
            return proxy.checkDaemonInfo().then(
                    () => proxy
            );
        }
    }
    );

    if (!startCallback) {
        return instancePromise;
    } else {
        instancePromise.then(
                instance => {
                    startCallback(null, instance);
                }
        ).catch(
                error => {
                    startCallback(error, null);
                }
        );
    }
}

const id = {
    getInstance: getInstance.bind(null, "assinareIdApplet", AssinareIdProxy)
};

const sign = {
    getInstance: getInstance.bind(null, "assinareSignApplet", AssinareSignProxy)
};

const assinare = {id, sign};

export { id, sign, assinare as default};

if (!self.assinare) {
    self.assinare = assinare;
}
