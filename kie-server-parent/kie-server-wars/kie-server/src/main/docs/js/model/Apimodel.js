var ApiModel = function () {
    this.modelJSON;
};

/**
 * Stores the model in JSON format in modelJSON property
 * @param modelUrl URL where model can be retrieved
 */
ApiModel.prototype.loadModel = function (modelUrl) {
    var stateChanged = function (caller) {
        if (this.readyState === 4) {
            if (this.status == 200) {
                //                Logger.success("Retrieving model finished with http status: OK " + this.status);
            } else {
                //                Logger.error("Retrieving model finished with http status: " + this.status);
                throw new CaughtException("Retrieving model finished with http status: " + this.status);
            }
            var modelJSON = caller.checkAndGetModel(this.responseText);
            caller.setModelJSON(modelJSON);
        }
    };

    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open('GET', modelUrl, false);
    var oldThis = this;
    var callback = stateChanged.bind(xmlhttp);
    xmlhttp.addEventListener('readystatechange', function () {
        callback(oldThis)
    });
    try {
        xmlhttp.send();
    } catch (e) {
        throw new CaughtException(e + "\nwith model URI " + modelUrl);
    }
    if (xmlhttp.status != 200) {
        throw new CaughtException("Retrieving model finished with http status: " + xmlhttp.status + "\nwith model URI " + modelUrl);
    }
};

/**
 * Checks model in string format and retrieves model in JSON. When problem during checking, exception is thrown.
 * @param givenModel model in string format
 * @returns {*} model in JSON format
 */
ApiModel.prototype.checkAndGetModel = function (givenModel) {
    try {
        givenModel = givenModel.replace(/[<]/g, "&lt;").replace(/[>]/g, "&gt;");
        var modelJSON = JSON.parse(givenModel);
        this.replacePropertyNames(modelJSON);
        return modelJSON;
    } catch (e) {
        Logger.error("Given model could not be parsed as JSON");
        throw new CaughtException("Given model could not be parsed as JSON");
    }
};

ApiModel.prototype.replacePropertyNames = function (modelJSON) {
    for (var key in modelJSON) {
        var value = modelJSON[key];
        if (typeof value !== "string") {
            this.replacePropertyNames(modelJSON[key]);
        }
        var newKey = Properties.keys[key];
        delete modelJSON[key];
        if (newKey != null) {
            modelJSON[newKey] = value;
        } else {
            modelJSON[key] = value;
        }
    }
};

ApiModel.prototype.setModelJSON = function (modelJSON) {
    this.modelJSON = modelJSON;
};
