function get_requests() {
	var url = "/api/schedule/message/list";
	var json_data;
	$.ajax({
		type : "GET",
		url : url,
		dataType : 'JSON',
		success : function(data) {
			json_data = data;
		},
		async : false
	});
	return json_data;
	//e.g. [{"sender":"titular","id":1},{"sender":"titular","id":2},{"sender":"titular","id":3},{"sender":"titular","id":4}]
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
		var sender = r.sender;
		var row = $('<tr class="request_event" request_id=' + r.id + '><td>Request from: ' + sender + '</td></tr>');
		requests_list.append(row);
	});
}

function get_request_information(ID) {
	var url = "/api/schedule/message/" + ID;
	var json_data;
	$.ajax({
		type : "GET",
		url : url,
		dataType : 'JSON',
		success : function(data) {
			json_data = data;
		},
		async : false
	});
	//{"status":"inprogress","proposal":{"new":[],"moved":[{"room":{"number":412,"floor":4,"building":"F"},
	//"item":{"course-id":"1004483BNR","course-activity":1687},"last-slot":10,"first-slot":7,"day":2,"week":35,"id":306}],
	//"deleted":[]},"message":"Message without sender","sender":"titular","id":1}
	return json_data;
}

function show_request_description() {
	$(".request_event").click(function() {
		var id = $(this).attr('request_id');
		$("#request_description").empty();
		var json_data = get_request_information(id);
		var message = json_data.message;
		var proposal = json_data.proposal;
		$("#request_description").append("<table><tr><th>Request description:</th></tr><tr><td>" + message + "</td></tr></table> <br />");
		load_request_proposal(proposal);
	});
}

$(document).ready(function() {
    $("#request_description").show();

    // show requests list
    load_requests_list(get_requests());
	show_request_description();
    $("#apply_button").click(function() {
        send_apply_request(function(check_results){
            
            load_schedule_check_results(check_results);
        });
    });
});
