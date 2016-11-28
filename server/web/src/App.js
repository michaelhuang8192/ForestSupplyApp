import React  from 'react'
import { formatPattern, Link } from 'react-router'
import jsCookie from 'js-cookie'

import Header from './Header'
import {Footer} from './Footer'
import ApplicationContext from './ApplicationContext'


class LoginPanel extends React.Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.req = null;
		this.loginDialog = null;
		this._checkLogin = this._checkLogin.bind(this);
		this.checkLoginTimeout = null;
	}

	clear() {
		if(this.checkLoginTimeout) {
			clearTimeout(this.checkLoginTimeout);
			this.checkLoginTimeout = null;
		}
		if(this.req != null) {
			this.req.abort()
			this.req = null;
		}
	}

	checkLogin() {
		this.clear();
		this._checkLogin();
	}

	_checkLogin() {
		this.checkLoginTimeout = null;
		this.req = $.get("/Api/Auth/checkAuth", (js) => {
			if(js != null && js.success && js.authFailed) {
				if(!this.loginDialog.hasClass("in")) this.loginDialog.modal('show');
			} else {
				this.loginDialog.modal('hide');
			}
		}, 'json')
		.always(() => {
			this.req = null;
			this.checkLoginTimeout = setTimeout(this._checkLogin, 5000);
		});
	}

	onLogin() {
		var userPassword = this.refs.userPassword.value;
		this.refs.userPassword.value = "";

		$.post(
			"/Api/Auth/getAuth",
			{
				userName: this.refs.userName.value,
				userPassword: userPassword
			},
			(js) => {
				if(js != null && js.success && !js.authFailed)
					this.loginDialog.modal('hide');
			},
			"json"
		)
	}

	componentDidMount() {
		this.loginDialog = $(this.refs.loginDialog).modal({
			keyboard: false,
			backdrop: 'static',
			show: false
		});

		this.checkLogin();
	}

	componentWillUnmount() {
		this.clear();
	}

	render() {
		return (
		<div ref="loginDialog" className="modal fade" tabindex="-1" role="dialog">
			  <div className="modal-dialog modal-large" role="document">
		    <div className="modal-content">
			    <div className="modal-header">
				  	<h4 className="modal-title">Log In</h4>
				 </div>

				<div className="modal-body">
					<p className="input-group input-group-md">
						<span className="input-group-addon">Name:</span>
					  	<input ref="userName" name="name" type="text" className="form-control" placeholder="User Name" />
					</p>

					<p className="input-group input-group-md">
						<span className="input-group-addon">Password:</span>
					  	<input ref="userPassword" name="password" type="password" className="form-control" placeholder="User Password" />
					</p>

				<div className="modal-footer">
			        <button type="button" className="btn btn-primary" onClick={this.onLogin.bind(this)}>Login</button>
			    </div>

				</div>
		    </div>
		  </div>
		</div>
		);
	}


}


export default class _ extends React.Component {
	constructor(props) {
		super(props);
		this.state = {};
	}

	render() {
		return (
			<div>
				<div id="header">
					<Header />
				</div>
				<div id="app_body">{this.props.children}</div>
				<div id="footer">
					<Footer />
				</div>
				<LoginPanel />
			</div>
		)
	}

}

