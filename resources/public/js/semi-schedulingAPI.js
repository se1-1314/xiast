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
function show_facilities_course(divID)
{
	return function(data)
	{
		var facilities = [];
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
									for ( i = 0; i < l; ++i) {
										facilities.push('<li>' + val[i] + '</li>');
									}
							}
					});
				});
			}
		});
				$(divID).empty();
				$(divID).append("<ul id='facilities-list'></ul>");
	
				$.each(facilities, function(index, value) 
				{
					$("#facilities-list").append(value);
				});
	};	
}


function get_facilities_course(divID, course_code)
{
	try {
			if (typeof divID === 'undefined')
			{
				throw("divID undefined");
			}
			var url = apicourse('get/' + course_code);
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

$("#test").click(function(){
	get_facilities_course('#facilities','1000330ANR');
});