import React  from 'react'
import { Link } from 'react-router'


export default class Home extends React.Component {
	constructor(props) {
		super(props);
		this.state = {counts: {}};
		this.req = null;
	}

	componentDidMount() {
	}

	componentWillUnmount() {
	}

	render() {
    	return (
<div className="container">
	<div className="panel panel-primary">

	  <div className="panel-heading">Server Info</div>
	  <div className="panel-body">
	  	<h3>ForestSupply V1.0</h3>
	  	
	  </div>
	 
	</div>
</div>
        );
    }



};
