var gLogger = require('./Logger');

var UserError = exports.UserError = function(message) {
	if(!(this instanceof UserError)) return new UserError(message);

	this.message = message;
    this.stack = null;
    Error.captureStackTrace(this, UserError);
}

UserError.prototype = Object.create(Error.prototype);
UserError.prototype.name = "UserError";
UserError.prototype.constructor = UserError;


exports.UserErrorPromise = function(message) {
	return Promise.reject(new UserError(message));
}


exports.DefaultErrorHandler = (err, req, res, next) => {
	if(err instanceof UserError) {
		res.json({success: false, error: err.message});
	} else if(err == null || err.error == null) {
		res.json({success: false, error: "Unexpected Error"});
	} else if(err.error instanceof UserError) {
		res.json({success: false, error: err.error.message});
	} else if(err.error instanceof Error) {
		gLogger.error("DefaultErrorHandler", err.error.stack);
		res.json({success: false, error: "Unexpected Error"});
	} else {
		gLogger.error("DefaultErrorHandler", String(err));
		res.json({success: false, error: "Unexpected Error"});
	}
}
