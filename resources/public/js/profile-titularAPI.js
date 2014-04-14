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
		$(divID).empty();
		$(divID).append('<ul id="assigned_course_list" class="listing"></ul>');
		$.each(assigned_courses,function(index, value)
		{
			$("#assigned_course_list").append(value);
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