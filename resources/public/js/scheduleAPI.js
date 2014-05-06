// Returns an array of scheduleblocks, representing
// the personal schedule of the user logged in
function get_current_user_schedule(){
    var schedule_blocks;
    var url ="/api/schedule/1/36/1/7/1/24";
    $.ajax({
        url: url,
        success: function(data){
            schedule_blocks  = data.schedule},
        dataType: 'json',
        async: false });
    return schedule_blocks;
}
