/*****************************************
Name: 			semi-schedlulingAPI.js
Creation Date: 	10/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	API handels user request for the semi-scheduling.
*****************************************/


/*
Name: 			get_facilities_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		none
Returns: 		a string (or array) containing the selected facilities
Creation Date: 	10/04/2014
comment:		new api features were added to directly recieve the requirements instead of going through course
				to update in the future
*/

var json_var;
var calendarEvent;


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

function update_course_activity_id(data)
{
	calendarEvent.course_activity_id = data.id;
}

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
			//facilities.push("speakers");
			//}
			json_var.facilities = facilities;
			json_var.instructor = "0";
			var data = new Object();
			
			data = JSON.stringify(json_var);
			if (typeof data === 'undefined'){
				// Do nothing
			}
			else{
			//alert(data);
			console.log(data);
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
		}
	catch(error) 
		{
			alert(error.message);
		}
}

$("#save_facilities").click(function(){
	set_facilities_course();
});