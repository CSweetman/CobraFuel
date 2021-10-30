import React from 'react';
import { HashRouter, Route, Switch } from 'react-router-dom';

import { Home } from './components/Home';
import { Room } from './components/Room';


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
