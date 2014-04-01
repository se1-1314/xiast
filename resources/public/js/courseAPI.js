/*

*/

function list_courses(keyword){

	var command = (typeof keyword === 'undefined') ? "list" : ("find/").concat(keyword);
	var url = apicourse(command);

	$.getJSON(url, function (data){
		console.log(data);
	})

}