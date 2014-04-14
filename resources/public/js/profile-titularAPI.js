/*****************************************
Name: 			profile-titularAPI.js
Creation Date: 	10/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	
*****************************************/

/*
Global Variables
Author: 		Anders Deliens & Youssef Boudiba
Creation Date: 	14/04/2014
Last modified: 	/04/2014	
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
jQuery selectors to trigger the corresponding functions:
*/

$("#save_facilities").click(function(){
	set_facilities_course();
});