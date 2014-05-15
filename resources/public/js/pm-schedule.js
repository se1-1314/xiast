function get_requests() {
    var json_data = [{
        sender : 'titular1',
        proposal : 'null',
        message : 'message1'
    }, {
        sender : 'titular2',
        proposal : 'null',
        message : 'message2'
    }, {
        sender : 'titular3',
        proposal : 'null',
        message : 'message3'
    }];
    return json_data;
}

function load_request_proposal(p){
    calendar_replace_proposal(p);
}

function load_requests_list(requests){
    // Request list table body
    var requests_list = $("#requests-list-body");
    requests_list.empty();
    // Add table rows with relevant click event callbacks
    requests.forEach(function(r){
        var row = $('<tr><td>Request from: ' + r.sender + '</td></tr>');
        row.click(function(){
            $("#request-description").empty();
            $("#request-description").append(r.message);
            load_request_proposal(r.proposal);
        });
        requests_list.append(row);
    });
}

$(document).ready(function() {
    // show requests list
    load_requests_list(get_requests());
});
