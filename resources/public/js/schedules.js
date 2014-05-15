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
	$(select_element).select2("open");
}

// Callback functions:

function program_callback() {
	var selected_program_id = $("#programs_select option:selected").attr('program_id');
	alert(selected_program_id);
	get_program_schedule_blocks(selected_program_id); // TODO: inladen in calender.
}

// Function calls:

$(document).ready(function() {
	$("select").select2({
		width : "200"
	});
	populate_program_select(document.getElementById("programs_select"), program_list(), program_callback);
	$("#show_selected_program").click(function() {
	});
});
