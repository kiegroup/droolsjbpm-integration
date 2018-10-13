$(document).ready(function () {
   	
	initializeForm();
	console.log('form initialized');
});
$.ajaxSetup({
    beforeSend: function(request) {
        request.setRequestHeader("Content-Type","application/json");
        request.setRequestHeader("Accept","application/json");
    }
});

function showSuccess(text) {
	var successAlert = $('#successAlert');
	successAlert.text(text);
	successAlert.removeClass('hidden');
}

function showFailure(text) {
	var failureAlert = $('#failureAlert');
	failureAlert.html(text);
	failureAlert.removeClass('hidden');
}

function clearNotifications() {
	var successAlert = $('#successAlert');
	successAlert.text('');
	successAlert.addClass('hidden');
	var failureAlert = $('#failureAlert');
	failureAlert.text('');
	failureAlert.addClass('hidden');
}


function startProcess(button) {
	if (validate()) {
		button.disabled = true;
		
		console.log('Process started with data ' + JSON.stringify(getData()));
		
		$.ajax({
		    method: 'POST',
		    url: getProcessEndpoint(),
		    data: JSON.stringify(getData())
		}).done(function( msg ) {
			console.log('Process started with data ' + JSON.stringify(getData()) +  ' process instance id ' + msg );
		    showSuccess('Process successfully started with instance id ' + msg);

		    // if we are inline
		    if(typeof afterProcessStarted === "function") {
            	afterProcessStarted();
			}
			// if we are embedded
		    if(typeof parent.afterProcessStarted === "function") {
                parent.afterProcessStarted();
            }
		}).fail(function( xhr, msg, error ) {
			showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
			button.disabled = false;
		});
	}
}

function startCase(button) {
	if (validate()) {
		button.disabled = true;
			
		$.ajax({
		    method: 'POST',
		    url: getCaseEndpoint(),
		    data: JSON.stringify(getData())
		}).done(function( msg ) {
			console.log('Case started with data ' + JSON.stringify(getData()) +  ' case id ' + msg );
			showSuccess('Case successfully started with case id ' + msg);

            // if we are inline
            if(typeof afterCaseStarted === "function") {
                afterCaseStarted();
            }
            // if we are embedded
            if(typeof parent.afterCaseStarted === "function") {
                parent.afterCaseStarted();
            }
		}).fail(function( xhr, msg, error ) {
			showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
			button.disabled = false;
		});
	}
}

/*
 * Task related functions
 */
var taskStatus = '';


function initTaskButtons() {
	
	var claimButton = $('#claimButton');
	var releaseButton = $('#releaseButton');
	var startButton = $('#startButton');
	var stopButton = $('#stopButton');
	var saveButton = $('#saveButton');
	var completeButton = $('#completeButton');
	
	
	switch (taskStatus) {
	    case 'Ready':
	    	claimButton.show();
	    	releaseButton.hide();
	    	startButton.hide();
	    	stopButton.hide();
	    	saveButton.hide();
	    	completeButton.show();
	        break;
	    case 'Reserved':
	    	claimButton.hide();
	    	releaseButton.show();
	    	startButton.show();
	    	stopButton.hide();
	    	saveButton.hide();
	    	completeButton.show();
	        break;
    	case 'InProgress':
    		claimButton.hide();
	    	releaseButton.show();
	    	startButton.hide();
	    	stopButton.show();
	    	saveButton.show();
	    	completeButton.show();
	        break;
	    default:
	    	claimButton.hide();
	    	releaseButton.hide();
	    	startButton.hide();
	    	stopButton.hide();
	    	saveButton.hide();
	    	completeButton.hide();
	        
	}
}

function claimTask() {
	$.ajax({
	    method: 'PUT',
	    dataType: 'text',
	    url: getTaskEndpoint() + '/states/claimed'	    
	}).done(function( msg ) {
		console.log('Task claimed');
	
		taskStatus = 'Reserved';
		initTaskButtons();
		showSuccess('Task claimed successfuly');
		
		// if we are inline
	    if(typeof afterTaskClaimed === "function") {
	    	afterTaskClaimed();
		}
		// if we are embedded
	    if(typeof parent.afterTaskClaimed === "function") {
            parent.afterTaskClaimed();
        }
	    
	}).fail(function( xhr, msg, error ) {
		showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
	});
}

function releaseTask() {
	$.ajax({
	    method: 'PUT',
	    dataType: 'text',
	    url: getTaskEndpoint() + '/states/released'	    
	}).done(function( msg ) {
		console.log('Task released');
	
		taskStatus = 'Ready';
		initTaskButtons();
		showSuccess('Task released successfuly');
		
		// if we are inline
	    if(typeof afterTaskReleased === "function") {
	    	afterTaskReleased();
		}
		// if we are embedded
	    if(typeof parent.afterTaskReleased === "function") {
            parent.afterTaskReleased();
        }
	    
	}).fail(function( xhr, msg, error ) {
		showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
	});
}

