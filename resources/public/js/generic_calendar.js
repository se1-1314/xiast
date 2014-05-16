// Global variables
// JQuery FullCalendar object
var calendar;

var current_user = "program-manager";
var error_color = "red";
var selected_color = "green";

var the_empty_proposal = {
    new: [],
    moved: [],
    // ids of deleted blocks
    deleted: []
}
var current_proposal = the_empty_proposal;

var selected_event = null;

// MISC FUNCTIONS
//------------------------------------------------------------------------------
function remove_from_array(events, event) {
    var idx = events.indexOf(event);
    if (idx != -1) {
        events.splice(idx, 1);
    }
}
function ids_in_proposal(p) {
    return p.moved.map(function(b){return b.id;})
        .concat(p.deleted);
}
function proposal_new_and_moved(p){
    return p.new.concat(p.moved);
}

// CONVERT
//------------------------------------------------------------------------------
// Scheduleblocks: for back-end scheduler
// Events: for front-end full_calendar view
// Scheduleblock -> Event and Event -> Scheduleblock
// TODO (lavholsb): add/subtract 30 minutes to fit events in calendar
function schedule_block_to_event(b){
    var e = {
        // TODO (lavholsb): edit event title
        title: b.item["course-code"],
        start: VUB_time_to_date(b.week, b.day, b["first-slot"]),
        end: VUB_time_to_date(b.week, b.day, b["last-slot"]),
        allDay: false,
        room: b.room,
        item: b.item
    };
    if ('id' in b)
        e.schedule_block_id = b.id;
    return e;
}
function proposal_block_to_event(b) {
    e = schedule_block_to_event(b);
    e.block_in_proposal = b;
    return e;
}

// TODO (lavholsb): add/subtract 30 minutes to fit events in calendar
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
// Returns an array of scheduleblocks, representing
// the personal schedule of the logged in user.
// Uses async callback
function get_users_schedule(start, end, success_callback) {
    var start_VUB = date_to_VUB_time(start);
    // Fixme, end probably needs + 30mins
    var end_VUB = date_to_VUB_time(end);
    var url = "/api/schedule/"
        + +start_VUB[0] + "/"
        + +end_VUB[0] + "/"
        + 1 + "/"
        + 7 + "/"
        + 1 + "/"
        + 30;
    skewer.log(url);
    $.ajax({
        type: "GET",
        url: url,
        dataType: 'JSON',
        success: function(data){ success_callback(data.schedule); },
        cache: true,
        async: true});
}
function calendar_schedule_source(start, end, callback) {
    skewer.log("bli");
    // Get the relevant blocks from start to end for current user
    get_users_schedule(start, end, function(schedule) {
        // Remove the blocks in the current proposal, as other functions
        // add these manually
        var ids_in_current_proposal = ids_in_proposal(current_proposal);
        var relevant_blocks = _.reject(schedule, function(b){
            return _.contains(ids_in_current_proposal, b.id); });
        // skewer.log(relevant_blocks.map(function(b) { return b.id}));
        var relevant_events = relevant_blocks.map(schedule_block_to_event);
        // The callback loads the events into the calendar
        callback(relevant_events);
    });
}
function calendar_proposal_source(start, end, callback) {
    skewer.log("bla");
    callback(
        proposal_new_and_moved(current_proposal)
            .map(proposal_block_to_event));
}
function calendar_render_proposal_block(b){
    calendar.fullCalendar('renderEvent',
                          proposal_block_to_event(b),
                          true);
}
// ADD
//................................................
// Adds a newly created (p.e. from a form) schedule block to the
// calendar & new_events
function add_new_schedule_block(b) {
    current_proposal.new.push(b);
    var e = schedule_block_to_event(b);
    calendar.fullCalendar('renderEvent', e, true);
}

// DELETE
//................................................


