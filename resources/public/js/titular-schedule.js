function send_proposal_with_message(snd, prop, msg) {
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

$(document).ready(function(){
	// show modal
    $("#send-proposal").click(function(){
        $("#send-proposal-event").modal("show");
    });
    // send proposal 
    $("#send-activity").click(function(){
        var proposal = generate_schedule_proposal();
        var titular = "titular";
        var message = $("#message").val();
        send_proposal_with_message(titular, proposal, message);
    });
    
});