function startTask() {
	$.ajax({
	    method: 'PUT',
	    dataType: 'text',
	    url: getTaskEndpoint() + '/states/started'	    
	}).done(function( msg ) {
		console.log('Task started');
	
		taskStatus = 'InProgress';
		initTaskButtons();
		showSuccess('Task started successfuly');

        // if we are inline
        if(typeof afterTaskStarted === "function") {
            afterTaskStarted();
        }
        // if we are embedded
        if(typeof parent.afterTaskStarted === "function") {
            parent.afterTaskStarted();
        }
	}).fail(function( xhr, msg, error ) {
		showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
	});
}

function stopTask() {
	$.ajax({
	    method: 'PUT',
	    dataType: 'text',
	    url: getTaskEndpoint() + '/states/stopped'	    
	}).done(function( msg ) {
		console.log('Task stopped');
	
		taskStatus = 'Reserved';
		initTaskButtons();
		showSuccess('Task stopped successfuly');

        // if we are inline
        if(typeof afterTaskStopped === "function") {
            afterTaskStopped();
        }
        // if we are embedded
        if(typeof parent.afterTaskStopped === "function") {
            parent.afterTaskStopped();
        }
	}).fail(function( xhr, msg, error ) {
		showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
	});
}

function saveTask() {
	$.ajax({
	    method: 'PUT',
	    dataType: 'text',
	    url: getTaskEndpoint() + '/contents/output',
	    data: JSON.stringify(getData())
	}).done(function( msg ) {
		console.log('Saved data ' + JSON.stringify(getData()));	
		showSuccess('Task data saved successfuly');

        // if we are inline
        if(typeof afterTaskSaved === "function") {
            afterTaskSaved();
        }
        // if we are embedded
        if(typeof parent.afterTaskSaved === "function") {
            parent.afterTaskSaved();
        }
	}).fail(function( xhr, msg, error ) {
		showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
	});	
}

function completeTask() {
	if (validate()) {
		$.ajax({
		    method: 'PUT',
		    dataType: 'text',
		    url: getTaskEndpoint() + '/states/completed?auto-progress=true',
		    data: JSON.stringify(getData())
		}).done(function( msg ) {
			console.log('Task completed with data ' + JSON.stringify(getData()));
		
			taskStatus = 'Completed';
			initTaskButtons();
			showSuccess('Task completed successfuly');

            // if we are inline
            if(typeof afterTaskCompleted === "function") {
                afterTaskCompleted();
            }
            // if we are embedded
            if(typeof parent.afterTaskCompleted === "function") {
                parent.afterTaskCompleted();
            }
		}).fail(function( xhr, msg, error ) {
			showFailure('Something went wrong ' + error + ' ( ' + xhr.responseText + ')');
		});
	}
}

function validate() {
	clearNotifications();
	var messages = '';
	$('input, select, textarea').each(
		    function(index){  
		        var input = $(this);
		        if (input.attr('required') != null && !input.val()) {
		        	messages += 'Field <b>' + $("label[for='"+input.attr('id')+"']").text() + '</b> is mandatory <br/>';		        	
		    	}
		        
		        if (input.attr('pattern') != null && input.attr('pattern') != '') {
		        	var value = input.val();

		            var expected = new RegExp(input.attr('pattern'));

		            if (!expected.test(value)) {
		            	messages += 'Field <b>' + $("label[for='"+input.attr('id')+"']").text() + '</b> has unexpected value <br/>';
		            }
		        }
		    }
		);
	if (messages != '') {
		showFailure(messages);
		console.log('Validation failed with message ' + messages);
		return false;
	}
	console.log('Validation successfull');
	return true;
}

var fileData = new Map();


function encodeImageFileAsURL(input) {

	var filesSelected = input.files;
	if (filesSelected.length > 0) {
		var fileToLoad = filesSelected[0];
		
		var fileInfo = {
				'name' : fileToLoad.name,
				'size' : fileToLoad.size,
				'data' : null
		};
		fileData.set(input.id, fileInfo);
		
		var fileReader = new FileReader();

		fileReader.onload = function(fileLoadedEvent) {
			var local = fileLoadedEvent.target.result; // <--- data: base64
			var srcData = local.replace(/^data:.*\/.*;base64,/, "");

			fileData.get(input.id).data = srcData;
			
			console.log("Converted Base64 version is " + srcData);
		}
		fileReader.readAsDataURL(fileToLoad);
	} else {
		alert("Please select a file");
	}
}

function getDocumentData(inputId) {

	if (fileData.has(inputId)) {
		var fileInfo = fileData.get(inputId);
		var document = {
			'DocumentImpl' : {
				'lastModified' : new Date(),
				'name' : fileInfo.name,
				'size' : fileInfo.size,
				'content' : fileInfo.data,
				'attributes' : {
					'_UPDATED_' : 'true'
				}
			}
		};
		return document;
	}
	return null;

}

function getDateFormated(id) {
	var d = new Date(document.getElementById(id).value);
	
	
	var wrappedDate = {
			'Date' : d.getTime()						
		};
	return wrappedDate;
}