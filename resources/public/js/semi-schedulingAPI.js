/*****************************************
Name: 			semi-schedlulingAPI.js
Creation Date: 	10/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	API handels user request for the semi-scheduling.
*****************************************/

/*
Global Variables
Author: 		Anders Deliens & Youssef Boudiba
Creation Date: 	10/04/2014
Last modified: 	14/04/2014	
*/

var json_var;		//contains result of a get request and is used in several functions as a way to retrieve information  without making additional get requests.
var calendarEvent;	//contains selected calendar event.

/*
Name: 			show_facilities_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		none
Returns: 		none
Creation Date: 	10/04/2014
Last modified: 	14/04/2014
*/

function show_facilities_course()
{
	return function(data)
	{
		json_var = data;
		$("#beamer").prop('checked', false);
		$("#overhead-projector").prop('checked', false);
		//$("#speakers").prop('checked', false);
		$.each(data, function(key, val) 
		{
			if (key === 'facilities')
			{
				var i;
				var l = val.length;
				for ( i = 0; i < l; ++i) 
				{
					if(val[i] == 'beamer')
					{
						$("#beamer").prop('checked', true);
					}
					else if(val[i] == 'overhead-projector')
					{
						$("#overhead-projector").prop('checked', true);
					}
					//else if(val[i] == 'speakers')
					//{
						//$("#speakers").prop('checked', true);
					//}
				}
			}
		});
	};	
}

/*
Name: 			get_facilities_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		Calendar event
Returns: 		none (callback function handles additional work).
Creation Date: 	10/04/2014
Last modified: 	14/04/2014
Note: 			Triggered by clicking on calendar event (see calenderviewer.js);
*/

function get_facilities_course(calEvent)
{
	try {
			var url = apicourse('activity/get/' + calEvent.course_activity_id);
			//calenderEvent variable is used to save calEvent for use in future PUT requests.
			calendarEvent = calEvent;
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_facilities_course(),
			  		dataType: "JSON"
				});	
		}
	catch(error) 
		{
			alert(error.message);
		}
}

/*
Name: 			update_course_activity_id
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		data (JSON result of a PUT request)
Returns: 		none (set course_activity_id to the new created one).
Creation Date: 	10/04/2014
Last modified: 	14/04/2014
Note: 			Silly function but AJAX calls require a succes function.
*/

function update_course_activity_id(data)
{
	calendarEvent.course_activity_id = data.id;
}

/*
Name: 			set_facilities_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		none
Returns: 		none (callback function handles additional work).
Creation Date: 	10/04/2014
Last modified: 	14/04/2014
*/

function set_facilities_course()
{
	try {
			var url = apicourse('activity/' + calendarEvent.course_activity_id);
			var facilities = [];
			if($("#beamer").prop('checked'))
			{
				facilities.push("beamer");
			}
			if($("#overhead-projector").prop('checked'))
			{
				facilities.push("overhead-projector");
			}
			//if($("#speakers").prop('checked'))
			//{
			//	facilities.push("speakers");
			//}
			json_var.facilities = facilities;
			json_var.instructor = "0";
			var data = new Object();	
			data = JSON.stringify(json_var);
			$.ajax(
				{
			  		type: "PUT",
			  		url: url,
			  		contentType: "application/json",
			  		data: data, 
		  			processData: false,
		  			success: update_course_activity_id,
			  		dataType: "JSON"
				});			
		}
	catch(error) 
		{
			alert(error.message);
		}
}

/*
jQuery selectors to trigger the corresponding functions:
*/

$("#save_facilities").click(function(){
	set_facilities_course();
});