$("#inteface-schedule").hide();
$("#edit-schedule").click(function(){
	$("#inteface-schedule").toggle("slow");
});

//Classroom edit
/*
Name: 			show_buildings
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID (used as a placeholder)
Returns: 		none
Creation Date: 	05/05/2014
*/

function show_buildings(divID)
{
	return function(data)
	{//data is a json structure that must be iterated through to obtain the needed values.
		var assigned_courses = [];
		$.each(data, function(key, val) 
		{
			//[]
			$.each(data, function(key, val) 
			{
				//{}
				$.each(val, function(key, val) 
				{
					//key-value pairs
					$.each(val, function(key, val) 
					{								
					});
					//construct the html element containing the building list.
					assigned_courses.push("<li id =</li>");
				});			
			});
		});
		$(divID).empty();
		$(divID).append('<ul id="building-list"></ul>');

		$.each(assigned_courses,function(index, value)
		{
			$("#assigned_course_list").append(value);
		});
		$(".course-item").click(function()
		{//Eventhandler to show the course description of a selected course.
			get_rooms(this.id, "#room-list");
		});
	};	
}

/*
Name: 			get_buildings
Author: 		Anders Deliens & Youssef Boudiba
Arguments: 		divID
Returns: 		none (callback function handles additional work).
Creation Date: 	05/05/2014
*/

function list_buildings(divID, keyword)
{
	try {
			var url = '/api/room/' + keyword;
			$.ajax(
				{
			  		type: "GET",
			  		url: url,
			  		success: show_buildings(divID),
			  		dataType: "JSON"
				});	
		}
	catch(error) 
		{
			alert(error.message);
		}
}
function find_building(divID){

	var keyword = $("#keyword-building").val();
	list-buildings(divID, keyword);
}