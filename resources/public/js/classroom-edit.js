// Fetching data

function fetch_data(url) {
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

function get_buildings() {
	var url = '/api/room/building/list';
	return fetch_data(url);
}

function get_rooms(building) {
	var url = '/api/room/list/' + building;
	return fetch_data(url);
}

// Parsing data

function buildings_list() {
	var json_data = get_buildings();
	return $.map(json_data, function(value) {
		var option = document.createElement("option");
		option.setAttribute("building", value);
		option.innerHTML = value;
		return option;
	});
}

function rooms_list(building) {
	var json_data = get_rooms(building);
	return json_data.rooms.map(function(room) {
		var option = document.createElement("option");
		option.setAttribute("number", room.id.number);
		option.setAttribute("floor", room.id.floor);
		option.setAttribute("building", building);
		option.innerHTML = "number: " + room.id.number + " floor: " + room.id.floor;
		return option;
	});
}

// Manipulating data

function populate_select(select_element, options, callback_function) {
	select_element.options.length = 0;
	options.forEach(function(opt) {
		select_element.appendChild(opt);
	});
	select_element.onchange = callback_function;
	$(select_element).select2("open");
}

// Callback functions:

function buildings_callback() {
	$("#edit_room").attr("disabled", "disabled");
	$("#delete_room").attr("disabled", "disabled");
	$("#edit_room_description").attr("disabled", "disabled");
	var room_id = document.getElementById("rooms_select");
	var selected_building = $("#buildings_select option:selected").attr('building');
	$("#add_room").removeAttr("disabled");
	// add button is now enabled.
	populate_select(room_id, rooms_list(selected_building), rooms_callback);
}

function show_room_description(building, floor, number) {
	var json_data = get_rooms(building);
	var capacity;
	var facalities;
	$("#room_description").empty();
	json_data.rooms.forEach(function(room) {
		if (room.id.number == number && room.id.floor == floor) {
			capacity = room.capacity;
			facilities = room.facilities;
		}
	});
	$("#facilities_description #beamer").prop('checked', false);
	$("#facilities_description #overhead-projector").prop('checked', false);
	facilities.forEach(function(f) {
		if (f == "beamer") {
			$("#facilities_description #beamer").prop('checked', true);
		}
		if (f == "overhead-projector") {
			$("#facilities_description #overhead-projector").prop('checked', true);
		}
	});
	$("#room_description").append("<strong>Capacity:</strong> " + capacity + " <br />");
	$("#edit_room_description").removeAttr("disabled");
	// edit button is now enabled.
}

function rooms_callback() {
	var number = $("#rooms_select option:selected").attr('number');
	var floor = $("#rooms_select option:selected").attr('floor');
	var building = $("#rooms_select option:selected").attr('building');
	$("#edit_room").removeAttr("disabled");
	$("#delete_room").removeAttr("disabled");
	show_room_description(building, floor, number);
}

function add_new_room() {
	var nr = +$("#add_room_event #room_number").val();
	var fl = +$("#add_room_event #room_floor").val();
	var bl = $("#buildings_select option:selected").attr('building');
	var cp = +$("#add_room_event #room_capacity").val();
	var fc = $("#add_room_event .room_facilities_check:checked").map(function() {
		return this.value;
	}).get();
	var r_id = {
		building : bl,
		floor : fl,
		number : nr
	};
	var room = {
		id : r_id,
		capacity : cp,
		facilities : fc
	};
	$.ajax({
		type : 'POST',
		url : '/api/room/',
		contentType : "application/json",
		data : JSON.stringify(room),
		async : false,
		processData : false,
		dataType : 'JSON'
	});
	$("#add_room_event").modal('hide');
}

function edit_room_description() {
	var nr = +$("#rooms_select option:selected").attr('number');
	var fl = +$("#rooms_select option:selected").attr('floor');
	var bl = $("#rooms_select option:selected").attr('building');
	var cp = +$("#edit_room_description_event #room_capacity").val();
	var fc = $("#edit_room_description_event .room_facilities_check:checked").map(function() {
		return this.value;
	}).get();

	var r_id = {
		building : bl,
		floor : fl,
		number : nr
	};
	var room = {
		id : r_id,
		capacity : cp,
		facilities : fc
	};
	$.ajax({
		type : 'PUT',
		url : '/api/room/',
		contentType : "application/json",
		data : JSON.stringify(room),
		async : false,
		processData : false,
		dataType : 'JSON'
	});
	$("#edit_room_description_event").modal('hide');
}

function delete_room() {
	var nr = +$("#rooms_select option:selected").attr('number');
	var fl = +$("#rooms_select option:selected").attr('floor');
	var bl = $("#rooms_select option:selected").attr('building');
	var r_id = {
		building : bl,
		floor : fl,
		number : nr
	};
	$.ajax({
		type : 'DELETE',
		url : '/api/room/',
		contentType : "application/json",
		data : JSON.stringify(r_id),
		async : false,
		processData : false,
		dataType : 'JSON'
	});
	$("#delete_room_event").modal('hide');
}

// Function calls:

$(document).ready(function() {
	// Initialize layout:
	$(".modal").modal('hide');
	$("#buildings_select").select2({
		placeholder : "Select a building",
		width : "200"
	});
	$("#rooms_select").select2({
		placeholder : "Select a classroom",
		width : "200"
	});
	// Populate select with buildings:
	populate_select(document.getElementById("buildings_select"), buildings_list(), buildings_callback);
	// Add new room to selected building:
	$("#add_room").click(function() {
		$("#add_room_event").modal('show');
	});
	$("#add_room_btn").click(function() {
		add_new_room();
	});
	// Edit description of a selecetd room:
	$("#edit_room_description").click(function() {
		$("#edit_room_description_event").modal('show');
	});
	$("#edit_room_description_btn").click(function() {
		edit_room_description();
	});
	// Delete a selecetd room:
	$("#delete_room").click(function() {
		$("#delete_room_event").modal('show');
	});
	$("#delete_room_btn").click(function() {
		delete_room();
	});
});
