// Fetching data

function write_data(url, data) {
	$.ajax({
		type : "POST",
		url : url,
		contentType : "application/json",
		data : data,
		async : false,
		processData : false,
		dataType : "JSON"
	});
}

// Get VUB week from current date

function update_current_vub_week() {
	var d = new Date();
	var date_array = date_to_VUB_time(d);
	$("#start-week").attr("placeholder", date_array[0]);
}

// Function calls

update_current_vub_week();

$("#schedule-activity-event").modal('hide');
$("#schedule-activity").click(function() {
	$("#schedule-activity-event").modal('show');
});
$("#send-proposal-event").modal('hide');
$("#send-proposal").click(function() {
	$("#send-proposal-event").modal('show');
});

// Send Proposal(titular)

function send_proposal(snd, prop, msg) {
	var url = "/api/schedule/message";
	var json_data = {
		sender : snd,
		proposal : prop,
		message : msg
	};
	var data = JSON.stringify(json_data);
	write_data(url, data);
	$("#send-proposal-event").modal('hide');
}


document.getElementById("send").onclick = function() {
	console.log("ajax twice");
	var proposal = {
		"new" : [],
		"moved" : [],
		"deleted" : []
	};
	var titular = "titular";
	var message = $("#message").val();
	send_proposal(titular, proposal, message);
};

// Get proposal(program manager)

