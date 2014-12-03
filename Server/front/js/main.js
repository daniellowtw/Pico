var config = {
    apiAddr: ""
}

function sendReq(){
    var key = $('#revKey').val();
    $.post(config.apiAddr + '/api/delete', {'revKey': key}, function(data, textStatus, xhr) {
        // TODO: handle failure case
        $('#result').css('display','block').html(data)
        console.log(data, textStatus)
    });
}

// $('#disableBtn').click(function(){alert(0)})
// jQuery(document).ready(function($) {
//     $('#disableBtn').click(sendReq)
// });
