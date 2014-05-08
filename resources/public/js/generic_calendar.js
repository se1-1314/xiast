// Global variables
var AspectRatio = 2;
var defaultView = 'agendaWeek';
var header = { left: 'prev,next today',
               center: 'title',
               right: 'agendaMonth,agendaWeek,agendaDay'};



// Returns a Calendar object with drag&drop disabled by default
function create_calendar(){
    return { new_events: [],
             moved_events: [],
             deleted_block_ids: [],

             AspectRatio: AspectRatio,
             defaultView: defaultView,
             header: header,
             events: [],
             editable: false,
             allDaySlot: false,
             snapMinutes: 30,
             firstHour: 8,
             minTime: 7,
             weekends: true,
             hiddenDays: [7],
             eventDurationEditable: false };
}

// Returns a Calendar object with drag&drop enabled
function create_modifiable_calendar(){
    var calendar = create_calendar();
    calendar.editable = true;
    return calendar;
}

// Converters:
// Scheduleblocks: for back-end scheduler
// Events: for front-end full_calendar view
// Scheduleblock -> Event and Event -> Scheduleblock
function schedule_block_to_event(b){
    var e = {
        title: b.item["course-code"],
        start: VUB_time_to_date(b.week, b.day, b["first-slot"]),
        end: VUB_time_to_date(b.week, b.day, b["last-slot"]),
        allDay: false,
        room: b.room,
        item: b.item };
    if ('id' in b)
        e.schedule_block_id = b.id;
    return e;
}
function event_to_schedule_block(e){
    var start = date_to_VUB_time(e.start);
    var end = date_to_VUB_time(e.end);
    var sb = {
        week: start[0],
        day: start[1],
        'first-slot': start[2],
        'last-slot': end[2],
        item: e.item,
        room: e.room
    };
    if ('schedule_block_id' in e) {
        sb.id = e.schedule_block_id;
    }
    return sb;
}
// ADDERS

// Adds a schedule_block to a given calendar
// Should only be used ONLOAD()
function add_schedule_block(calendar, block){
    return calendar.events.push(schedule_block_to_event(block));
}
// Adds a newly created (p.e. from a form) schedule block to the
// calendar & new_events
function add_new_schedule_block(jqobj, calendar, b){
    var e = schedule_block_to_event(b);
    calendar.new_events.push(e);
    jqobj.fullCalendar('renderEvent', e, true);
}

// Callback when event has been clicked
function calendar_event_click_event(jqobj, calendar){
    return function(calendar_event, js_event, view){
        var button = "<button id=\"delete_button\"type=\"button\" class=\"btn btn-lg btn-danger\">Delete </br>" + calendar_event.title + "</button>";
        if (typeof calendar.previous_clicked !== 'undefined'){
            calendar.previous_clicked.color = "#3a87ad";
            $(jqobj).fullCalendar("updateEvent", calendar.previous_clicked);
        }
        calendar.previous_clicked = calendar_event;
        calendar_event.color = "#FF0000";
        $("#schedule-buttons").empty().append(button);
        $(jqobj).fullCalendar("updateEvent", calendar_event);
        $("#delete_button").click(
            function (){
                console.info("clicked");
                delete_event(jqobj, calendar, calendar_event);});
    }
}

// FIXME
function hack_around_backend_bug(schedule_block) {
    var sb = jQuery.extend({}, schedule_block);
    sb.item = jQuery.extend({}, sb.item);
    var course_code = sb.item['course-code'];
    delete sb.item['course-code'];
    sb.item['course-id'] = course_code;
    return sb;
}

// PROPOSALS
// Generates a back-end scheduler compatible proposal from a given calendar
function generate_schedule_proposal(calendar){
    return {
        new: calendar.new_events.map(event_to_schedule_block)
            .map(hack_around_backend_bug),
        moved: calendar.moved_events.map(event_to_schedule_block)
            .map(hack_around_backend_bug),
        deleted: calendar.deleted_block_ids
    };
}
// Sends a compatible proposal to the back-end scheduler
function send_schedule_proposal(prop){
    $.ajax({
        type: 'POST',
        url: '/api/schedule/proposal/apply',
        success: function(){},
        contentType: "application/json",
        data: JSON.stringify(prop),
        dataType: 'JSON'});
}

// BACK-END GETTERS
// Returns (at the moment) the schedule of program with id = 1 (1e ba CW)
// TODO use these functions in autocomplete lists
// Gets an array of raw programs from back-end
function list_programs(){
    var programs;
    var url = apiprogram('list');
    $.ajax({
        url: url,
        success: function(data){ programs  = data.programs; },
        dataType: 'json',
        async: false });
    return programs;
}
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


