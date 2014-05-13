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
	var room_id = document.getElementById("rooms_select");
	var selected_building = $("#buildings_select option:selected").attr('building');
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
		}
	});
	$("#room_description").append("<strong>Capacity:</strong> " + capacity);
}

function rooms_callback() {
	var number = $("#rooms_select option:selected").attr('number');
	var floor = $("#rooms_select option:selected").attr('floor');
	var building = $("#rooms_select option:selected").attr('building');
	show_room_description(building, floor, number);
}

// Function calls:

$("select").select2({width: "200"});
populate_select(document.getElementById("buildings_select"), buildings_list(), buildings_callback);
