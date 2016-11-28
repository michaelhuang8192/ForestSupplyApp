
function PK() {
	if(!(this instanceof PK)) {
		console.log("PK");
		return new PK();
	}
	BASE.call(this);
}

function BASE() {
	if(!(this instanceof BASE)) {
		console.log("BASE");
		return new BASE();
	}

	console.log(">>>BASE");
}

PK.prototype = Object.create(BASE.prototype);
PK.prototype.constructor = PK;


new PK();