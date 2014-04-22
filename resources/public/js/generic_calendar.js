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
function schedule_block_to_event(b){
    return {
        title: b.item["course-id"],
        start: VUB_time_to_date(b.week, b.day, b["first-slot"]),
        end: VUB_time_to_date(b.week, b.day, b["last-slot"]),
        allDay: false,
        schedule_block_id: b.id,
        room: b.room,
        item: b.item
    };
}

// Adds a schedule_block to a given calendar
function add_schedule_block(calendar, block){
return calendar.events.push(schedule_block_to_event(block));
}

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
        $("#delete_button").click(function () {console.info("clicked"); delete_event(jqobj, calendar, calendar_event);});
    }
}

function populate_calendar_request(calendar){
/*          var url = api_schedule()

            $.ajax({
            type: "GET"
            url:
            })
    */}

function event_to_schedule_block(e){
    var start = date_to_VUB_time(e.start);
    var end = date_to_VUB_time(e.end);
    return {
        id: e.schedule_block_id,
        week: start[0],
        day: start[1],
        'first-slot': start[2],
        'last-slot': end[2],
        item: e.item,
        room: e.room
    };
}
function generate_schedule_proposal(calendar){
    return {
        new: calendar.new_events.map(event_to_schedule_block),
        moved: calendar.moved_events.map(event_to_schedule_block),
        deleted: calendar.deleted_block_ids
    };
}
function send_schedule_proposal(prop){
    $.ajax({
        type: 'POST',
        url: '/api/schedule/proposal/apply',
        succes: function(){},
        dataType: 'JSON'});
}

function event_dropped(calendar){
    // Events without schedule_block_ids are newly created
    // and are already in the new_events list.
    return function(event,dayDelta,minuteDelta,allDay,revertFunc){
        if (event.hasOwnProperty('schedule_block_id')) {
            calendar.moved_events.push(event);
        }};
}
function event_list_remove_event(events, event){
    var idx = events.indexOf(event);
    if (idx != -1){
        events.splice(idx, 1);
    }}


// Deletes an event (if exists) from a calendar and its event-lists
function delete_event(jqobj, calendar, e){
    if (e.hasOwnProperty('schedule_block_id')){
        calendar.deleted_block_ids.push(e);
        event_list_remove_event(calendar.new_events, e);
        event_list_remove_event(calendar.moved_events, e);
        jqobj.fullCalendar('removeEvents', function(e1) {return e === e1;});
    }
}
// Adds a newly created (p.e. from a form) schedule block to the
// calendar & new_events
function add_new_schedule_block(jqobj, calendar, b){
    var e = schedule_block_to_event(b);
    calendar.new_events.push(e);
    jqobj.fullCalendar('renderEvent', e, true);
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

    sb.id = 24;// should be returned from AJAX request
    sb.week = form.week.value;
    sb.day = form.day.value;
    sb['first-slot'] = form.first_slot.value;
    sb['last-slot'] = form.last_slot.value;
    sb.item.type = form.WPO.checked ? "WPO" : "HOC";
    sb.item["course-activity"] = 5; // where to retrieve this?
    sb.item["course-id"] = form.course_code.value;
    sb.room.building = form.building.value;
    sb.room.floor = form.floor.value;
    sb.room.number = form.number.value;

    if (is_valid(sb)){

        add_schedule_block(c ,sb); // need a way to correctly find / extract the calendar object
        render_calendar($("#schedule-content"), c);

        form.reset();

        }
    else {
        // do something
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
sb1.item["course-activity"] = '5';
sb1.item["course-id"] = '1000428ANR';
sb1.room = new Object();
sb1.room.building = 'E';
sb1.room.floor = 1;
sb1.room.number = 8;

var sb2 = new Object();
//sb2.week = 32;
sb2.day = 1;
sb2['first-slot'] = 8;
sb2['last-slot'] = 11;
sb2.item = new Object();
sb2.item.type = "WPO";
sb2.item["course-activity"] = '6';
sb2.item["course-id"] = '2000431ANR';
sb2.room = new Object();
sb2.room.building = 'E';
sb2.room.floor = 1;
sb2.room.number = 7;

var sb3 = {
    //id: 23,
    week: 32,
    day: 4,
    'first-slot': 13,
    'last-slot': 16,
    item: {
        type: "HOC",
        "course-activity": '7',
        "course-id": '1004123ANR',
    },
    room: {
        building: 'E',
        floor: 1,
        number: 7,
    }
}

// testing

var c = create_modifiable_calendar();


render_calendar($("#schedule-content"), c);

add_new_schedule_block($("#schedule-content"), c, sb1);
add_new_schedule_block($("#schedule-content"), c, sb2);
add_new_schedule_block($("#schedule-content"), c, sb3);
