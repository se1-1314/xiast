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

/*

*/
function apiURL(root, command){
	return root.concat(command);
}

/*

*/
function apicourse(command){
	return apiURL(courses, command);
}

/*

*/
function apiprogram(command){
	return apiURL(programs, command);
}