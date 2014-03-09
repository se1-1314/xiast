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
		events: [{
					title: 'Software engineering',
					start: new Date(2014, 2, 12, 9, 0),
					end: new Date(2014, 2, 12, 11, 0),
					allDay: false
				},
				{
					title: 'Some random class',
					start: new Date(2014, 2, 13, 15, 0),
					end: new Date(2014, 2, 13, 18, 0),
					allDay: false
				},

				]

	});
});