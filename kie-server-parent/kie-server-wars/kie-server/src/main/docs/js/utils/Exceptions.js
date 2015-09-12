var CaughtException = function (msg) {
    this.msg = msg;
};

CaughtException.prototype.getMsg = function () {
    return this.msg;
};
