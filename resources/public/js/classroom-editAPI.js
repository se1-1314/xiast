function get_buildings() {
	var url = '/api/room/building/list';
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

function get_rooms(building) {
	var url = '/api/room/list/' + building;
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

function populate_select(select_element, options, callback_function) {
	options.forEach(function(opt) {
		select_element.appendChild(opt);
	});
	select_element.onchange = callback_function;
}

function buildings_callback() {
	var selected_building = $("#buildings_select option:selected").attr('building');
	populate_select(document.getElementById("rooms_select"), rooms_list(selected_building), rooms_callback);
}

function show_room_description(building,floor,number) {
	var json_data = get_rooms(building);
	var capacity;
	var facalities;
	$("#room_description").empty();
	json_data.rooms.forEach(function(room) {
		capacity = room.capacity;
	});
	$("#room_description").append("<h2>Capacity</h2>" + capacity);
}

function rooms_callback() {
	var number = $("#rooms_select option:selected").attr('number');
	var floor = $("#rooms_select option:selected").attr('floor');
	var building = $("#rooms_select option:selected").attr('building');
	show_room_description(building,floor,number);
}

populate_select(document.getElementById("buildings_select"), buildings_list(), buildings_callback);