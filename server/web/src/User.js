import React  from 'react'
import {Link} from 'react-router'
import {ListView, AjaxAdapter} from './TinyListView'

var adapter = new AjaxAdapter({
	url: '/Api/User/getPage'
});

adapter.getView = function(position) {
	var item = this.getItem(position);

	if(item == null) {
		return (<div></div>);
	} else {
		var date = new Date();
		date.setTime(item.createdTime);
		return (
			<div className="listItem">
				<div className="row">
					<div className="col-xs-1"><b>#{item._id}</b></div>
					<div className="col-xs-3">{item.name}</div>
					<div className="col-xs-3">{date.toLocaleString()}</div>
				</div>
			</div>
		);
	}

};

export default class _ extends React.Component {
	constructor(props) {
		super(props);
		this.state = {item: {}};
		this.req = null;
	}

	componentDidMount() {
	}

	componentWillUnmount() {
		adapter.refresh();
	}

	onFileChange(event) {
		this.state.item.fileName = event.target.files[0].name;
		this.setState();
	}

	onDeleteUser() {
		$.post("/Api/User/deleteDoc", {_id: this.state.item._id}, (js) => {
			if(js != null && js.deleted) {
				$(this.refs.newCountDialog).modal('hide');
		    	adapter.refresh();
			}
		}, 'json');
	}

	onSaveChange() {
		var url = "/Api/User/" + (this.state.item._id ? "updateDoc" : "newDoc");
		$.ajax({
		    url: url,
		    type: 'POST',
		    data: JSON.stringify(this.state.item),
		    contentType: 'application/json; charset=utf-8',
		    dataType: 'json',
		    success: (msg) => {
		    	$(this.refs.newCountDialog).modal('hide');
		    	adapter.refresh();
		    }
		});
	}

	onClickItem(position) {
		var item = $.extend({}, adapter.getItem(position), {password: ""});
		this.setState({item: item});
		$(this.refs.newCountDialog).modal('show');
	}

	onItemChange(event) {
		this.state.item[event.target.name] = event.target.value;
		this.setState();
	}

	newCount() {
		var item = this.state.item;
		return (
		<div ref="newCountDialog" className="modal fade" tabindex="-1" role="dialog">
		  <div className="modal-dialog modal-large" role="document">
		    <div className="modal-content">
				<div className="modal-body">
					<p className="input-group input-group-md">
						<span className="input-group-addon">Name:</span>
					  	<input name="name" value={item.name} onChange={this.onItemChange.bind(this)} type="text" className="form-control" placeholder="Name" />
					</p>

					<p className="input-group input-group-md">
						<span className="input-group-addon">Password:</span>
					  	<input name="password" value={item.password} onChange={this.onItemChange.bind(this)} type="password" className="form-control" placeholder="Password" />
					</p>

				<div className="modal-footer">
					<button type="button" className="btn btn-danger pull-left" onClick={this.onDeleteUser.bind(this)}>Delete</button>

			        <button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="button" className="btn btn-primary" onClick={this.onSaveChange.bind(this)}>Save changes</button>
			    </div>

				</div>
		    </div>
		  </div>
		</div>
		);
	}

	popNewCountDialog() {
		this.setState({item: {name: "", password: ""}});
		$(this.refs.newCountDialog).modal('show');
	}

	render() {
    	return (
<div className="container" id="container_orders">
	<div className="toolbox">
<button onClick={this.popNewCountDialog.bind(this)} type="button" className="btn btn-success pull-right" aria-label="Left Align">
  <span className="glyphicon glyphicon-plus" aria-hidden="true"></span>
</button>
	</div>
	<ListView
		ref="listView"
		adapter={adapter}
		outterScroll={true}
		onItemClick={this.onClickItem.bind(this)}
	/>
	{this.newCount()}
</div>
        );
    }

};
