/*****************************************
Name:                   programAPI.js
Creation Date:  21/03/2014
Last modified:  04/04/2014
Author:                 Kwinten Pardon

API calls and processing of resulting JSON
concerning programs

*****************************************/

/*****************************************
Name:                   process_JSON_program
Creation Date:  02/04/2014
Last modified:  04/04/2014
Author:                 Kwinten Pardon
Parameters:     divID: The id where the information should be displayed
returns:                function to be used as a callback in the AJAX call

*****************************************/
function process_JSON_program(divID, key){

    return function(data){
        console.log(data);
        // We are going to store the information in an array and in the end write the array to the given div
        var programs = [];

        $.each(data, function(key, val)
               {
                   // We want the values correspondending the key: 'programs'
                   // The key may also be 'result' if the JSON is generated by a find POST request
                   // We check if we have the expected key
                   if ((key === 'programs') || (key === 'result')){

                       // variable creation
                       var id = -1
                       var title = -1

                       // because strange JSON constructions makes for multiple loops
                       $.each(val, function(key, val){
                           $.each(val, function(key, val){
                               // we store the value of title in the variable 'title'
                               if (key == 'title'){
                                   title = val;
                               }
                               // Same applies to program-id
                               else if (key == 'id'){
                                   id = val;
                               }
                           })
                               // Store the information in the programs array
                               programs.push("<li id='" + id + "' class='list-item btn program-item'>" + title + "</li>");
                       })


                           }
               })
            // Writing the information to the html div addressed by there given ID
            $(divID).empty();
        $(divID).append("<ul id='program-list' class='listing'></ul>");

        $.each(programs, function(index, value) {
            $("#program-list").append(value);
        });
    }
}

// Returns an array of programs from back-end (lavholsb)
function sync_list_programs(){
    var programs;
    var url = apiprogram('list');
    $.ajax({
        url: url,
        success: function(data){ programs  = data.programs; },
        dataType: 'json',
        async: false });
    return programs;
}

/*
  Name: list_programs
  Arguments:      divID: Required ID of the div where the programs should be listed
  keyword: optional keyword when you are searching for a specific program
  Returns: Void
  Author: Kwinten Pardon
  Date: 01/04/2014
*/
function list_programs(divID, keyword){

    try {
        // If divID is empty (required parameter)
        // We throw an error stating that divID is required
        if (typeof divID === 'undefined') {
            throw("Requires divID");
        }

        // Given a keyword: the command should be find.
        // otherwise the command should be list (list every existing program)
        var command = (typeof keyword === 'undefined') ? "list" : "find";
        // Create API url ServerLove.js for more details
        var url = apiprogram(command);

        // The fun starts with the JSON call
        // this is an AJAX call that executes the given function on the resulting data
        if (command == 'list')
        {
            console.log(command);
            console.log(url);
            $.ajax(
                {
                    type: "GET",
                    url: url,
                    success: process_JSON_program(divID),
                    dataType: "JSON"
                });
        }
        else
        {
            console.log(command);
            console.log(url);
            console.log(keyword);

            var data = new Object();
            data.keywords = [keyword];
            data = JSON.stringify(data);

            console.log(data);
            console.log("---------");
            $.ajax(
                {
                    type: "POST",
                    url: url,
                    data: data,
                    processData: false,
                    contentType: "application/json",
                    success: process_JSON_program(divID),
                    dataType: "JSON",
                });
        }
        // If an error was throw we display the error to the console of the user.
        // He may then choose to laugh or warn us about it.
        // He / She will Probably do both
    } catch(error) {
        console.error(error);
    }
}


function find_programs(divID){

    var form = $("#program-search")[0];
    var keyword = form.keyword.value;
    form.keyword.value = "";

    list_programs(divID, keyword);

    return false;
}

function create_program_success(data){
    console.info(data);
}

function create_program(){

    var form = $("#program-creation")[0];

    var title = form.title.value;
    var description = form.description.value;
    var manager = form.manager.value;

    if ( title === '' ||  description === '' || manager === ''){
        throw "form may not contain empty values";
    }

    var data = new Object();
    data.title = title;
    data.description = description;
    data.manager = manager
    data.mandatory = [];
    data.optional = []
    data = JSON.stringify(data);

    url = apiprogram("add");

    $.ajax({
        type: "POST",
        url: url,
        data: data,
        processData: false,
        contentType: "application/json",
        success: function (data){console.info(data); form.reset();},
        dataType: "JSON",

    });

    $("#NewProgram").modal("hide");

    return false;
}

function delete_program(program_id){

    url = apiprogram("del").concat("/" + program_id);

    $.ajax({
        type: "DELETE",
        url: url,
        success: function (data) {console.info(data)}
    })
}

$("#programs").on("mousedown", ".program-item", function (){
    $('.program-item').removeClass('active');
    $(this).addClass('active');
    list_courses_by_program("#courses", this.id);
})

$("#PE-program-list").on("mousedown", ".program-item", function (){
    $('.program-item').removeClass('active');
    $(this).addClass('active');
    $('#delete_program_button').empty()
    $('#delete_program_button').append("<button class=\"btn btn-danger btn-lg\" onclick=\"delete_program(\'" + this.id + "\')\">Delete Program</button>")
    list_courses_by_program("#PE-course-list", this.id);
})
