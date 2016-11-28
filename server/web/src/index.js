import React  from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, browserHistory, IndexRoute, Redirect } from 'react-router'
import App from './App'
import Home from './Home'
import InventoryCount from './InventoryCount'
import User from './User'

$(function() {


ReactDOM.render((
<Router history={browserHistory}>
  <Route path="/web/" component={App}>
    <IndexRoute component={Home}/>
    <Route path="home" component={Home}/>
    <Route path="user" component={User}/>
    <Route path="inventory-count" component={InventoryCount}/>
  </Route>
  <Redirect from="/" to="/web/" />
  <Redirect from="/web" to="/web/" />
</Router>

), document.getElementById('app'));


});
