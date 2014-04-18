/*****************************************
Name: 			profile-titularAPI.js
Creation Date: 	14/04/2014
Author: 		Anders Deliens & Youssef Boudiba
Description:	API handels user request for the titular profile.
*****************************************/

/*
Global Variables
Author: 		Anders Deliens & Youssef Boudiba
Creation Date: 	18/04/2014
*/

var current_course_code;		//contains coursecode of current selected course.
var current_type;				//HOC/WPO

/*
Name: 			show_assigned_course
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID (used as a placeholder)
Returns: 		none
Creation Date: 	14/04/2014
Last modified:	18/04/2014
*/

function show_assigned_courses(divID)
{
	return function(data)
	{//data is a json structure that must be iterated through to obtain the needed values.
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
					});
					//construct the html element containing the course list.
					assigned_courses.push("<li id ='" + course_code + "' class='list-item btn course-item'>" + course_title + "</li>");
				});			
			});
		});
		$(divID).empty();
		$(divID).append('<ul id="assigned_course_list" class="listing"></ul>');
		
		//Set of actions needed to delete duplicates from course list.
		/*var seen = [];
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
		}*/

		$.each(assigned_courses,function(index, value)
		{
			$("#assigned_course_list").append(value);
		});
		$(".course-item").click(function()
		{//Eventhandler to show the course description of a selected course.
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
Last modified:	18/04/2014
*/

function get_assigned_courses(divID)
{
	try {
			var url = apititular('courses');
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
Returns: 		none (show course description and facilities)
Creation Date: 	14/04/2014
Last modified:	18/04/2014
*/

function show_course_info(divID)
{
	$("#beamer").prop('checked', false);
	$("#overhead-projector").prop('checked', false);
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
			//--------------------------------------------------------------------------
			else if (key === 'activities')
			{//[]
				$.each(val, function(key, val) 
				{//{}
					var type = ""; //HOC or WPO
					var facilities = [0,0];  //[beamer, overhead-projector]  can be 0 (not available) or 1(available).                         
					$.each(val, function(key, val) 
					{
						if(key === 'type')
						{
							type = val;
						}
						else if(key === 'facilities')
						{
							for (var i = 0; i < val.length; ++i) 
							{
								if(val[i] == 'beamer')
								{
									facilities[0] = 1;
								}
								else if(val[i] == 'overhead-projector')
								{
									facilities[1] = 1;
								}
							}
						}
					});
					var course_facilities = [type, facilities];
					result.push(course_facilities);	
				});
			}
			//--------------------------------------------------------------------------
		});
		//Set of actions needed to delete duplicates from course list.
		var seen = [];
		$.each(result,function(index, value)
		{
			if (seen[value])
			{
				 result[index] = 0; 
			}
			else
			{
				seen[value] = true;
			}
		}); 
		for (var i = result.length - 1; i >= 0; i--) 
		{
			if (result[i] === 0) 
			{
				result.splice(i, 1);
			}
		}
		if (result[1][0] == "HOC")
		{
			var temp = result[1];
			result[1] = result[0];
			result[0] = temp;
		}
		$("#hoc").click(function()
		{
			current_type = "HOC";
			$("#beamer").prop('checked', false);
			$("#overhead-projector").prop('checked', false);
			if(result[0][1][0] == 1)
			{
				$("#beamer").prop('checked', true);
			}
			if(result[0][1][1] == 1)
			{
				$("#overhead-projector").prop('checked', true);
			}
		});
		$("#wpo").click(function()
		{
			current_type = "WPO";
			$("#beamer").prop('checked', false);
			$("#overhead-projector").prop('checked', false);
			if(result[1][1][0] == 1)
			{
				$("#beamer").prop('checked', true);
			}
			if(result[1][1][1] == 1)
			{
				$("#overhead-projector").prop('checked', true);
			}
		});
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
			current_course_code = course_code;
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
Name: 			update_course_facilities
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		course_code
Returns: 		none (callback function handles additional work).
Creation Date: 	10/04/2014
*/
function update_facilities(id)
{
	return function(data)
	{
		var url = apicourse('activity/' + id);
		var facilities = [];
		if($("#beamer").prop('checked'))
		{
			facilities.push("beamer");
		}
		console.log($("#overhead-projector").prop('checked'));
		if($("#overhead-projector").prop('checked'))
		{
			facilities.push("overhead-projector");
		}
		var json_var = data;
		json_var.facilities = facilities;
		json_var.instructor = "0";
		var new_data = new Object();	
		new_data = JSON.stringify(json_var);
		$.ajax(
		{
	  		type: "PUT",
	  		url: url,
	  		contentType: "application/json",
	  		data: new_data, 
			processData: false,
			async : false,
			success : function (data) {console.info(data);},
	  		dataType: "JSON"
		});
	};		
}
function update_course_facilities()
{
	return function(data)
	
	{
		console.log(data);
		
		var course_activity_idies = [];		//[id, id,...]
		$.each(data, function(key, val) 
		{
			if (key === 'activities')
			{//[]
				var controle = 0;
				var current_id;
				$.each(val, function(key, val) 
				{//{}         
					$.each(val, function(key, val) 
						{  		              
							if(key === 'type' && val === current_type)
							{
								controle = 1;
							}
							else if(key === 'id')
							{
								current_id = val;
							}
						});
					if(controle == 1)
					{
						course_activity_idies.push(current_id);
						controle = 0;
					}
				});
			}
		});
		console.log(course_activity_idies.join(', '));
		console.log(course_activity_idies.length);
		for (var i = course_activity_idies.length - 1; i >= 0; i--) 
		{
			var id = course_activity_idies[i];
			var url = apicourse('activity/get/' + id);
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: update_facilities(id),
			  		dataType: "JSON"
				});	
		}
	};	
}

/*
Name: 			set_course_info
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		course_code
Returns: 		none (callback function handles additional work).
Creation Date: 	10/04/2014
*/

function set_course_facilities()
{
	try {
		console.info("set course facilities");
			var url = apicourse('get/' + current_course_code);
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: update_course_facilities(),
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

$("#editfacilities").click(function(event){
	event.stopPropagation();
	set_course_facilities();
});

//$("#facilities").on("click", "#editfacilities", function (){
//	console.info("button pushed");
//	set_course_facilities();
//	});
