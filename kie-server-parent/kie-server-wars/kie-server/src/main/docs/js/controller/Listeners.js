var Listeners = function () {

};

Listeners.prototype.init = function () {
    this.regEvents();
    this.loadListener();
    this.primaryLoadListener();
    this.secondaryLoadListener();
};

Listeners.prototype.loadModelEvent = function (e) {
    var modelPath = document.querySelector("#modelUrl").value;
    this.loadModel(modelPath);
};

Listeners.prototype.loadPrimaryModelEvent = function (e) {
    this.loadModel(Properties.primaryModelPath);
};

Listeners.prototype.loadSecondaryModelEvent = function (e) {
    this.loadModel(Properties.secondaryModelPath);
};

Listeners.prototype.loadModel = function (modelPath) {
    try {
        ProgressBar.showProgressBar();
        window.apiModel.loadModel(modelPath);
        window.graphics.show(window.apiModel.modelJSON);
        window.graphics.closeMethodElement();
        window.graphics.createAnchorsToTypes();
    } catch (e) {
        if (e instanceof CaughtException) {
            Logger.error(e.getMsg());
        } else {
            Logger.error("Unexpected error during retrieving model");
        }
    } finally {
        ProgressBar.hideProgressBar();
    }
};

Listeners.prototype.loadListener = function () {
    var button = document.querySelector("#loadModel");
    button.addEventListener("click", this.loadModelEvent.bind(this));
};

Listeners.prototype.primaryLoadListener = function () {
    var button = document.querySelector("#primaryLoad");
    button.addEventListener("click", this.loadPrimaryModelEvent.bind(this));
};

Listeners.prototype.secondaryLoadListener = function () {
    var button = document.querySelector("#secondaryLoad");
    button.addEventListener("click", this.loadSecondaryModelEvent.bind(this));
};

Listeners.prototype.regEvents = function () {

    $('.header').click(function () {
        var parent = $(this).parent();

        if (parent.hasClass('closed')) {
            parent.children(".children").slideToggle("slow");
            parent.children(".children").promise().done(
                function (onFired) {
                    parent.removeClass('closed');
                    parent.addClass('open');
                }
            );
        } else {
            parent.children(".children").slideToggle("slow");
            parent.children(".children").promise().done(
                function (onFired) {
                    parent.removeClass('open');
                    parent.addClass('closed');
                }
            );
        }
    });
};
