var baseURL = "http://localhost:8080/ezScrum/resource/";
var export = function(){
	fetch(baseURL+"accounts").then(function(res){
		console.log(res);
	});
}