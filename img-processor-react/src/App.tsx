import React from "react";
import "./App.css";
import ImageProcessor from "./components/image_processor";
import "bootstrap/dist/css/bootstrap.min.css";
import "react-toastify/dist/ReactToastify.css";

function App() {
  return (
    <div className="App">
      <header
        className="p-3 bg-secondary text-white text-center"
        style={{ fontFamily: 'Inter, sans-serif', fontSize: '2.5rem' }}
      >
        <h1>Image Processor</h1>
      </header>
      <main className="container mt-4">
        <ImageProcessor />
      </main>
    </div>
  );
}

export default App;
