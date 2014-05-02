/*
Name: ServerLove.js
Creation Date: 21/03/2014
Author: Kwinten Pardon

To prevent the use of hard coded urls for api call, This library has been created to generate the required url
*/


/********************************
********Global variables*********
*********************************/

var api_root = "/api/";
var courses = api_root.concat("course/");
var programs = api_root.concat("program/");
var enrollment = api_root.concat("enrollment/");
var schedule = api_root.concat("schedule/");
var titular = api_root.concat("titular/");

/*
Creation Date: 21/03/2014
Author: Kwinten Pardon
*/
function apiURL(root, command){
	return root.concat(command);
}

/*
Creation Date: 21/03/2014
Last modified: 02/04/2014
Author: Kwinten Pardon
*/
function apicourse(command){
	return apiURL(courses, command);
}

/*
Creation Date: 18/04/2014
Author: Anders Deliens & Youssef Boudiba
*/
function apititular(command){
	return apiURL(titular, command);
}

/*
Creation Date: 21/03/2014
Author: Kwinten Pardon
*/
function apiprogram(command){
	return apiURL(programs, command);
}

function apienrollment(command){
	return apiURL(enrollment, command);
}

function apischedule(command){
	return apiURL(schedule, command);
}