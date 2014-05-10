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

$(".modal").modal('hide');
document.getElementById("schedule-activity").onclick = function() {
	$("#schedule-activity-event").modal('show');
};
document.getElementById("send-proposal").onclick = function() {
	$("#send-proposal-event").modal('show');
};

// Send Proposal(titular)

function send_proposal_with_message(snd, prop, msg) {
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
	var proposal = generate_schedule_proposal(c);
	var titular = "titular";
	var message = $("#message").val();
	send_proposal_with_message(titular, proposal, message);
};

// Get proposal(program manager)