function create_delete_button(jqobj, calendar, calendar_event){
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

// ONCLICK CALLBACK
// Callback function: when an event has been clicked it will be highlighted
function calendar_event_click_event(calendar_event, js_event, view){
	if (current_user.toLowerCase() === 'student' || current_user.toLowerCase() === 'guest'){
        throw "Not authorized";
    }
    if (selected_event == calendar_event) {
        delete selected_event.color;
        selected_event = null;
    } else {
        if (selected_event != null)
            delete selected_event.color;
        calendar_event.color = selected_color;
        selected_event = calendar_event;
    }
    calendar.fullCalendar("rerenderEvents");
}

function alert_screen(type, msg){
    alert = '<div class="alert container alert-dismissable alert-' + type + '" id="alert"> \
    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times</button> \
    <div id="alert-body">' + msg + '</div> \
    </div>';
    $("#menu").after(alert);
}

// Deletes an event (if exists) from a calendar and its event-lists
function delete_event(e) {
    if (current_user.toLowerCase() === 'student' || current_user.toLowerCase() === 'guest'){
        throw "Not authorized";
    }
    if (e === null){
        alert_screen("info", "Please select the event you want to delete first");
    }
    if ('schedule_block_id' in e) {
        current_proposal.deleted.push(e.schedule_block_id);
        remove_from_array(current_proposal.moved, event_to_schedule_block(e));
    } else {
        remove_from_array(current_proposal.new, event_to_schedule_block(e));
    }
    calendar_remove_event(e);
}

// MOVE
//................................................
function event_dropped(event, dayDelta, minuteDelta, allDay, revertFunc) {
    // Not a new event and not already in moved of current proposal
    if ('schedule_block_id' in event && !current_proposal.moved.some(function(b) {
        return (event.block_in_proposal == b);
    })) {
        var b = event_to_schedule_block(event);
        event.block_in_proposal = b;
        current_proposal.moved.push(b);
    } else {
        // Event is either in moved or new of proposal, so update it
        $.extend(event.block_in_proposal, event_to_schedule_block(event));
    }
}

// RESET
//................................................
function calendar_remove_event(e){
    calendar.fullCalendar('removeEvents', function(e1) {
        return e === e1;
    });
}
function calendar_reset(){
    calendar.fullCalendar('removeEvents');
    current_proposal = the_empty_proposal;
    calendar.fullCalendar('refetchEvens');
    alert_screen("success", "Reset complete");
}

// PROPOSALS
//------------------------------------------------------------------------------

// FIXME (aleijnse)
// course-code -> course-id (for back end)
function hack_around_backend_bug(schedule_block) {
    var sb = jQuery.extend({}, schedule_block);
    sb.item = $.extend({}, sb.item);
    var course_code = sb.item['course-code'];
    delete sb.item['course-code'];
    delete sb.item.title;
    sb.item['course-id'] = course_code;
    return sb;
}

// hack to convert a complete schedule-proposal: course-code -> course-id
function fix_proposal_wrt_backend_bugs(proposal) {
    return {
        new: proposal.new.map(hack_around_backend_bug),
        moved: proposal.moved.map(hack_around_backend_bug),
        deleted: proposal.deleted
    };
}

// GENERATE
// Generates a back-end scheduler compatible proposal
function calendar_load_proposal(p){
    current_proposal = p;
    calendar.fullCalendar("rerenderEvents");
}

function calendar_replace_proposal(p){
    calendar_reset();
    calendar_load_proposal(p);
}

// APPLY
// Sends current_proposal to apply/save it into the DB and refreshes the
// calendarview to reset the internal events (lavholsb)
function send_apply_request(success) {
    apply_schedule_proposal(fix_proposal_wrt_backend_bugs(current_proposal),
                            success);
}

// Sends a compatible proposal to the back-end scheduler
function apply_schedule_proposal(prop, success) {
    postJSON('/api/schedule/proposal/apply', prop, success);
}

// EDIT
// Sends current_proposal to apply/save it into the DB and refreshes the
// calendarview to reset the internal events (lavholsb)
function send_check_request(success){
    // FIXME: hack around back-end bug: convert_to_proposal_id
    check_schedule_proposal(fix_proposal_wrt_backend_bugs(current_proposal),
                            success);
}

// Sends a compatible proposal to the back-end scheduler
function check_schedule_proposal(prop, success) {
    postJSON('/api/schedule/proposal/check', prop, success);
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

function date_shown_on_calendar() {
	return calendar.fullCalendar('getDate');
}

function calendar_go_to_block(sb) {
    calendar.fullCalendar(
        "gotoDate", VUB_time_to_date(sb.week, sb.day, sb["first-slot"]));
}


function mark_erratic_blocks(err_blocks){
    // Map over existing events in calendar and change color of each
    // erroneous one, then rerender
    calendar.fullCalendar("clientEvents", function(e){
        return _.some(err_blocks, function(err_b) {
            return _.isEqual(err_b, event_to_schedule_block(e))});
    }).forEach(function(e){
        e.color = error_color;
        e.erratic = true;
    });
    calendar.fullCalendar("rerenderEvents");
}
function unmark_erratic_blocks() {
    calendar.fullCalendar("clientEvents", function(e) {
        return e.erratic;
    }).forEach(function(e) {
        delete e.color;
    });
    calendar.fullCalendar("rerenderEvents");
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
        eventSources: [calendar_schedule_source, calendar_proposal_source],
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
});
