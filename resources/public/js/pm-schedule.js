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

function load_request_proposal(p) {
	calendar_replace_proposal(p);
}

function load_requests_list(requests) {
	// Request list table body
	var requests_list = $("#requests-list-body");
	requests_list.empty();
	// Add table rows with relevant click event callbacks
	requests.forEach(function(r) {
		var message = r.message;
		var sender = r.sender;
		var proposal = r.proposal;
		var message_display = "<table><tr><th>Request description:</th></tr><tr><td>" + message + "</td></tr></table> <br />";
		var row = $('<tr><td>Request from: ' + sender + '</td></tr>');
		row.click(function() {
			$("#request-description").empty();
			$("#request-description").append(message_display);
			load_request_proposal(proposal);
		});
		requests_list.append(row);
	});
}

$(document).ready(function() {
	// show requests list
	load_requests_list(get_requests());
});
