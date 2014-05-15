// Global variables
// JQuery FullCalendar object
var calendar;

var current_user = "program-manager";
var error_color = "red";
var selected_color = "green";

// Proposal management
var evt_prop = {
    new_events: [],
    moved_events: [],
    deleted_events: [],
};
var erratic_events = [];
var selected_event = null;

// MISC FUNCTIONS
//------------------------------------------------------------------------------
function remove_from_array(events, event) {
    var idx = events.indexOf(event);
    if (idx != -1) {
        events.splice(idx, 1);
    }
}

// CONVERT
//------------------------------------------------------------------------------
// Scheduleblocks: for back-end scheduler
// Events: for front-end full_calendar view
// Scheduleblock -> Event and Event -> Scheduleblock
function schedule_block_to_event(b) {
    var e = {
        title: b.item["course-code"],
        start: VUB_time_to_date(b.week, b.day, b["first-slot"]),
        end: VUB_time_to_date(b.week, b.day, b["last-slot"]),
        allDay: false,
        room: b.room,
        item: b.item,
        orig: $.extend(true, {}, b)
    };
    if ('id' in b)
        e.schedule_block_id = b.id;
    return e;
}

function event_to_schedule_block(e) {
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
// Adds a newly created (p.e. from a form) schedule block to the
// calendar & new_events
function add_new_schedule_block(b) {
    var e = schedule_block_to_event(b);
    evt_prop.new_events.push(e);
    calendar.fullCalendar('renderEvent', e, true);
}

// DELETE
//................................................

// ONCLICK CALLBACK
// Callback function: when an event has been clicked it will be highlighted
function calendar_event_click_event(calendar_event, js_event, view) {
    if (selected_event != null) delete selected_event.color;
    calendar_event.color = selected_color;
    selected_event = calendar_event;
    calendar.fullCalendar("rerenderEvents");
}

// Deletes an event (if exists) from a calendar and its event-lists
function delete_event(e) {
    if ('schedule_block_id' in e) {
        evt_prop.deleted_events.push(e);
        remove_from_array(evt_prop.moved_events, e);
    } else {
        remove_from_array(evt_prop.new_events, e);
    }
    calendar_remove_event(e);
}

// MOVE
//................................................
function event_dropped(event, dayDelta, minuteDelta, allDay, revertFunc) {
    // Not a new event and not already in calendar.moved_events
    if ('schedule_block_id' in event && !evt_prop.moved_events.some(function(e) {
        return (e.schedule_block_id == event.schedule_block_id);}))
    {
        evt_prop.moved_events.push(event);
    }
}

// RESET
//................................................
function calendar_remove_event(e){
    calendar.fullCalendar('removeEvents', function(e1) {
        return e === e1;
    });
}
function calendar_remove_event_add_orig(e){
    calendar_remove_event(e);
    calendar.fullCalendar("renderEvent", schedule_block_to_event(e.orig), true);
}
function calendar_reset(){
    // Delete new and moved events
    evt_prop.new_events.forEach(calendar_remove_event);
    evt_prop.new_events = [];
    evt_prop.moved_events.forEach(calendar_remove_event_add_orig);
    evt_prop.moved_events = [];
    evt_prop.deleted_events.forEach(calendar_remove_event_add_orig);
    evt_prop.deleted_events = [];
    unmark_erratic_blocks();
}

// PROPOSALS
//------------------------------------------------------------------------------

// FIXME (aleijnse)
function hack_around_backend_bug(schedule_block) {
    var sb = jQuery.extend({}, schedule_block);
    sb.item = jQuery.extend({}, sb.item);
    var course_code = sb.item['course-code'];
    delete sb.item['course-code'];
    delete sb.item.title;
    sb.item['course-id'] = course_code;
    return sb;
}

// GENERATE
// Generates a back-end scheduler compatible proposal
function generate_schedule_proposal() {
    return {
        new: evt_prop.new_events.map(event_to_schedule_block)
            .map(hack_around_backend_bug),
        moved: evt_prop.moved_events.map(event_to_schedule_block)
            .map(hack_around_backend_bug),
        deleted: evt_prop.deleted_events.map(function(e){
            return e.schedule_block_id; })
    };
}

function calendar_replace_proposal(p){
    calendar_reset();
    // TODO
}

// SEND
// Generates a proposal, sends the proposal, and refreshes the
// calendarview to reset the internal events (lavholsb)
function send_proposal() {
    send_schedule_proposal(generate_schedule_proposal());
    // destroy_calendar($("#schedule-content"), c);
}

// Sends a compatible proposal to the back-end scheduler
function send_schedule_proposal(prop) {
    $.ajax({
        type: 'POST',
        url: '/api/schedule/proposal/apply',

        // reload the page to clear new/moved/deleted events in fullcalendar
        success: function() {
            location.reload();
        },
        contentType: "application/json",
        data: JSON.stringify(prop),
        dataType: 'JSON'
    });
}

// Returns an array of scheduleblocks, representing
// the personal schedule of the logged in user
function get_users_schedule() {
    var url = "/api/schedule/1/52/1/7/1/24";
    var schedule_blocks;
    $.ajax({
        type: "GET",
        url: url,
        dataType: 'JSON',
        success: function(data){
            schedule_blocks = data.schedule;},
        async: false});
    return schedule_blocks;
}

function send_proposal() {
    alert("send_proposal");
    send_schedule_proposal(generate_schedule_proposal());
}

// Converts array of raw programs to an array of strings:
// ["program_title -- program_id", ...].
// To be used in auto-complete list when looking for
// schedule of a specific program
function get_program_titles_ids(raw_programs) {
	var titles_ids = new Array();
	for (var i = 0; i < raw_programs.length; i++) {
		titles_ids[i] = raw_programs[i].title + " -- " + raw_programs[i].id;
	}
	return titles_ids;
}

function postJSON(url, data, succes) {
    return $.ajax({
        type: "POST",
        url: url,
        data: JSON.stringify(data),
        contentType: "application/json",
        success: succes,
        dataType: "JSON"
    });
}

function current_proposal() {
	return generate_schedule_proposal();
}

function date_shown_on_calendar() {
	return calendar.fullCalendar('getDate');
}

function calendar_go_to_block(sb) {
    calendar.fullCalendar(
        "gotoDate", VUB_time_to_date(sb.week, sb.day, sb["first-slot"]));
}


function mark_erratic_blocks(err_blocks) {
    // Map over existing events in calendar and change color of each
    // one, then rerender
    evt_prop.events.forEach(function(e) {
        var sb = event_to_schedule_block(e);
        err_blocks.forEach(function(err_sb) {
            if (_.isEqual(sb, err_sb)) {
                e.color = error_color;
                erratic_events.push(e);
                calendar.fullCalendar("rerenderEvents");
            }
        });
    });
}

function unmark_erratic_blocks() {
    erratic_events.forEach(function(e) {
        delete e.color;
        calendar.fullCalendar("rerenderEvents");
    });
    erratic_events = [];
}

// PAGE-BUTTON INVOKES
//------------------------------------------------------------------------------
$(document).ready(function() {
    calendar = $("#schedule-content");
    // Renders a calendar variable to a calendar view and displays it on the screen
    calendar.fullCalendar({
        aspectRatio: 1.6,
        defaultView: 'agendaWeek',
        header: { left: 'prev,next today',
                  center: 'title',
                  right: 'agendaMonth,agendaWeek,agendaDay' },
        editable: (current_user == "titular"|| current_user == "program-manager")
            ? true : false,
        events: get_users_schedule().map(schedule_block_to_event),
        eventDrop: event_dropped,
        eventClick: calendar_event_click_event,
        allDaySlot: false,
        snapMinutes: 30,
        firstHour: 8,
        minTime: 7,
        weekends: true,
        hiddenDays: [0],
        eventDurationEditable: false
    });

    $("#delete_button").click(function() {
        delete_event(selected_event);
    });

    $("#reset_button").click(function(){
    calendar_reset();
        });
});
