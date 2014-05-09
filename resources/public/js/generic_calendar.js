// Global variables
var AspectRatio = 2;
var defaultView = 'agendaWeek';
var header = { left: 'prev,next today',
               center: 'title',
               right: 'agendaMonth,agendaWeek,agendaDay'};
var c;    // Represents the calendar view. Initialized by calendar_onload()
var jqobj = $("#schedule-content");


// MISC FUNCTIONS
//------------------------------------------------------------------------------
function remove_from_array(events, event){
    var idx = events.indexOf(event);
    if (idx != -1){
        events.splice(idx, 1);
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

// CREATE CALENDAR
//------------------------------------------------------------------------------
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

// Renders a calendar variable to a calendar view and displays it on the screen
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

function destroy_calendar(jqobj, calendar){
    try{
        jqobj.fullCalendar('destroy');
    } catch(error){
        console.log(error);
    }
}

// CONVERT
//------------------------------------------------------------------------------
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

// CALENDAR MANIPULATIONS
//------------------------------------------------------------------------------
// ADD
//................................................
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

// TODO: make a function (in autocomplete.js) which returns a String[] of
// ?day-slot? suggestions (check with adeliens & aleijnse) to plan an activity


// TODO: create a repeat function/variable to keep track
// of how many weeks a specific activity should be repeated(lavholsb)
// or fetch this from the form <<<

// TODO: make a function (in autocomplete.js) which returns a String[]
// roomsuggestions (lavholsb <-> aleijnse)


// TODO: adapt to new form type of 'create event' (lavholsb -> kwpardon)
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

// DELETE
//................................................

// ONCLICK CALLBACK
// Callback function: when an event has been clicked it will be highlighted in
// red and a delete button will appear. When this button has been clicked,
// the selected event will be deleted
// TODO: split this function: onclick part, delete button part (vb.
// stel dat ik later nog iets anders wil doen als een event wordt aangeklikt)
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


// MOVE
//................................................
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

// RESET
//................................................
function calendar_reset(calendar){

}


// PROPOSALS
//------------------------------------------------------------------------------

// FIXME (aleijnse)
function hack_around_backend_bug(schedule_block) {
    var sb = jQuery.extend({}, schedule_block);
    sb.item = jQuery.extend({}, sb.item);
    var course_code = sb.item['course-code'];
    delete sb.item['course-code'];
    sb.item['course-id'] = course_code;
    return sb;
}

// GENERATE
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
// SEND

// Generates a proposal, sends the proposal, and refreshes the
// calendarview to reset the internal events (lavholsb)
function send_proposal() {
    send_schedule_proposal(generate_schedule_proposal(c));
    // destroy_calendar($("#schedule-content"), c);
   // calendar_onload();
}



// Sends a compatible proposal to the back-end scheduler
function send_schedule_proposal(prop){
    $.ajax({
        type: 'POST',
        url: '/api/schedule/proposal/apply',

        // reload the page to clear new/moved/deleted events in fullcalendar
        success: function() {location.reload();},
        contentType: "application/json",
        data: JSON.stringify(prop),
        dataType: 'JSON'});
}

// ONLOAD
//------------------------------------------------------------------------------
// Loads the schedule of the user currently logged in into the calendar
function load_current_user_schedule(c){
    var scheduleblocks = get_current_user_schedule();
    scheduleblocks.forEach(function (sb) {
        add_schedule_block(c, sb);
    });
}
function calendar_onload(){
    if(current_user == "guest" || current_user == "student"){
        c = create_calendar();}
    else if(current_user == "titular" || current_user == "program-manager"){
        c = create_modifiable_calendar();}

    load_current_user_schedule(c);

    render_calendar($("#schedule-content"), c);
}

// TODO: should be called only when page.onload() (lavholsb)+ FIXME
calendar_onload();

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

// add_new_schedule_block($("#schedule-content"), c, sb2);
// add_new_schedule_block($("#schedule-content"), c, sb3);

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
function get_courses_courseactivities(){
}

function course_activity_string(course_title, activity_name) {
    return course_title + ": " + activity_name;
}
function flatten(list){
    return [].concat.apply([],list);
}
function course_activities(course){
    return c.activities.map(function(a) {
        return {
            course_code: c["course-code"],
            course_title: c.title,
            activity_id: a.id,
            activity_name: a.name};});
}

function get_room_suggestions(week, day, first_slot, last_slot,
                              proposal,
                              callback) {
    return $.ajax({
        type: "POST",
        url: "/api/room/free/"+week+"/"+day+"/"+first_slot+"/"+last_slot,
        data: JSON.stringify(proposal),
        contentType: "application/json",
        success: callback,
        dataType: "JSON"});
}

function get_schedule_block_suggestions() {};
