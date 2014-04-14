
// Global variables
var AspectRatio = 2;
var defaultView = 'agendaWeek';
var header = { left: 'prev,next today',
			   center: 'title',
			   right: 'agendaMonth,agendaWeek,agendaDay'}




function create_calendar(){
	var calendar = new Object();
	calendar.AspectRatio = AspectRatio;
	calendar.defaultView = defaultView;
	calendar.header = header;
	calendar.events = [];
	calendar.editable = false;
	return calendar;
}

function create_modifiable_calendar(){
	var calendar = create_calendar;
	calendar.editable = true;
	return calendar;

}

function populate_calendar(calendar){
	var events = calendar.events;

	return function(data){
		
		var anEvent = {
			title: getCourseNameByCourseCode(data.item["course-id"]),
			start: VUB_standardTOstandard_time(data.week, data.day, data["first-slot"]),
			end: VUB_standardTOstandard_time(data.week, data.day, data["last-slot"]),
			allDay: false
		}
		console.log(anEvent);
		events.push(anEvent);
		console.log(events);
		console.log(calendar.events);
		
	}
}

function populate_calendar_request(calendar){


/*	var url = apischedule()

	$.ajax({
		type: "GET"
		url: 
	})
*/

}

function render_calendar(divID, calendar){
	try{
		if ((typeof divID === 'undefined') || (typeof calendar === 'undefined')){
			throw "give parameters! - render_calendar \n divID: " + divID + "\n calendar: " + calendar;
		}

		console.log(calendar);

		$(divID).fullCalendar({
			aspectRatio: calendar.aspectRatio,
			defaultView: calendar.defaultView,
			header: calendar.header,
			editable: calendar.editable,
			events: calendar.events
		})

	} catch(error){
		console.log(error);
	}
}


var sb1 = new Object();
sb1.id = 21;
sb1.week = 32;
sb1.day = 1;
sb1['first-slot'] = 4;
sb1['last-slot'] = 7;
sb1.item = new Object();
sb1.item.type = "HOC";
sb1.item["course-activity"] = '5';
sb1.item["course-id"] = '1000428ANR';
sb1.room = new Object();
sb1.room.building = 'E';
sb1.room.floor = 1;
sb1.room.number = 8;

var sb2 = new Object();
sb2.id = 22;
sb2.week = 32;
sb2.day = 1;
sb2['first-slot'] = 8;
sb2['last-slot'] = 11;
sb2.item = new Object();
sb2.item.type = "WPO";
sb2.item["course-activity"] = '6';
sb2.item["course-id"] = '1000428ANR';
sb2.room = new Object();
sb2.room.building = 'E';
sb2.room.floor = 1;
sb2.room.number = 7;



// testing

var c = create_calendar();
var f = populate_calendar(c);
f(sb1);
f(sb2);

render_calendar("#schedule-content", c);