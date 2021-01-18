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
var board;
const human = 'O';
const aiPlayer = 'X';
var gameOver = false;

// $.ajax({
//   url: '/user_date',
//   type: 'get',
//   dataType: 'JSON',
//   success: function(data){
//     $("#name")[0].innerHTML = data.name;
//   }
  // complete: function(data) {
  //   var hello = data.responseJSON.hello;
  //   $("#name_date")[0].innerHTML = hello;
  // }
// });

function displayWin(player){
	var cells = $(".cell");
	for(var i = 0; i < cells.length; i++){
		cells[i].removeEventListener('click', onCellClick, false);
	}
  $("#winner")[0].innerHTML = player + " won";
}

function onCellClick(event){
  var cellId = event.target.id; // get teh reference to the clicked element
  var cell = $("#"+cellId)[0];
  if(cell.innerText === '' && !gameOver){
      cell.innerText = human;
      board[cellId] = human;
  }
	var json_array = [];
	for(key in board){
		json_array.push(board[key]);
	}
  $.ajax({
            type: "POST",
            url: "/ttt/play",
            timeout: 2000,
            dataType:'json',
            data: {grid:json_array},
            success: function(data) {
							var grid = data.grid;
							var cells = $(".cell");
							for(var i = 0; i < grid.length; i++){
								if((grid[i] === human) || (grid[i] === aiPlayer)){
									board[i] = grid[i];
									cells[i].innerText = grid[i];
								}
							}
              var winner = data.winner;
							if(winner === human){
								displayWin(human);
							}else if(winner === aiPlayer){
								displayWin(aiPlayer);
							}
            }
        });
}

function start(){
  board = Array.from(Array(9).keys()); // build array from 0 to 9

  var cells = $(".cell");
  for(var i = 0; i < cells.length; i++){
    cells[i].innerText = '';
    cells[i].addEventListener('click', onCellClick, false);
  }
}

 start();