function event_dropped(calendar){
    // Events without schedule_block_ids are newly created
    // and are already in the new_events list.
    var moved_events = calendar.moved_events;
    return function(event,dayDelta,minuteDelta,allDay,revertFunc){
        if ('schedule_block_id' in event
            && !moved_events.some(function(e){
                return (e.schedule_block_id == event.schedule_block_id);})) {
            calendar.moved_events.push(event);
        }};
}
function remove_from_array(events, event){
    var idx = events.indexOf(event);
    if (idx != -1){
        events.splice(idx, 1);
    }
}


// Deletes an event (if exists) from a calendar and its event-lists
function delete_event(jqobj, calendar, e){
    if ('schedule_block_id' in e) {
        calendar.deleted_block_ids.push(e.schedule_block_id);
        remove_from_array(calendar.moved_events, e);
    } else {
        remove_from_array(calendar.new_events, e);
    }
    jqobj.fullCalendar('removeEvents', function(e1) {return e === e1;});
}


function render_calendar(obj, calendar){
    try{
        obj.fullCalendar({
            aspectRatio: calendar.aspectRatio,
            defaultView: calendar.defaultView,
            header: calendar.header,
            editable: calendar.editable,
            events: calendar.events,
            eventDrop: event_dropped(calendar),
            eventClick: calendar_event_click_event(obj, calendar)
        });
    } catch(error){
        console.log(error);
    }
}

function is_valid(obj){
    var result = true;
    $.each(obj, function(key, value){
        if (result == false)  {
            return result;
        }
        else if (typeof value == "object"){
            console.log("object");
            result = is_valid(value);
        }
        else if (typeof value == "undefined"){
            console.log("undefined");
            result = false;
        }
        else if (value == ""){
            console.log("empty string");
            result = false;
        }
        else {
            console.log(typeof value);
        }
    });
    console.log(result);
    return result;
}

function create_event(){
    // fetch the form by id
    var form = $("#event-creation")[0];

    var sb = new Object();
    sb.room = new Object();
    sb.item = new Object();

    sb.week = +form.week.value;
    sb.day = +form.day.value;
    sb['first-slot'] = +form.first_slot.value;
    sb['last-slot'] = +form.last_slot.value;
    sb.item.type = form.WPO.checked ? "WPO" : "HOC";
    sb.item["course-activity"] = +form.course_activity.value;
    sb.item["course-code"] = form.course_code.value;
    sb.room.building = form.building.value;
    sb.room.floor = +form.floor.value;
    sb.room.number = +form.number.value;
    if (true){
        add_new_schedule_block($("#schedule-content"), c ,sb);
        //form.reset();
    }
    else {
        alert("Invalid form");
    }
}

var sb1 = new Object();
//sb1.id = 21;
sb1.week = 32;
sb1.day = 1;
sb1['first-slot'] = 4;
sb1['last-slot'] = 7;
sb1.item = new Object();
sb1.item.type = "HOC";
sb1.item["course-activity"] = 1626;
sb1.item["course-id"] = '1000447ANR';
sb1.room = new Object();
sb1.room.building = 'E';
sb1.room.floor = 0;
sb1.room.number = 4;



var sb2 = new Object();
sb2.week = 32;
sb2.day = 1;
sb2['first-slot'] = 8;
sb2['last-slot'] = 11;
sb2.item = new Object();
sb2.item.type = "WPO";
sb2.item["course-activity"] = 1628;
sb2.item["course-id"] = '1000447ANR';
sb2.room = new Object();
sb2.room.building = 'E';
sb2.room.floor = 0;
sb2.room.number = 5;

var sb3 = {
    //id: 23,
    week: 32,
    day: 5,
    'first-slot': 13,
    'last-slot': 16,
    item: {
        type: "HOC",
        "course-activity": 1900,
        "course-code": '1015328ANR',
    },
    room: {
        building: 'E',
        floor: 0,
        number: 6,
    }
}

// TODO: these functions should only be called when loading the page ('onload()')
var c = null;
if(current_user == "guest" || current_user == "student"){
    c = create_calendar();}
else if(current_user == "titular" || current_user == "program-manager"){
    c = create_modifiable_calendar();}

render_calendar($("#schedule-content"), c);


// testing

//var c = create_modifiable_calendar();

// TODO: work this out (lavholsb)
//var schedule = get_current_user_schedule();

//schedule.forEach(function(sb) {
//    add_schedule_block(c, sb); });



function send_proposal() {
    alert("send_proposal");
    send_schedule_proposal(generate_schedule_proposal(c));
}

//$("#apply_button").onclick = send_proposal;

 add_new_schedule_block($("#schedule-content"), c, sb2);
// add_new_schedule_block($("#schedule-content"), c, sb3);
