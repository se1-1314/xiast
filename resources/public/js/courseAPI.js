/*
Name: courseAPI.js
Creation Date: 21/03/2014
Author: Kwinten Pardon

API calls and processing of resulting JSON concerning courses
*/

function print_json(data){
	console.log(data);
}

function list_courses(divID, keyword){

	try {
		// If divID is empty (required parameter)
		// We throw an error stating that divID is required
		if (typeof divID === 'undefined') {
			throw("Requires divID");
		}

		// Given a keyword: the command should be find.
		// otherwise the command should be list (list every existing program)
		var command = (typeof keyword === 'undefined') ? "list" : "find";
		// Create API url ServerLove.js for more details
		var url = apicourse(command);

		// The fun starts with the JSON call
		// this is an AJAX call that executes the given function on the resulting data
		if (command == 'list'){

			$.ajax({
  				type: "GET",
  				url: url,
  				//success: process_JSON_program(divID),
  				success: print_json,
  				dataType: "JSON"
				});

			} else {

				var data = new Object();
				data.keywords = [keyword];
				data = JSON.stringify(data);

				$.ajax({
  				type: "POST",
  				url: url,
  				data: data, 
  				processData: false,
  				contentType: "application/json",
  				//success: process_JSON_program(divID),
  				success: print_json,
  				dataType: "JSON",

				});
			}

	// If an error was throw we display the error to the console of the user.
	// He may then choose to laugh or warn us about it.
	// He / She will Probably do both
	} catch(error) {
	//	console.log(error);
	}


}