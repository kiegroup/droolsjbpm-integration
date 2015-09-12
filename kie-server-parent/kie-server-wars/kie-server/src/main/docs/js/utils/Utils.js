function Utils(object) {

};

Utils.isEmpty = function (object) {
    if (object == undefined) {
        return true;
    }
    if (object == null) {
        return true;
    }
    if (object == "") {
        return true;
    }
    if (!(object instanceof HTMLElement) && Object.keys(object).length == 0) {
        return true;
    }
    return false;
};

Utils.isNotEmpty = function (object) {
    return !Utils.isEmpty(object);
};

Utils.subs = function (object) {
    var fromProperties = Properties[object];
    if (Utils.isEmpty(fromProperties)) {
        return object;
    } else {
        return fromProperties;
    }
};

String.prototype.trimSlash = function () {
    var string = this.trim();
    if (string.charAt(0) == "/") {
        string = string.substring(1);
    }
    if (string.charAt(string.length - 1) == "/") {
        string = string.substring(0, string.length - 1);
    }
    return string;
};
