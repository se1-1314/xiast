/*****************************************
Name: 			semi-schedlulingAPI.js
Creation Date: 	10/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	API handels user request for the semi-scheduling.
*****************************************/


/*
Name: get_facilities
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 	none
Returns: a string (or array) containing the selected facilities
Creation Date: 10/04/2014
*/
function get_facilities()
{
	$("#save_facilities").click(function(){
	  var result = $("#facilities input:checked").map(
	     function () {return this.value;}).get().join(', ');
		return result;
	});
}

/*
Name: get_facilities_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 	none
Returns: a string (or array) containing the selected facilities
Creation Date: 10/04/2014
!We gaan hier vanuit dat we vakken kunnen selecteren en dus de bijhorende courseid kunnen opvragen!
*/
function get_facilities_course(divID,course-code)
{
	try {
			if (typeof divID === 'undefined')
			{
				throw("divID undefined");
			}
	
			// Create API url ServerLove.js for more details
			var url = apicourse('/get/' + course-code);
	
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_facilities_course(divID),
			  		dataType: "JSON"
				});
				
		}
	catch(error) 
		{
			alert(error.message);
		}
}

function show_facilities_course(divID)
{
	return function(data){
	var facilities = [];
	
	$.each(data, function(key, val) 
			{
				if (key === 'activities')
				{
					$.each(val, function(key, val)
						{
								if (key == 'course_facility_requirements')
								{
									$.each(val, function(key, val)
										{
											if (key == 'facilities')
												{
													$.each(val, function(key, val)
														{
															if (key == 'enum')
															{
																$.each(val, function(key, val)
																{
																	facilities.push("<li>" +  val + "</li>");
																});
															}
														});
												}
										});
								}
						});
				}
			});
			// Writing the information to the html div addressed by there given ID
			$(divID).empty();
			$(divID).append("<ul id='facilities-list'></ul>");

			$.each(facilities, function(index, value) 
			{
				$("#facilities").append(value);
			});
}		
$("#test").click(function()
	{
		$("#schedule-content").fadeIn('slow').fadeOut('slow');
		get_facilities_course('facilities',007);
	});
