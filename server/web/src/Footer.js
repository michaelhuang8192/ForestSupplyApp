import React  from 'react'

var Footer = React.createClass({
	render() {
		return (
			<div className="container text-center">
  				<div>&copy; {new Date().getFullYear()} TinyAppsDev</div>
  			</div>
		);
	}
});

module.exports = {
	Footer: Footer
};