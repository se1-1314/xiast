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
function flatten(list){
    return [].concat.apply([],list);
}
function course_activities(c){
    return c.activities.map(function(a) {
        return {
            course_code: c["course-code"],
            course_title: c.title,
            activity_id: +a.id,
            activity_name: a.name};});
}

function fill_activity_list(activity_list){
    // <select> list
    var activity_list = $("#course-activities");
    var activities = flatten(users_schedulable_courses().map(course_activities));
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
    return opt;
}
function fill_schedule_block_suggestions(blocks){
    suggestions = $("#day-hour");
    suggestions.empty();
    blocks.forEach(function(b){
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
         "last-slot": +form["start-hour"].value + +form.duration.value,
         item: {
             "course-activity": +activities.value,
             "course-code": activities[activities.selectedIndex].course_code},
         room: {
             building: room.building,
             floor: +room.floor,
             number: +room.number}};
    // FIXME
    add_new_schedule_block($("#schedule-content"), c, schedule_block);
}
function fill_room_list(room_ids){
    room_ids.map(function(rid){
        var opt = document.createElement("option");
        opt.innerHTML = rid.building + " " + rid.floor + "." + rid.number;
        opt.floor = rid.floor;
        opt.building = rid.building;
        opt.number = rid.number;
        $("#room-floor").append(opt);
    });
}
function load_schedule_check_result(res){
    calendar_go_to_block(res.concerning[0]);
}
$(document).ready(function(){
    // Fill day+start-slot combinations
    $.getJSON("/api/room/list", function(data){
        fill_room_list(data.rooms.map(function(r){return r.id;}));
    });
    $("#course-activities").change(function(){
        $("#day-hour").empty();
        var activity = this.options[this.selectedIndex];
        var start_week = +$("#start-week").val();
        var repeat = +$("#repeat").val();
        var duration = +$("#duration").val();
        var day = +$("#day").val();
        var days = (day) ? [day, day] : [1, 6];
        if (activity && start_week && repeat && duration)
            get_schedule_block_suggestions(
                {weeks: [start_week, start_week + repeat - 1],
                 days: days,
                 slots: [1, 26]},
                duration,
                +activity.value,
                current_proposal(),
                fill_schedule_block_suggestions);
    });
    $(".modal").modal('hide');
    $("#schedule-activity").click(function() {
        var date = date_to_VUB_time(date_shown_on_calendar());
        $("#start-week").val(date[0]);
        $("#day").val(date[1]);
        fill_activity_list();
        update_current_vub_week();
        $("#schedule-activity-event").modal('show');
    });
    $("#add-schedule-block-btn").click(create_event);
});
