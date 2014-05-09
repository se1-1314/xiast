// Fetching data

// Function calls:

// Get VUB week from current date

function update_current_vub_week(){
	var d = new Date();
	var date_array = date_to_VUB_time(d);
	$("#start-week").attr("placeholder", date_array[0]);
}

update_current_vub_week();
$("#schedule-activity-event").modal('hide');
$("#schedule-activity").click(function() {
	$("#schedule-activity-event").modal('show');
});
