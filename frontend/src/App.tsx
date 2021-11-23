import React from 'react';
import { HashRouter, Route, Routes} from 'react-router-dom';
import { Home } from './components/Home';
import { Room } from './components/Room';


function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/:roomCode" element={<Room />} />
        <Route path="/" element={<Home />} />
      </Routes>
    </HashRouter>
  );
}

export default App;
