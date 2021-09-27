/* global assinareID */

var assinareIdInstance;

window.processData = function (err, data) {
    if (err) {
        console.error(err);
        alert(JSON.stringify(err, null, 4));
    } else {
        console.debug(data);
        alert(JSON.stringify(data, null, 4));
    }
};

window.processAddress = function (err, data) {
    if (err) {
        console.error(err);
        alert(JSON.stringify(err, null, 4));
    } else {
        console.debug(data);
        alert(JSON.stringify(data, null, 4));
    }
};

window.processPicture = function (err, data) {
    if (err) {
        console.error(err);
        alert(JSON.stringify(err, null, 4));
    } else {
        console.debug(data);
        alert("Picture size: " + data.picture.length);
        
        //code to download picture
        window.picData = data;
        var s = "data:image/jp2;base64," + data.picture;
        var anchor = document.getElementById("picture");
        anchor.href = s;
        anchor.click();
    }
};

var assinareParams = {
    assinareIdApplet: "assinareIdApplet"
};

window.documentLoaded = function () {
    assinareID.getInstance(
            assinareParams,
            function ( err, instance ) {
                if ( err ) {
                    alert( "Error loading AssinareIdApplet: " + err )
                } else {
                    assinareIdInstance = instance;
                    // here are initialized the part in the page that interact with the Applet
                    // e.g. activate buttons to obtain the data
                    alert("AssinareIdApplet started.");
                }
            }
    );
};

window.getCitizenData = function () {
    try {
        assinareIdInstance.getCitizenData("processData");
    } catch (ex) {
        console.error(ex);
        alert("An error occurred while invoking AssinareID: " + ex);
    }
};

window.getCitizenAddress = function () {
    try {
        assinareIdInstance.getCitizenAddress("processAddress");
    } catch (ex) {
        console.error(ex);
        alert("An error occurred while invoking AssinareID: " + ex);
    }
};

window.getCitizenPicture = function () {
    try {
        assinareIdInstance.getCitizenPicture("processPicture");
    } catch (ex) {
        console.error(ex);
        alert("An error occurred while invoking AssinareID: " + ex);
    }
};
