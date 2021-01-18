// for protecting the routes

function not_authen_redirect(req, res, next){
    if(req.isAuthenticated()){
        return  next();
    }
    return res.json({status:"ERROR"});
}

function authen_redirect(req, res, next){
    if(req.isAuthenticated()){
        return res.json({status:"ERROR"});
    }
    next();
}


module.exports = {not_authen_redirect : not_authen_redirect,
    authen_redirect: authen_redirect};