/*
Name: courseAPI.js
Creation Date: 21/03/2014
Author: Kwinten Pardon

API calls and processing of resulting JSON concerning courses
*/

function print_courses(divID){

	return function (data){

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
		console.error(error);
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

function create_course(){
	var form = $("#create_course_form")[0];

	CourseCode = form.CourseCode.value;
	title = form.title.value;
	titular = form.titular.value;
	description = form.description.value;
	departement = form.departement.value;
	grade = form.ba.checked ? "ba" : "ma";

	if (CourseCode === '' || title === '' || description === '' || departement === '' || grade === '' || titular === ''){
		throw "no field may not be empty"
	}

	course = new Object()
	course["course-code"] = CourseCode;
	course.title = title;
	course.description = description;
	course.titular = titular;
	course.department = departement;
	course.grade = grade;

	data = JSON.stringify(course);
	url = apicourse("add");

	$.ajax({
		type : "POST",
		url : url,
		data : data,
		processData: false,
		contentType: "application/json",
		success : function(data) {console.log(data); form.reset();},
		dataType: "JSON"
	})

	return false;
}

function delete_course(){

	var form = $("#delete_course_form")[0];

	var course_code = form.course_code.value;

	url = apicourse("del").concat("/" + course_code);

	$.ajax({
		type : "DELETE",
		url : url,
		success : function(data) {console.info(data)}
	})

	return false;
}


$("#courses").on("mousedown", ".course-item", function (){
	$('.course-item').removeClass('active');
	$(this).addClass('active');
	get_course_info("#course-info", this.id);
})

$("#PE-course-list").on("mousedown", ".course-item", function (){
	$('.course-item').removeClass('active');
	$(this).addClass('active');
//	$('#remove_course_button').empty()
//	$('#remove_course_button').append("<button class=\"btn btn-danger btn-lg\" onclick=\"remove_course_from_program(\'" + this.id + "\')\">Remove course</button>")
	get_course_info("#course-info", this.id);
})
