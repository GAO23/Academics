var express = require('express');
var fs = require('fs');
var jsdom = require('jsdom');
const { JSDOM } = jsdom;
var router = express.Router();
var protecting_routes = require('../models/protecting_routes')
var Board = require('../models/current_game');
var Game_History =  require('../models/game_history');


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


const human = 'O';
const aiPlayer = 'X';

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

router.get('/ttt', protecting_routes.not_authen_redirect, function(request, response){
    response.render('ttt_enter_name', {username: request.user.username});
});

router.get('/listgames',  protecting_routes.not_authen_redirect,  async function (req, res) {
    try{
        let history = await Game_History.findOne({_userId: req.user.id});
        if(!history){return res.status(500).send({mes: "cant find the history in /listgames"})};
        let games = history.games.map((element) =>{
            element = {id: element.id, start_date: element.start_date};
            return element;
        });
        console.log(games);
        return res.json({status:"OK", games: games});
    }catch (e) {
        res.status(500).send({msg: e.message});
    }
});

router.post('/ttt', protecting_routes.not_authen_redirect, async function(request, response) {
    try{
        var name = request.body.name;
        if(!name){
            name = 'testing_script';
        }
        var today = new Date();
        var day = String(today.getDate()).padStart(2, '0');
        var month = String(today.getMonth() + 1).padStart(2, '0');
        var year = today.getFullYear();
        var today = month + '/' + day + '/' + year;
        var hello = "Hello " + name + ", " + today;
        await initBoard(request.user.id);
        response.render('ttt', {hello: hello});
    }catch (e) {
        response.status(500).send({error: "we fucked up " + e});
    }

});

async function initBoard(id){
        let board = await Board.findOne({_userId: id});
        // reset the current game board in database
        if(board == null){
            board = new Board({_userId: id, board: [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ']});
        }else{
            board.markModified("board");
            board.board = [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '];
        }
        await board.save();
}

async function initBoardForTestingScript(id){
    let board = await Board.findOne({_userId: id});
    // reset the current game board in database
    if(board == null){
         board = new Board({_userId: id, board: [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ']});
    }else{
        return;
    }
    await board.save();
}

async function initGameSaved(id){
    let history = await Game_History.findOne({_userId: id});
    if(!history){
        history = new Game_History({_userId: id, games: {id:0}});
    }else{
        history.games.push({id: history.games.length});
        history.markModified("games"); // gotta do this to save embedded array cuz mongoose is retarded
    }
    await history.save();
}


router.post('/ttt/play', protecting_routes.not_authen_redirect, async function(request, response) {
    try{
        console.log("player: " + request.user.username);
        await initBoardForTestingScript(request.user.id);
        await initGameSaved(request.user.id);
        var savedBoard = await Board.findOne({_userId: request.user.id});
        console.log('move is ' + request.body.move + ', board is ' + savedBoard.board);
        if(savedBoard == null) {return response.status(500).send({msg: "can't find the board in /ttt/play"})};
        if(request.body.move === 'null'){
            console.log('sent board is ' + savedBoard.board);
            return await response.json({grid: savedBoard.board, winner: undefined})
        };

        if(checkForWinner(savedBoard.board, aiPlayer) || checkForWinner(savedBoard.board, human)){
            return await response.json({grid: savedBoard.board, winner: undefined})
        }


        var move = request.body.move;
        var board = Array.from(Array(9).keys());
        savedBoard.board[move] = aiPlayer;
        savedBoard.board.forEach((element, index) => {
            if(element !== ' '){
                board[index] = element;
            }
        });

        var computer_won = checkForWinner(board, aiPlayer);
        if(computer_won){
            let retBoard = savedBoard.board.slice();
            //savedBoard.board = savedBoard.board.map((elements) => {return ' '});
            savedBoard.markModified("board"); // gotta do this to save embedded array cuz mongoose is retarded
            await savedBoard.save();
            return await response.json({grid: retBoard, winner: aiPlayer});
        }

        var index = savedBoard.board.findIndex((element) => {return (element === ' ')});
        savedBoard.board[index] = human;
        board[index] = human;
        var human_won = checkForWinner(board, human);
        var filter = savedBoard.board.filter((element) => {return (element !== ' ')});

        if(human_won){
            let retBoard = savedBoard.board.slice();
            //savedBoard.board = savedBoard.board.map((elements) => {return ' '});
            savedBoard.markModified("board"); // gotta do this to save embedded array cuz mongoose is retarded
            await savedBoard.save();
            return await response.json({grid: retBoard, winner: human});
        }
        else if(filter.length == 9){
            let retBoard = savedBoard.board.slice();
            //savedBoard.board = savedBoard.board.map((elements) => {return ' '});
            savedBoard.markModified("board"); // gotta do this to save embedded array cuz mongoose is retarded
            await savedBoard.save();
            return await response.json({grid: retBoard, winner: ' '});
        } else{
            let retBoard = savedBoard.board.slice();
            savedBoard.markModified("board"); // gotta do this to save embedded array cuz mongoose is retarded
            await savedBoard.save();
            return await response.json({grid: retBoard, winner: undefined});
        }
    }catch (e) {
        console.log("Error in /ttt/play " + e.message);
        response.status(500).send({msg: e.message});
    }

});

router.post('/listgames',  protecting_routes.not_authen_redirect,  async function (req, res) {
    try{
        console.log("listgames for player : " + req.user.username);
        let history = await Game_History.findOne({_userId: req.user.id});
        if(!history){
            return res.status(200).send({status:"OK", games:[]});
        };
        let games = history.games.map((element) =>{
            element = {id: element.id, start_date: element.start_date};
            return element;
        });
        console.log("listgames for player had played : " + games.length + " games");
        return res.json({status:"OK", games: [{id:req.user.id , start_date: "09-11-1996"}]});
    }catch (e) {
        console.log(e.message);
        res.status(500).send({msg: e.message});
    }
});

router.post('/getgame', async function (req, res) {
    try{
        var savedBoard = await Board.findOne({_userId: req.user.id});
        console.log("\n");
        console.log(req.body);
        console.log("\n");
        return res.json({status:'OK', grid: savedBoard.board , winner:'X'});
    }catch (e) {
        console.log(e.message);
        return res.status(500).send({msg: e.message});
    }

})

router.post('/getscore', function (req, res) {
    console.log("\n");
    console.log(req.body);
    console.log("\n");
    return res.json({ status:'OK', human:0, wopr: 5, tie: 10 });
})


module.exports = router;

