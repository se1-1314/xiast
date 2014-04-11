$(document).ready(function() {

	var date = new Date();

	var d = date.getDate();
	var m = date.getMonth();
	var y = date.getFullYear();

	console.log(m);

	$('#schedule-content').fullCalendar({
		aspectRatio: 2,
		defaultView: 'agendaWeek',
		header: {
			left: 'prev,next today',
			center: 'title',
			right: 'agendaWeek,agendaDay'
		},
		editable: true,
		events: [
				{
					course_activity_id: '9',
					title: 'Vak1',
					start: new Date(y, m, d, 10, 30),
					allDay: false,
				}
				,
				{
					course_activity_id: '10',
					title: 'Vak2',
					start: new Date(y, m, d, 15, 30),
					allDay: false,
				}
				],
		eventClick:  
		function(calEvent, jsEvent, view) 
		{
        	get_facilities_course(calEvent.course_activity_id);
        }
	});
});