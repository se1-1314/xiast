$(document).ready(function() {

	var date = new Date();
	var d = date.getDate();
	var m = date.getMonth();
	var y = date.getFullYear();

	console.log('wohoo');

	$('#schedule').fullCalendar({
		aspectRatio: 2,
		defaultView: 'agendaWeek',
		header: {
			left: 'prev,next today',
			center: 'title',
			right: 'agendaWeek,agendaDay'
		},
		editable: true,
		events: []

	});
});