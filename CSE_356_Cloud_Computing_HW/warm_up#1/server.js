const http = require("http");
// const hostname = '0.0.0.0';
// const port = 3000;
var express = require('express');
var bodyParser = require('body-parser');
var fs = require('fs');
var jsdom = require('jsdom');
const { JSDOM } = jsdom;
// only if we want short varible for window and change the global dom
// const { document } = (new JSDOM('')).window;
// global.document = document;
var app = express();
// parse application/json
app.use(bodyParser.json());
app.use(express.urlencoded({extended: true}));

const winningRows = [
	[0, 1, 2],
	[3, 4, 5],
	[6, 7, 8],
	[0, 3, 6],
	[1, 4, 7],
	[2, 5, 8],
	[0, 4, 8],
	[6, 4, 2]
]

var board; // build array from 0 to 9

const human = 'O';
const aiPlayer = 'X';
var hello;

function checkForWinner(board, player){
  let moves = board.reduce((arg1, arg2, arg3) => (arg2 === player) ? arg1.concat(arg3) : arg1, []);
  for(let [index, row] of winningRows.entries()){
    if(row.every(elem => moves.indexOf(elem) > -1)){
      var gameOver = {index: index, player: player};
      break;
    }
  }
  return gameOver;
}

app.get('/ttt', function(request, response){
	response.sendFile('./index.html', {root: __dirname});

});

app.get('/style.css', function(request, response) {
  response.sendFile('./style.css', {root: __dirname});
});

// app.get('/ttt', function(request, response) {
// 	var name = request.query.name;
// 	var today = new Date();
// 	var day = String(today.getDate()).padStart(2, '0');
// 	var month = String(today.getMonth() + 1).padStart(2, '0');
// 	var year = today.getFullYear();
// 	var today = month + '/' + day + '/' + year;
// 	this.hello = "Hello " + name + ", " + today;
//   response.send(modify_html_name('./ttt.html', this.hello));
// });

app.post('/ttt', function(request, response) {
	var name = request.body.name;
	var today = new Date();
	var day = String(today.getDate()).padStart(2, '0');
	var month = String(today.getMonth() + 1).padStart(2, '0');
	var year = today.getFullYear();
	var today = month + '/' + day + '/' + year;
	this.hello = "Hello " + name + ", " + today;
	board = Array.from(Array(9).keys());
  response.send(modify_html_name('./ttt.html', this.hello));
});


app.post('/ttt/play', function(request, response) {
	var grid = request.body.grid;
	for(let i = 0; i < grid.length; i++){
				board[i] = grid[i];
	}
	var human_won = checkForWinner(board, human);
	if(human_won){
		response.json({grid: grid, winner: human});
		return;
	}

	for(let i = 0; i < grid.length; i++){
		if(!(grid[i] === human) && !(grid[i] === aiPlayer)){
			grid[i] = aiPlayer;
			board[i] = aiPlayer;
			break;
		}

	}

	var computer_won = checkForWinner(board, aiPlayer);
	if(computer_won){
		response.json({grid: grid, winner: aiPlayer});
	}else{
		response.json({grid: grid, winner: ""});
	}

});


app.get('/ttt.css', function(request, response) {
  response.sendFile('./ttt.css', {root: __dirname});
});


app.get('/ttt.js', function(request, response) {
  response.sendFile('./ttt.js', {root: __dirname});
});



function modify_html_name(html, name){
	var htmlSource = fs.readFileSync(html, "utf8");
	const dom = new JSDOM(htmlSource);
	dom.window.document.querySelector("#name").textContent = name; // "Hello world"
	// fs.writeFile('ttt.html', dom.window.document.documentElement.outerHTML); // no need to write out
	return dom.window.document.documentElement.outerHTML;
}
// app.listen(port, hostname, function(){
//	console.log("running");
// });

 app.listen(3000, function(){
 	console.log("running");
 });
