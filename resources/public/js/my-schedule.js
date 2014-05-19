// Fetching data

function write_data(url, data) {
    $.ajax({
        type : "POST",
        url : url,
        contentType : "application/json",
        data : data,
        async : false,
        processData : false,
        dataType : "JSON"
    });
}

// Get VUB week from current date

function update_current_vub_week() {
    var d = new Date();
    var date_array = date_to_VUB_time(d);
    $("#start-week").attr("placeholder", date_array[0]);
}
function course_activity_string(course_title, activity_name) {
    return course_title + ": " + activity_name;
}
function course_activities(c){
    return c.activities.map(function(a) {
        return {
            course_code: c["course-code"],
            course_title: c.title,
            activity_id: +a.id,
            activity_name: a.name};});
}

function fill_activity_list(){
    // <select> list
    var activity_list = $("#course-activities");
    var activities = _.flatten(users_schedulable_courses().map(course_activities),
                               true);
    activities.map(function(a){
        option = document.createElement("option");
        option.innerHTML = a.course_title + ": " + a.activity_name;
        option.course_code = a.course_code;
        option.value = +a.activity_id;
        activity_list.append(option);
    });
}
function get_schedule_block_suggestions(
    timespan, length, activity_id, proposal, callback){
    postJSON(
        "/api/schedule/proposal/available-blocks",
        {timespan: timespan,
         "block-length": length,
         "course-activity": activity_id,
         proposal: proposal},
        callback);
}
function get_room_suggestions(week, day, first_slot, last_slot,
                              proposal, callback){
    postJSON("/api/room/free/"+week+"/"+day+"/"+first_slot+"/"+last_slot,
             proposal,
             callback);
}
function schedule_block_to_day_hour_option(b){
    var opt = document.createElement("option");
    opt.innerHTML = "W" + b.week + "D" + b.day + "S" +
        b["first-slot"] + "-" + b["last-slot"];
    opt.week = b.week;
    opt.day = b.day;
    opt["first-slot"] = b["first-slot"];
    return opt;
}
function fill_schedule_block_suggestions(blocks){
    suggestions = $("#day-hour");
    suggestions.empty();
    var sorted = blocks.sort(function(a, b){
        if (a.week < b.week) return -1;
        if (a.week > b.week) return 1;
        if (a.day < b.day) return -1;
        if (a.day < b.day) return 1;
        if (a["first-slot"] < b["first-slot"]) return -1;
        if (a["first-slot"] > b["first-slot"]) return 1;
        return 0;
    });
    sorted.forEach(function(b){
        suggestions.append(schedule_block_to_day_hour_option(b));});
}
function create_event(){
    var form = $("#schedule-activity-event form")[0];
    var activities = form["course-activities"];
    var rooms = form["room-floor"];
    var room = rooms[rooms.selectedIndex];
    var schedule_block =
        {week: +form["start-week"].value,
         day: +form.day.value,
         // FIXME first-slot =/= start-hour
         "first-slot": +form["start-hour"].value,
         "last-slot": +form["start-hour"].value + +form.duration.value - 1,
         item: {
             "course-activity": +activities.value,
             "course-code": activities[activities.selectedIndex].course_code},
         room: {
             building: room.building,
             floor: +room.floor,
             number: +room.number}};
    // FIXME
    add_new_schedule_block(schedule_block);
}
function fill_room_list(room_ids){
    room_ids.map(function(rid){
        var opt = document.createElement("option");
        opt.innerHTML = room_id_string(rid);
        opt.floor = rid.floor;
        opt.building = rid.building;
        opt.number = rid.number;
        $("#room-floor").append(opt);
    });
}
function load_schedule_check_result(res){
    unmark_erratic_blocks();
    mark_erratic_blocks(res.concerning);
    calendar_go_to_block(res.concerning[0]);
}
function load_schedule_check_results(results){
    var error_log = $("#error-log");
    // Populate error log
    // Remove all rows except header row
    error_log.find("tr:gt(0)").remove();
    // Add new rows
    results.forEach(function(r){
        // FIXME backend sends items with course-id fields, but
        // everything works with course-code here
        r.concerning.forEach(function(block){
            var course_id = block.item["course-id"];
            block.item["course-code"] = course_id;
            delete block.item["course-id"];
        });
        var row = $('<tr class="danger"><td>'+r.type+'</td></tr>');
        row.click(function(){
            load_schedule_check_result(r);
        });
        error_log.append(row);
    });
}
function update_schedule_block_suggestions(){
    $("#day-hour").empty();
    var activities = $("#course-activities").get(0);
    var activity = activities.options[activities.selectedIndex];
    var start_week = +$("#start-week").val();
    var repeat = +$("#repeat").val();
    var duration = +$("#duration").val();
    var day = +$("#day").val();
    var days = (day) ? [day, day] : [1, 6];
    if (activity && start_week && repeat && duration)
        get_schedule_block_suggestions(
            {weeks: [start_week, start_week],
             days: days,
             slots: [1, 26]},
            duration,
            +activity.value,
            current_proposal,
            fill_schedule_block_suggestions);
}
$(document).ready(function(){
    // Fill day+start-slot combinations
    $.getJSON("/api/room/list", function(data){
        fill_room_list(data.rooms.map(function(r){return r.id;}));
    });
    // Add activity form
    // Update schedule block suggestions on changes of the following:
    ["#course-activities",
     "#day",
     "#start-week",
     "#repeat",
     "#duration"].forEach(function(id){
         $(id).change(_.debounce(update_schedule_block_suggestions, 1000));
//         $(id).change(_.debounce(update_room_suggestions, 1000));
     });
    $(".modal").modal('hide');
    $("#day-hour").change(function(){
        var opt = this.options[this.selectedIndex];
        $("#week").val(opt.week);
        $("#day").val(opt.day);
        $("#start-hour").val(opt["first-slot"]);
    });
    // Buttons
    $("#schedule-activity").click(function() {
        var date = date_to_VUB_time(date_shown_on_calendar());
        $("#start-week").val(date[0]);
        $("#day").val(date[1]);
        fill_activity_list();
        update_current_vub_week();
        $("#schedule-activity-event").modal('show');
    });
    $("#add-schedule-block-btn").click(create_event);
        $("#edit_button").click(function() {
        alert("edit button not yet defined");
    });
    $("#delete_button").click(function() {
        delete_event(selected_event);
    });
    $("#reset_button").click(function(){
        calendar_reset();
    });
    $("#check_button").click(function() {
        send_check_request(function(check_results){
            skewer.log(check_results);
            if (check_results.length == 0){
                alert_screen("success", "Check passed");
            }
            else{
                alert_screen("warning", "Check failed. Look at the error log for more details");
            }
            load_schedule_check_results(check_results);
        });
    });
});
