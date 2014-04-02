/*****************************************
Name: programAPI.js
Creation Date: 21/03/2014
Last modified: 1/04/2014
Author: Kwinten Pardon

API calls and processing of resulting JSON
concerning programs
 *****************************************/
var divID = null; // UGLY!!!

function process_JSON(data){
	console.log("entered");
	//console.log(data);
			// We are going to store the information in an array and in the end write the array to the given div
			var programs = [];

			$.each(data, function(key, val) {
				// We want the values correspondending the key: 'programs'
				// We check if we have the expected key
				if (key === 'programs'){

					// variable creation
					var id = -1
					var title = -1

					// because strange JSON constructions makes for multiple loops
					$.each(val, function(key, val){
						$.each(val, function(key, val){
							// we store the value of title in the variable 'title'
							if (key == 'title'){
								title = val;
							}
							// Same applies to program-id
							else if (key == 'program-id'){
								id = val;
							}
						})
					})
					// Store the information in the programs array
					programs.push("<li id='" + id + "'>" + title + "</li>");
				}
			})
			// Writing the information to the html div addressed by there given ID
			$(divID).append("<ul id='program-list'></ul>");

			$.each(programs, function(index, value) {
				$("#program-list").append(value);
			});
		}

/*
Name: list_programs
Arguments: 	divID: Required ID of the div where the programs should be listed
			keyword: optional keyword when you are searching for a specific program
Returns: Void
Author: Kwinten Pardon
Date: 01/04/2014
*/
function list_programs(given_divID, keyword){

	try {
		// If divID is empty (required parameter)
		// We throw an error stating that divID is required
		if (typeof given_divID === 'undefined') {
			throw("Requires divID");
		}
		
		divID = given_divID

		// Given a keyword: the command should be find.
		// otherwise the command should be list (list every existing program)
		var command = (typeof keyword === 'undefined') ? "list" : "find";
		// Create API url ServerLove.js for more details
		var url = apiprogram(command);

		// The fun starts with the JSON call
		// this is an AJAX call that executes the given function on the resulting data
		
		// $.getJSON(url, process_JSON);
		if (command == 'list'){
			console.log(command);
			console.log(url);
			$.ajax({
  				type: "GET",
  				url: url,
  				success: process_JSON,
  				dataType: "JSON"
				});
			} else {
				$.ajax({
  				type: "POST",
  				url: url,
  				data: 'keywords='.concat(keyword), 
  				success: process_JSON,
  				dataType: "JSON"
				});
			}

	// If an error was throw we display the error to the console of the user.
	// He may then choose to laugh or warn us about it.
	// He / She will Probably do both
	} catch(error) {
		console.error(error);
	}


}
