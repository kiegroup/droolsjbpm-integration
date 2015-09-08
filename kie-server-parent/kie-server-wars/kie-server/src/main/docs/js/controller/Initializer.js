var Initializer = function () {

};

Initializer.prototype.initialize = function () {
    try {
        ProgressBar.showProgressBar();
        window.apiModel = new ApiModel();
        window.listener = new Listeners();
        window.listener.init();
        window.graphics = new Graphics();
        window.graphics.init();
        window.apiModel.loadModel(Properties.primaryModelPath);
        window.graphics.show(window.apiModel.modelJSON);
        window.graphics.closeMethodElement();
        window.graphics.createAnchorsToTypes();
    } catch (e) {
        if (e instanceof CaughtException) {
            Logger.error(e.getMsg());
        } else {
            Logger.error("Unexpected error occured :-(\n" + e);
        }
    } finally {
        ProgressBar.hideProgressBar();
    }
};
