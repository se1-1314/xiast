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
					coursecode: '1000330ANR',
					title: 'Vak1',
					start: new Date(y, m, d, 10, 30),
					allDay: false,
				}
				,
								{
					coursecode: '1000332ANR',
					title: 'Vak2',
					start: new Date(y, m, d, 15, 30),
					allDay: false,
				}
				],
		eventClick:  
		function(calEvent, jsEvent, view) 
		{
        	get_facilities_course(calEvent.coursecode);
        }
	});
});