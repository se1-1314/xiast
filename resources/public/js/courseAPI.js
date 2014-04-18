/*
Name: courseAPI.js
Creation Date: 21/03/2014
Author: Kwinten Pardon

API calls and processing of resulting JSON concerning courses
*/

function print_courses(divID){

	return function (data){
	console.log(data);
//	console.log('optional: ' + data.optional);
//	console.log('mandatory: ' + data.mandatory);

	$(divID).empty();
	$(divID).append("<ul id='course-list' class='listing'></ul>");

	$("#course-list").append("<h2>Mandatory</h2>");
	$.each(data.mandatory, function(val, key){
		$("#course-list").append("<li id='" + key + "' class='list-item btn course-item'>" + key + "</li>");
	});


	$("#course-list").append("<h2>Optional</h2>");
	$.each(data.optional, function(val, key){
		$("#course-list").append("<li id='" + key + "' class='list-item btn course-item'>" + key + "</li>");
	});

	}
}

function print_course_info(divID){

	return function(data){
		$(divID).empty();
		$(divID).append("<ul id='course-info-ul' class='listing'></ul>");
		$("#course-info-ul").append("<h2>" + data.title + "</h2>");
	}

}

function list_courses_by_program(divID, program){

	try {
		// If divID is empty (required parameter)
		// We throw an error stating that divID is required
		if ((typeof divID === 'undefined') || (typeof program === 'undefined')) {
			throw("Both parameters are required");
		}
		var url = apiprogram("get").concat("/" + program);

		// The fun starts with the JSON call
		// this is an AJAX call that executes the given function on the resulting data
			$.ajax({
  				type: "GET",
  				url: url,
  				//success: process_JSON_program(divID),
  				success: print_courses(divID),
  				dataType: "JSON"
				});

	// If an error was throw we display the error to the console of the user.
	// He may then choose to laugh or warn us about it.
	// He / She will Probably do both
	} catch(error) {
	//	console.log(error);
	}


}

function get_course_info(divID, course){

	try {
		// If divID is empty (required parameter)
		// We throw an error stating that divID is required
		if ((typeof divID === 'undefined') || (typeof course === 'undefined')) {
			throw("Both parameters are required");
		}

		var url = apicourse("get").concat("/" + course);
		console.log(url);

		$.ajax({
  				type: "GET",
  				url: url,
  				//success: process_JSON_program(divID),
  				success: print_course_info(divID),
  				dataType: "JSON"
				});

	} catch(error) {
		console.log(error);
	}

}

function print_student_courses(divID){
	
	return function(data){

		var enrollments = data.enrollments;
		console.log(enrollments);

		$(divID).empty();
		$(divID).append("<ul id='student_courses'></ul>");

		$.each(enrollments, function(index, value){
			$("#student_courses").append("<li>" + value.course + "</li>");
		})
	}
}


function list_courses_by_current_student(divID){
	try {

		if (typeof divID === 'undefined') {
			throw("No div ID makes me cry");
		}

		var url = apienrollment("student");
		console.log(url);

		$.ajax({
  				type: "GET",
  				url: url,
  				//success: process_JSON_program(divID),
  				success: print_student_courses(divID),
  				dataType: "JSON"
				});

	}catch(error) {
		console.log(error);
	}
}

function getCourseNameByCourseCode(coursecode){

	var url = apicourse("get").concat("/" + coursecode);
	var result = "";

	$.ajax({
		type: "GET",
		url: url,
		success: function(data){result = data.title},
		async: false,
		dataType: "JSON"
	});
	return result;
}


$("#courses").on("mousedown", ".course-item", function (){
	console.log(this);
	$('.course-item').removeClass('active');
	$(this).addClass('active');
	get_course_info("#course-info", this.id);
})
