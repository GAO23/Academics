$('#logout')[0].addEventListener("click", onClick);



function onClick(event){
    event.preventDefault();
    console.log("fired");
    $('#hidden_log_out_form')[0].submit();
}