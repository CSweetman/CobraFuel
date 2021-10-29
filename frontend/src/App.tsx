import React from 'react';
import {
  Switch,
  Route,
  HashRouter
} from "react-router-dom";
import { Home } from './compoents/Home';
import { Room } from './compoents/Room';


function App() {
  return (
    <HashRouter>
      <Switch>
        <Route path="/:roomCode">
          <Room />
        </Route>
        <Route path="/">
          <Home />
        </Route>
      </Switch>
    </HashRouter>
  );
}

export default App;
