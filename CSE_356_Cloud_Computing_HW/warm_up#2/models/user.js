var mongoose = require('mongoose');


var userSchema = new mongoose.Schema({
    username: String,
    email: { type: String, unique: true },
    roles: [{ type: 'String' }],
    isVerified: { type: Boolean, default: false },
    password: String,
    passwordResetToken: String,
    passwordResetExpires: Date
});

module.exports = mongoose.model('User', userSchema);