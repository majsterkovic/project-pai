import React from 'react';
import { RouterProvider } from 'react-router-dom';
import 'antd/dist/reset.css';
// import './App.css';
import router from './router';
import Navbar from "./Navbar";


function App() {
  return (
      <div>
          <Navbar/>
        <RouterProvider router={router}/>
      </div>
  );
}

export default App;
