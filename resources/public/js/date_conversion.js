/*
Name: date_conversion.js
Creation Date: 28/02/2014
Author: Kwinten Pardon

conversion from week day standard used by the VUB to the 
*/

var start_date = "09/16/2013"

/*

*/
function VUB_standardTOstandard_time(week, day, timeslot){

	var days_to_add = (7 * (week - 1)) + day -1;
	var start = new Date(start_date);
	var time = TimeslotToTime(timeslot);

	var date = new Date(start.setDate(start.getDate() + days_to_add));

	if (isInt(time)){
		date.setHours(time);
	} else {
		date.setMinutes(30);
		time = time - 0.5;
		date.setHours(time);
	}

	return date;

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

function isOdd(num) { return num % 2 === 1;}

function isInt(n) {
   return n % 1 === 0;
}

function TimeslotToTime(timeslot){
	timeslot = timeslot - 1;
	var first_slot = 7;

	if (isOdd(timeslot)){
		return first_slot + (timeslot / 2);
	} else{
		return first_slot + ((timeslot - 1) / 2) + 0.5;
	}



}