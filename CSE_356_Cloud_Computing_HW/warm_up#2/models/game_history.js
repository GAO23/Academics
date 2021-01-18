var mongoose = require('mongoose');


var game_history = new mongoose.Schema({
    games:   [{
        id: Number,
        start_date: {type: Date, required: true, default: Date.now}
    }],
    _userId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' }
});

module.exports = mongoose.model('Game_History', game_history);