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
// Send Proposal(titular)

function send_proposal(snd, prop, msg){
    var url = "/api/schedule/message";
    var json_data = {
        sender : snd,
        proposal : prop,
        message : msg
    };
    var data = JSON.stringify(json_data);
    write_data(url, data);
    $("#send-proposal-event").modal('hide');
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
            activity_id: a.id,
            activity_name: a.name};});
}

function fill_activity_list(activity_list){
    // <select> list
    var activities = flatten(users_schedulable_courses().map(course_activities));
    activities.map(function(a){
        option = document.createElement("option");
        option.innerHTML = a.course_title + ": " + a.activity_name;
        activity_list.appendChild(option);
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
// function create_event(){
//     // fetch the form by id
//     var form = $("#event-creation")[0];

//     var sb = new Object();
//     sb.room = new Object();
//     sb.item = new Object();

//     sb.week = +form.week.value;
//     sb.day = +form.day.value;
//     sb['first-slot'] = +form.first_slot.value;
//     sb['last-slot'] = +form.last_slot.value;
//     sb.item["course-activity"] = +form.course_activity.value;
//     sb.item["course-code"] = form.course_code.value;
//     sb.room.building = form.building.value;
//     sb.room.floor = +form.floor.value;
//     sb.room.number = +form.number.value;
//     if (true){
//         add_new_schedule_block($("#schedule-content"), c ,sb);
//         //form.reset();
//     }
//     else {
//         alert("Invalid form");
//     }
// }

$(document).ready(function(){
    // Fill day+start-slot combinations
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
                {weeks: [start_week, start_week + repeat],
                 days: days,
                 slots: [1, 26]},
                duration,
                activity.activity_id,
                current_proposal(),
                fill_schedule_block_suggestions);
    });
    $(".modal").modal('hide');
    $("#schedule-activity").click(function() {
        var date = date_to_VUB_time(date_shown_on_calendar());
        $("#start-week").val(date[0]);
        $("#day").val(date[1]);
        var activity_list = document.getElementById("course-activities");
        fill_activity_list(activity_list);
        update_current_vub_week();
        $("#schedule-activity-event").modal('show');
    });
    // document.getElementById("send-proposal").onclick = function() {
    //     $("#send-proposal-event").modal('show');
    // };
    $("#send").click(function(){
        var proposal = generate_schedule_proposal(c);
        var titular = "titular";
        var message = $("#message").val();
        send_proposal(titular, proposal, message);
    });

});
