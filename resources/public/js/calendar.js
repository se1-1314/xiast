/*
Name: calendar.js
Creation Date: 06/02/2014
Author: Kwinten Pardon

Creating manipulating and rendering of a calendar instance
*/

/************************************************************************************************************/
/********************************** global variables ********************************************************/
/************************* may be moved to a global config file *********************************************/
/************************************************************************************************************/
var days_per_week = 7;
var max_slots_per_day = 34;
var days_array_nl = ["maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag"];
var days_array_en = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"];
var days_array = [days_array_nl, days_array_en];
/************************************************************************************************************/


/*
name: week_matrix
Arguments:	week_calendar:	A week calendar instance

Returns: 2 dimensional array wich contains the information of the week
Author: Kwinten Pardon
Date: 25/02/2014

Extracts the information matrix from the week calendar
*/
function week_matrix(week_calendar){
	return week_calendar[1];
}


/*
Name: week_number
Arguments:	week_calender:	A week calendar instance

Returns: Integer indicating what week of the year this week calendar represents
Author: Kwinten Pardon
Date: 25/02/2014

Extracts the week number from the week calendar
*/
function week_number(week_calendar){
	return week_calendar[0];
}


/*
Name: write_days
Arguments:	week_calendar	: A week calendar instance
			language		: the integer value of the given language
							: 0 = Dutch, 1 = English
Returns: calendar
Author: Kwinten Pardon
Date: 06/02/2014

Writes the name of the days in the calendar in the given language
This is where we use the extra timeslot for.
Note: it's a assignment procedure.
*/
function write_days(week_calendar, language){
	var week_calendar = week_matrix(week_calendar);
	var lang_days_array = days_array[language] // Extract the days by language. Since the names of the days are located in an array and we expect the language to be an integer, we can simple extract the day names in the right language
	for (var index = 1; index < week_calendar.length; index++){ // The first time slot of each day is reservad for the day name
		week_calendar[index][0] = lang_days_array[index -1];
	}
	return week_calendar;
}


/*
Name: write_time
Arguments:	week_calendar	: A week calendar instance
Returns: calendar
Author: Kwinten Pardon
Date: 06/02/2014

Writes the time indication. This where we use the extra "day" slot for
Note: it's a assignment procedure.
*/
function write_time(week_calendar){
	var week_calendar = week_matrix(week_calendar);
	var time_array = week_calendar[0];
	var hour = 7;
	var minutes = "00";
	for (var index = 1; index < time_array.length; index++){
		time_array[index] = hour + ":" + minutes;
		if (minutes == "00"){
			minutes = "30";
		}
		else{
			minutes = "00";
			hour = hour + 1;
		}
	}
	return week_calendar
}

/*
Name: create_week_calendar
Arguments: None
Returns: calendar
Author: Kwinten Pardon
Date: 06/02/2014

creates a calendar.
A week calendar is array with length 2
The first slot contains the week number (function week_number)
The second slot contains the information matric (function week_matrix)

The matrix information is day, timeslot
*/
function create_week_calendar(week_number){ // a calendar is a two dimensional array
	//console.info('entered create_week_calendar');
	var week_calendar = new Array(days_per_week + 1) // we create a buffer array to write the time of the timeslots
	//console.info('2 dimensional array created');
	for (var index = 0; index < week_calendar.length; index++){
		week_calendar[index] = new Array(max_slots_per_day + 1); // we add an extra timeslot. This timeslot will be used to write the correspondending day in
	}
	//console.info('added extra timeslots');
	week_calendar = [week_number, week_calendar] // Array literal
	//console.info('overhead array with week_number');
	write_days(week_calendar, 0) // TODO read language form cookie, database
	write_time(week_calendar)
	return week_calendar
}

/*
Name: add_event
Arguments: 	week_calendar	: A week calendar instance
			event_name		: The name of the event in string
			day				: Integer value of the day (1 = monday => 7 = sunday)
			start_slot		: Integer value of the start_slot
			end_slot		: Integer value of the end_slot
Returns: calendar
Author: Kwinten Pardon
Date: 06/02/2014

End slot is included.
Adds the event to the calendar
*/
function add_event(week_calendar, event_name, day, start_slot, end_slot){
	try{
		// assertions
		if (day < 1) throw "Day may not be negative or zero";
		if (day > 7) throw "There a no more then 7 days";
		if (end_slot < start_slot) throw "An event can't stop before it has begon";
		
		var week_calendar = week_matrix(week_calendar);
		var day = week_calendar[day];
		for (slot = start_slot; slot <= end_slot; slot++){
			var current_slot = day[slot];
			// If the time slot is an array (occurs when 2 events are already taking place at the same time)
			if (current_slot instanceof Array){
				var slot_event_number = current_slot.length;
				/* minimum length = 1; indexing starts at 0
				therefore the last element in the array is length -1
				when adding an new element to the array it suffices to add an element at length which increments the actual length to length +1*/
				current_slot[slot_event_number] = event_name;
			}
			// the slot already contains exactly 1 event
			else if (typeof current_slot !== 'undefined'){
				var new_slot = [current_slot, event_name];
				day[slot] = new_slot;
			}
			// empty slot
			else {
				day[slot] = event_name;
			}
		}
	}
	catch(error){
		console.error(error);
	}
}

function week_calendar_render(calendar){
	try{
		throw "Not implemented";
	}
	catch(error){
		console.error(error);
	}
}

/***********************************
function calendar_test(){
	console.info('entered test');
	c = create_week_calendar(5);
	console.info('calendar created');
	write_days(c, 0);
	console.info('days written');
	add_event(c ,"t", 1, 0, 2);
	add_event(c ,"t2", 1, 2, 3);
	add_event(c ,"t3", 1, 3, 4);
	add_event(c ,"t4", 1, 0, 4);
	add_event(c ,"t4", 0, 0, 4);
	add_event(c ,"t4", 8, 0, 4);
	console.info('events created');
	return c
}
***********************************/