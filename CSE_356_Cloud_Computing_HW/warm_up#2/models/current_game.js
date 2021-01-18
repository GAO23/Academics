var mongoose = require('mongoose');


var current_board = new mongoose.Schema({
    board: [{
        type: String
    }],
    _userId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' }
});

module.exports = mongoose.model('Board', current_board);