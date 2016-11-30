import React  from 'react'
import {Link, browserHistory} from 'react-router'
import {ListView, AjaxAdapter} from './TinyListView'

var adapter = new AjaxAdapter({
	url: '/Api/InventoryCount/getPage'
});

adapter.getView = function(position) {
	var item = this.getItem(position);

	if(item == null || this._renderListView == null) {
		return (<div></div>);
	} else {
		return this._renderListView(item);
	}

};

export default class _ extends React.Component {
	constructor(props) {
		super(props);
		this.state = {item: {}};
		this.req = null;
	}

	onEdit(item, evt) {
		evt.stopPropagation();

		var item = $.extend({}, item);
		if(item.name == null) item.name = "";

		this.setState({item: item});
		$(this.refs.newCountDialog).modal('show');
	}

	renderListView(item) {
		var date = new Date();
		date.setTime(item.createdTime);

		return (
			<div className="listItem">
				<div className="row">
					<div className="col-xs-1"><b>#{item._id}</b></div>
					<div className="col-xs-2">{item.productCount}</div>
					<div className="col-xs-4">{item.name}</div>
					<div className="col-xs-3">{date.toLocaleString()}</div>
					<div className="col-xs-2"><button type="button" onClick={this.onEdit.bind(this, item)} className="btn btn-info pull-right"><span className="glyphicon glyphicon-edit"></span> Edit</button></div>
				</div>
			</div>
		);
	}

	componentDidMount() {
		$.get("/Api/User/getDocs", (js) => {
			this.setState({userList: js.docs});
		}, 'json');
	}

	componentWillMount() {
		adapter._renderListView = this.renderListView.bind(this);
	}

	componentWillUnmount() {
		adapter._renderListView = null;
		adapter.refresh();
	}

	onFileChange(event) {
		this.state.item.fileName = event.target.files[0].name;
		this.setState();
	}

	onDelete() {
		$.post("/Api/InventoryCount/deleteDoc", {_id: this.state.item._id}, (js) => {
			if(js != null && js.deleted) {
				$(this.refs.newCountDialog).modal('hide');
		    	adapter.refresh();
			}
		}, 'json');
	}

	onSaveChange() {
		var data = new FormData();
		if(this.refs.uploadFile.files.length)
	    	data.append('file', this.refs.uploadFile.files[0]);

	    data.append('item', JSON.stringify(this.state.item));

	    $.ajax({
	        url: '/Api/InventoryCount/updateDoc',
	        data: data,
	        processData: false,
	        contentType: false,
	        type: 'POST',
	        success: (data) => {
	        	$(this.refs.newCountDialog).modal('hide');
	        	adapter.refresh();
	        }
	    });
	}

	onClickItem(position) {
		var item = adapter.getItem(position);
		if(item == null) return;

		browserHistory.push(`/web/inventory-count-report/${item._id}`);
	}

	onItemNameChange(event) {
		this.state.item.name = event.target.value;
		this.setState();
	}

	onCheckTeam(event) {
		var idx = parseInt(event.target.name.substr(6));
		this.state.item.employeeList[idx].teamId = event.target.value;
		this.setState();
	}

	onSelectUser(event) {
		var idx = parseInt(event.target.value);
		if(idx <= 0) return;
		event.target.value = "";

		var user = this.state.userList[idx - 1];
		var employeeList = this.state.item.employeeList || [];
		for(var employee of employeeList) {
			if(employee._id == user._id) return;
		}

		employeeList.push({_id: user._id, name: user.name, teamId: 0});
		this.state.item.employeeList = employeeList;
		this.setState();
	}

	newCount() {
		var item = this.state.item;
		var employeeList = item.employeeList || [];
		var employeeListView = [];
		var idx = 0;
		for(var employee of employeeList) {
			employeeListView.push(
			<li className="list-group-item">
				<div className="row">
					<div className="col-xs-4">{employee.name}</div>
					<div className="col-xs-4"><label><input type="radio" name={"RGIVC_" + idx} value="1" checked={employee.teamId == 1} onChange={this.onCheckTeam.bind(this)} /> Team A</label></div>
					<div className="col-xs-4"><label><input type="radio" name={"RGIVC_" + idx} value="2" checked={employee.teamId == 2} onChange={this.onCheckTeam.bind(this)} /> Team B</label></div>
				</div>
			</li>
			);
			idx++;
		}

		var userListView = [];
		var userList = this.state.userList || [];
		var idx = 1;
		for(var user of userList) {
			userListView.push(
				<option value={idx}>{user.name}</option>
			);
			idx++;
		}

		return (
		<div ref="newCountDialog" className="modal fade" tabindex="-1" role="dialog">
		  <div className="modal-dialog modal-large" role="document">
		    <div className="modal-content">
				<div className="modal-body">
					<p className="input-group input-group-md">
						<span className="input-group-addon" id="sizing-addon1">Name:</span>
					  	<input value={item.name} onChange={this.onItemNameChange.bind(this)} type="text" className="form-control" placeholder="Any Name" aria-describedby="sizing-addon1" />
					</p>

					<p>
						<div className="row">
							<div className="col-xs-6">
								<label className="btn btn-default btn-md">
								    Browse For Product Excel <input ref="uploadFile" type="file" style={{display: 'none'}} onChange={this.onFileChange.bind(this)} />
								</label>
							</div>
							<div className="col-xs-6"><h4>{item.fileName}</h4></div>
						</div>
					</p>

					<p>
						<select className="form-control" onChange={this.onSelectUser.bind(this)}>
						   <option value="">Add User</option>
						   {userListView}
						 </select>
					</p>

					<p>
					<ul className="list-group">{employeeListView}</ul>

					</p>

				<div className="modal-footer">
					<button type="button" className="btn btn-danger pull-left" onClick={this.onDelete.bind(this)}>Delete</button>

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
		this.setState({item: {name:""}});
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
