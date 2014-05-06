function get_buildings() {
	var url = '/api/room/building/list';
	var result = null;
	$.ajax({
		type : "GET",
		url : url,
		dataType : "JSON",
		async : false,
		success : function(data) {
			result =  data;
		}
	});
	result = data;
}

function construct_available_tags() {
	var json_data = get_buildings();
	var available_tags = [];
	$.each(json_data, function(key) {
		alert(key);
		available_tags.push(key);
	});
	return available_tags;
}

$(function() {
	var availableTags = construct_available_tags();
	alert(availableTags);
	$("#keyword-building").autocomplete({
		source : availableTags
	});
});
