/*****************************************



 *****************************************/
function list_programs(divID, keyword){

	try {
		if (typeof divID === 'undefined') {
			throw("Requires divID");
		}
		//typeof divID === 'undefined' ? throw("Requires divID");

		var command = (typeof keyword === 'undefined') ? "list" : ("find/").concat(keyword);
		var url = apiprogram(command);

		$.getJSON(url, function (data) {
			var programs = [];

			$.each(data, function(key, val) {
				if (key === 'programs'){

					var id = -1
					var title = -1

					/*because strange JSON constructions makes for multiple loops*/
					$.each(val, function(key, val){
						$.each(val, function(key, val){
							console.log(key + ' - ' + val);
							if (key == 'title'){
								title = val;
							}
							else if (key == 'program-id'){
								id = val;
							}
						})
					})

					programs.push("<li id='" + id + "'>" + title + "</li>");
				}
			})

			$(divID).append("<ul id='program-list'></ul>");

			$.each(programs, function(index, value) {
				$("#program-list").append(value);
			});
		});
	} catch(error) {
		console.error(error);
	}


}
