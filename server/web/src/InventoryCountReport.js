import React  from 'react'
import {Link, browserHistory} from 'react-router'
import {ListView, ArrayAdapter} from './TinyListView'


export default class _ extends React.Component {
	constructor(props) {
		super(props);
		this.state = {item: {}};
		this.req = null;

		this.adapter = new ArrayAdapter();
		this.adapter.getView = (position) => {
			var item = this.adapter.getItem(position);

			if(item == null) {
				return (<div></div>);
			} else {
				return (
					<div className={"listItem " + (item.matched ? "listItem_matched" : "")}>
						<div className="row">
							<div className="col-xs-2"><b>{item.num}</b></div>
							<div className="col-xs-5">{item.team1}</div>
							<div className="col-xs-5">{item.team2}</div>
						</div>
					</div>
				);
			}

		};
	}

	componentDidMount() {
		this.loadItems();
	}

	loadItems() {
		$.get(
			'/Api/InventoryCount/getCountReport?inventoryCountId=' + this.props.params.id,
			(js)=> {
				if(js == null || js.success != true) return;
				this.docs = js.docs;
				this.filterItem();			
			},
			'json'
		);
	}

	filterItem() {
		if(!this.mode)
			this.adapter.setData(this.docs);
		else {
			var docs = [];
			for(var doc of this.docs) {
				if(doc.matched) continue;
				docs.push(doc);
			}
			this.adapter.setData(docs);
		}
	}

	onFilterItem(mode, evt) {
		evt.stopPropagation();

		this.mode = mode;
		if(this.docs == null) {
			this.loadItems();
			return;
		}
		this.filterItem();
	}

	onRefresh() {
		this.loadItems();
	}

	componentWillMount() {

	}

	componentWillUnmount() {
	}


	render() {
    	return (
<div className="container" id="container_inv_count_report">
	<div className="btn-toolbar toolbox">

<Link to="/web/inventory-count" className="btn btn-default">
  <span className="glyphicon glyphicon-arrow-left"></span>
</Link>

<a target="_blank" href={'/Api/InventoryCount/downloadCountReport?inventoryCountId=' + this.props.params.id} className="btn btn-success pull-right">
	  Download CSV<span className="glyphicon glyphicon-download-alt"></span>
</a>


<div className="btn-group pull-right">
  <button type="button" className="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
    Match <span className="caret"></span>
  </button>
  <ul className="dropdown-menu">
    <li><a href="#" onClick={this.onFilterItem.bind(this, 0)}>Show All</a></li>
    <li><a href="#" onClick={this.onFilterItem.bind(this, 1)}>Show Unmatched</a></li>
  </ul>
</div>

	<button onClick={this.onRefresh.bind(this)} type="button" className="btn btn-info pull-right">
	  Refresh <span className="glyphicon glyphicon-refresh"></span>
	</button>

	</div>

	<ListView
		ref="listView"
		adapter={this.adapter}
		outterScroll={true}
	/>
</div>
        );
    }

};
