/*
Name: courseAPI.js
Creation Date: 21/03/2014
Author: Kwinten Pardon

API calls and processing of resulting JSON concerning courses
*/

function list_courses(keyword){

	var command = (typeof keyword === 'undefined') ? "list" : ("find/").concat(keyword);
	var url = apicourse(command);

	$.getJSON(url, function (data){
		console.log(data);
	})

}