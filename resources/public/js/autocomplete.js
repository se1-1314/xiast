// Converts array of raw programs to an array of strings:
// ["program_title -- program_id", ...].
// To be used in auto-complete list when looking for
// schedule of a specific program
function get_program_titles_ids(raw_programs){
    var titles_ids = new Array();
    for(var i = 0; i < raw_programs.length; i++){
       titles_ids[i] = raw_programs[i].title + " -- " + raw_programs[i].id;
    }
return titles_ids;
}


// TODO: Create function which returns a String[] containing
// courses with their activities(lavholsb)
function get_courses_courseactivities(raw_courses_details){

}
