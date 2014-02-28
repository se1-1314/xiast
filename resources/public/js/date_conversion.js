/*
Name: date_conversion.js
Creation Date: 28/02/2014
Author: Kwinten Pardon

conversion from week day standard used by the VUB to the 
*/

var start_date = "09/16/2013"

/*

*/
function VUB_standardTOstandard_time(week, day){

	var days_to_add = (7 * (week - 1)) + day -1;
	var start = new Date(start_date);
	return  new Date(start.setDate(start.getDate() + days_to_add));

}

/*
*/
function standard_timeTOVUB_standard(date){

	var start = new Date(start_date);
	var stop_date = new Date(start_date);
	stop_date = new Date(start.setDate(stop_date.getDate() + 7));
	var week_counter = 1;
	var given_date = new Date(date);

	while(given_date > stop_date){
		week_counter += 1;
		given_date.setDate(given_date.getDate() - 7);
	}

	var day = given_date.getDate() - start.getDate() + 8; // Fuck if I know why +8

	console.info("Kwinten is awesome"); // 8h30 coding session taking its toll

	return [week_counter, day]

}