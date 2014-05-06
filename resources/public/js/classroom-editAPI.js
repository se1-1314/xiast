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
	console.log(result);
	return result;
}

function construct_available_tags() {
	var json_data = get_buildings();
	var available_tags = [];
	$.each(json_data, function(key) {
		console.log(key);
		available_tags.push(key);
	});
	return available_tags;
}

$(function() {
	var availableTags = construct_available_tags();
	console.log(availableTags);
	$("#keyword-building").autocomplete({
		source : availableTags
	});
});
