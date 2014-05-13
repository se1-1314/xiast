// Getters

function get_requests() {
	var json_data = [{
		sender : 'titular1',
		proposal : 'null',
		message : 'message1'
	}, {
		sender : 'titular2',
		proposal : 'null',
		message : 'message2'
	}, {
		sender : 'titular3',
		proposal : 'null',
		message : 'message3'
	}];
	return json_data;
}

// Parsing requests in a structure that can be manipulated

function requests_list() {
	var json_data = get_requests();
	return $.map(json_data, function(value) {
		var message = value.message;
		var sender = value.sender;
		var row = $("<tr><td message=" + message + " sender=" + sender + " class='request-event'> Request from: " + sender + "</td></tr>");
		return row;
	});
}

// Manipulating data

function populate_table(table_id, table_data) {
	// Delete all rows except header
	$("#" + table_id + " tbody").empty();
	// Populate table with requests
	table_data.forEach(function(row) {
		$("#" + table_id + " tbody").append(row);
	});
	//select_element.onchange = callback_function;
}

function show_request_description(description) {
	$("#request-description").empty();
	$("#request-description").append("<table><tr><th>Request description:</th></tr><tr><td>" + description + "</td></tr></table> <br />");
	$("#request-description").show();
}


$(document).ready(function() {
	$("#request-description").hide();
	// show requests list
	populate_table("requests-list", requests_list());
	$(".request-event").click(function() {
		var description = $(this).attr('message');
		show_request_description(description);
	});
});
