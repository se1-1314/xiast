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
function show_facilities_course()
{
	return function(data)
	{
		alert(coursecode);
		$("#beamer").prop('checked', false);
		$("#overhead-projector").prop('checked', false);
		$("#speakers").prop('checked', false);
		$.each(data, function(key, val) 
		{
			if (key === 'activities')
			{
				$.each(val, function(key, val)
				{
					$.each(val, function(key, val)
					{
						if (key == 'facilities')
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
								else if(val[i] == 'speakers')
								{
									$("#speakers").prop('checked', true);
								}
							}
						}
					});
				});
			}
		});
	};	
}


function get_facilities_course(course_code)
{
	try {
			var url = apicourse('get/' + course_code);
			coursecode = course_code;
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
var coursecode = '';

function set_facilities_course(course_code)
{
	try {
			var url = apicourse('delete/' + course_code);
			$.ajax(
				{
			  		type: "DELETE",
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