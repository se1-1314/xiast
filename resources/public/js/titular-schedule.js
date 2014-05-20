function send_proposal_with_message(prop, msg) {
    var url = "/api/schedule/message";
    var json_data = {
        proposal : fix_proposal_wrt_backend_bugs(prop),
        message : msg
    };
    var data = JSON.stringify(json_data);
    write_data(url, data);
}

$(document).ready(function(){
    // show modal
    $("#send-proposal").click(function(){
        $("#send-proposal-event").modal("show");
    });
    // send proposal
    $("#send-activity").click(function(){
        var message = $("#message").val();
        send_proposal_with_message(current_proposal, message);
        $("#send-proposal-event").modal('hide');
    });
});
