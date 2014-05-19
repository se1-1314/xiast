// Fetching data

function get_programs() {
	var url = '/api/program/list';
	var result = null;
	$.ajax({
		type : "GET",
		url : url,
		dataType : "JSON",
		async : false,
		success : function(data) {
			result = data;
		}
	});
	return result;
}

// Parsing data

function program_list() {
	var json_data = get_programs();
	return $.map(json_data.programs, function(program) {
		var option = document.createElement("option");
		option.setAttribute("program_id", program.id);
		option.setAttribute("title", program.title);
		option.innerHTML = program.title;
		return option;
	});
}

// Manipulating data

function populate_program_select(select_element, options, callback_function) {
	options.forEach(function(opt) {
		select_element.appendChild(opt);
	});
	select_element.onchange = callback_function;
}

// Callback functions:
// PROGRAM SCHEDULES
// Querying schedules of dedicated programs (all types of users can do this)
//------------------------------------------------------------------------------
// Gets schedule-blocks for a dedicated program
// for all weeks, all days and all slots
function get_program_schedule_blocks(program_id) {
	var url = "/api/schedule/program/" + program_id + "/1/52/1/7/1/24";
	var schedule_blocks;
	$.ajax({
		type : "GET",
		url : url,
		dataType : 'JSON',
		success : function(data) {
			schedule_blocks = data.schedule;
		},
		async : false
	});
	return schedule_blocks;
}

function program_callback() {
	new_calendar = $("#schedule-content");
	new_calendar.empty();
	var selected_program_id = $("#programs_select option:selected").attr('program_id');
	new_calendar.fullCalendar({
		aspectRatio : 1.6,
		defaultView : 'agendaWeek',
		header : {
			left : 'prev,next today',
			center : 'title',
			right : 'agendaMonth,agendaWeek,agendaDay'
		},
		editable : false,
		events : get_program_schedule_blocks(selected_program_id).map(schedule_block_to_event),
		eventDrop : event_dropped,
		eventClick : calendar_event_click_event,
		allDaySlot : false,
		snapMinutes : 30,
		firstHour : 8,
		minTime : 7,
		weekends : true,
		hiddenDays : [0],
		eventDurationEditable : false
	});
}

// Function calls:

$(document).ready(function() {
	$("#programs_select").select2({
		placeholder : "Select a program",
		width : "200"
	});
	populate_program_select(document.getElementById("programs_select"), program_list(), program_callback);
});
