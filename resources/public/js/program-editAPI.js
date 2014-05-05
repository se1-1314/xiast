$('#submitprogramsearch').click(function(){
	list_programs('program-list');
});


$('#add-program').click(function(){
	var programma = prompt('Please Enter the name of the new Program.','');
	if(programma != ''){
		var url = apiprogram('add');

		$.ajax(
		{
			type: "POST",
			url: url,
			success: alert(programma),
			dataType: "JSON"
		});
	}
});