import React  from 'react'
import { Link, browserHistory } from 'react-router'
import jsCookie from 'js-cookie'

export default class _ extends React.Component {
	constructor(props) {
		super(props);
		this.state = {item: {}};
		this.req = null;
	}

	logout() {
		jsCookie.remove('serverAuth');
		
	}

	render() {
    return (
<div className="container">
  <ul className="nav nav-tabs">
    <li><Link activeClassName="active" to="/web/home">Home</Link></li>
    <li><Link to="/web/user">User</Link></li>
    <li><Link to="/web/inventory-count">Inventory Count</Link></li>
    <li><a href="/web/home" onClick={this.logout.bind(this)}>Logout</a></li>
  </ul>
</div>
)
	}

}