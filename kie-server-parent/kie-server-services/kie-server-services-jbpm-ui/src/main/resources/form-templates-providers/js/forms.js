appformer = {
    forms: {
        Documents: function Documents(autoUpload) {
            this.autoUpload = autoUpload;
        },
        Document: function Document(id, name, url, size, lastModified) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.size = size;
            this.lastModified = lastModified;
        }
    }
};

appformer.forms.Documents.get = function() {
    return new appformer.forms.Documents(false);
};

appformer.forms.Documents.get = function(autoUpload) {
    if (autoUpload == true) {
        return new appformer.forms.Documents(true);
    }

    return new appformer.forms.Documents(false);
};

appformer.forms.Documents.prototype.preventEvents = function (event) {
    event.preventDefault();
    event.stopPropagation();
};

appformer.forms.Documents.prototype.dropFiles = function (event) {
    this.preventEvents(event);

    if (event.dataTransfer.items) {
        for (var i = 0; i < event.dataTransfer.items.length; i++) {
            if (event.dataTransfer.items[i].kind === 'file') {
                var item = event.dataTransfer.items[i];
                var isFile = true;
                if (typeof (item.webkitGetAsEntry) == "function") {
                    isFile = item.webkitGetAsEntry().isFile;
                } else if (typeof (item.getAsEntry) == "function") {
                    isFile =  item.getAsEntry().isFile;
                }

                if (isFile) {
                    this.dropFile(item.getAsFile());
                }
            }
        }
    } else {
        this.dropFilesList(event.dataTransfer.files);
    }
};

appformer.forms.Documents.prototype.dropFilesList = function(fileList) {
    for (var i = 0; i < fileList.length; i++) {
        this.dropFile(fileList[i]);
    }
};

appformer.forms.Documents.prototype.dropFile = function (file) {
    var id = Number(Math.random().toString().slice(2,11)).toString();

    var document = new appformer.forms.Document(id, file.name, '', file.size, file.lastModified);

    if (this.autoUpload) {
        var callback =  this.onDropCallback;
        var fileReader = new FileReader();
        fileReader.onload = function (event) {
            if (event.target.readyState == FileReader.DONE) {
                var b64 = event.target.result.replace(/^data:.+;base64,/, '');
                document.content = b64;
                document.size = document.content.size;
                callback(document);
            }
        };
        fileReader.readAsDataURL(file);
    } else {
        this.onDropCallback(document, file);
    }
};

appformer.forms.Documents.prototype.bind = function(element) {
    if (!element) {
        throw "Cannot bind documents upload to a null element";
    }
    if (!element.tagName) {
        throw "Cannot bind documents upload to a non html element";
    }

    var tag = element.tagName.toUpperCase();

    if (tag === "DIV") {
        this.divElement = element;
        ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave'].forEach(eventName => this.divElement.addEventListener(eventName, event => this.preventEvents(event)));
        this.divElement.addEventListener('drop', event => this.dropFiles(event));
        return this;
    } else if (tag === "INPUT" && element.type.toUpperCase() == "FILE") {
        this.inputElement = element;
        this.inputElement.addEventListener('change', event => this.dropFilesList(event.target.files));
        return this;
    }

    throw "Cannot bind documents to " + element.tagName + " elements";
};

appformer.forms.Documents.prototype.onDrop = function(callback) {
    this.onDropCallback = callback;
    return this;
};

$("div[data-field=dropRegion]").on('dragover', function(e) {
    var dt = e.originalEvent.dataTransfer;
    if (dt.types && (dt.types.indexOf ? dt.types.indexOf('Files') != -1 : dt.types.contains('Files'))) {
      $(this).find("[data-field=dropHere]").show();
      $(this).find("[data-field=dragHere]").hide();
    }
});
$("div[data-field=dropRegion]").on('dragleave', function(e) {
   dragTimer = window.setTimeout(function() {
       $(this).find("[data-field=dropHere]").hide();
       $(this).find("[data-field=dragHere]").show();
   }, 25);
});

var template = 
	'<div class="document"> ' +
	'	<span data-field="state"></span> ' +
	'	<span class="kie-wb-common-forms-docs-upload-content vertical-">' + 
	'	        <a data-field="document" target="_blank"></a>' +
	'	    </span>' + 
	'	<span data-field="actions" class="kie-wb-common-forms-docs-upload-content"><span aria-hidden="true" class="close">×</span></span>' +
	'</div>';

$("div[data-field=documentCollection]").each(function(index, element){
	var documentCollections = appformer.forms.Documents.get(true);
	var dropRegionElement = $(element).find("div[data-field=dropRegion]")[0];
	documentCollections.bind(dropRegionElement);
	documentCollections.onDrop(function(document, file) {
		var newElement = $(template);
		var anchor = $(newElement).find("a[data-field=document]");
		$(anchor).data('document', document);
		anchor.text(document.name);
		$(element).find('div[data-field="dragHere"]').remove();
		$(element).prepend(newElement);
	});
});

$(document).on('click', '.kie-wb-common-forms-docs-upload-content > .close', function() {
	$(this).parents('.document').remove();
});

$('input[data-slider-id]').slider({});


