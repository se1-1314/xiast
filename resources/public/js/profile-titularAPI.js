/*****************************************
Name: 			profile-titularAPI.js
Creation Date: 	14/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	API handels user request for the titular profile.
*****************************************/

/*
Name: 			show_assigned_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID (used as a placeholder)
Returns: 		none
Creation Date: 	14/04/2014
*/

function show_assigned_courses(divID)
{
	return function(data)
	{
		var assigned_courses = [];
		//courses:
		$.each(data, function(key, val) 
		{
			var course_title;
			var course_code;
			//[]
			$.each(data, function(key, val) 
			{
				//{}
				$.each(val, function(key, val) 
				{
					//key-value pairs
					$.each(val, function(key, val) 
					{
						if (key === 'course-code')
						{
							course_code = val;
						}
						else if (key === 'title')
						{
							course_title = val;
						}
						else if (key === 'titular' && val === '0000585')
						{
							//class may be more suitable.
							assigned_courses.push("<li id ='" + course_code + "' class='list-item btn course-item'>" + course_title + "</li>");
						}	
					});
				});			
			});
		});
		assigned_courses = $.unique(assigned_courses);
		$(divID).empty();
		$(divID).append('<ul id="assigned_course_list" class="listing"></ul>');
		
		//---------------------------------------------------------------
		var seen = [];
		$.each(assigned_courses,function(index, value)
		{
			var html_var = $(value);
			var html_text = html_var.text();
			if (seen[html_text])
			{
				 assigned_courses[index] = 0; 
			}
			else
			{
				seen[html_text] = true;
			}
		});
		for (var i = assigned_courses.length - 1; i >= 0; i--) 
		{
			if (assigned_courses[i] === 0) 
			{
				assigned_courses.splice(i, 1);
			}
		}
		//---------------------------------------------------------------

		$.each(assigned_courses,function(index, value)
		{
			$("#assigned_course_list").append(value);
		});
		$(".course-item").click(function()
		{
			get_course_info(this.id, "#description");
		});
	};	
}

/*
Name: 			get_assigned_courses
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID
Returns: 		none (callback function handles additional work).
Creation Date: 	14/04/2014
*/

function get_assigned_courses(divID)
{
	try {
			var url = apicourse('list');
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_assigned_courses(divID),
			  		dataType: "JSON"
				});	
		}
	catch(error) 
		{
			alert(error.message);
		}
}


/*
Name: 			show_course_info
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID
Returns: 		none (callback function handles additional work).
Creation Date: 	14/04/2014
*/

function show_course_info(divID)
{
	return function(data)
	{
		var result = [];
		$.each(data, function(key, val) 
		{
			if (key === 'description')
			{
				$(divID).empty();
				$(divID).text(val);
			}
			else if (key === 'activities')
			{
				$.each(val, function(key, val) 
				{
					$.each(val, function(key, val) 
					{
						var type = "";
						var fac = [0,0];                                  
						$.each(val, function(key, val) 
						{
							if(key === 'type')
							{
								type = val;
							}
							else if(key === 'facilities')
							{
								{
									var i;
									var l = val.length;
									for ( i = 0; i < l; ++i) 
									{
										if(val[i] == 'beamer')
										{
											fac[0] = 1;
										}
										else if(val[i] == 'overhead-projector')
										{
											fac[1] = 1;
										}
									}
								}
							}
						});
						var facilities = [type, fac];
						result.push(facilities);	
					});
				});
			}
		});
		alert(result.join(',  '));
	};	
}

/*
Name: 			get_course_info
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID & course_code
Returns: 		none (callback function handles additional work).
Creation Date: 	14/04/2014
*/

function get_course_info(course_code, divID)
{
	try {
			var url = apicourse('get/' + course_code);
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_course_info(divID),
			  		dataType: "JSON"
				});	
		}
	catch(error) 
		{
			alert(error.message);
		}
}

/*
Name: 			show_facilities
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		none
Returns: 		none
Creation Date: 	10/04/2014
Last modified: 	14/04/2014
*/

function show_facilities()
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

function get_facilities(course_code,divID)
{
	try {
			var url = apicourse('activity/get/' + course_code);
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_facilities(),
			  		dataType: "JSON"
				});	
		}
	catch(error) 
		{
			alert(error.message);
		}
}

/*
Global Variables
Author: 		Anders Deliens & Youssef Boudiba
Creation Date: 	10/04/2014
Last modified: 	14/04/2014	
*/


/*
jQuery selectors to trigger the corresponding functions:
*/
