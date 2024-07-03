import React from 'react';
import { Link } from 'react-router-dom';
import backgroundImage from '../assets/img.png';

const Home = () => {
  const backgroundStyle = {
    backgroundImage: `url(${backgroundImage})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    minHeight: '100vh',
    width: '100vw',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'flex-end', // Changed to align content to the bottom
    color: 'white',
    position: 'relative',
  };

  const overlayStyle = {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  };

  const contentStyle = {
    position: 'relative',
    zIndex: 1,
    textAlign: 'center',
    marginBottom: '3%', // Add some bottom margin to lift it slightly from the bottom
  };

  const titleStyle = {
    fontSize: '3.5em',
    marginBottom: '30px',
    textShadow: '2px 2px 4px rgba(0,0,0,0.5)',
    fontWeight: 'bold',
    letterSpacing: '2px',
  };

  const buttonStyle = {
    display: 'inline-block',
    padding: '15px 30px',
    margin: '20px 0',
    backgroundColor: 'rgba(76, 175, 80, 0.8)',
    color: 'white',
    textDecoration: 'none',
    borderRadius: '30px',
    fontSize: '18px',
    fontWeight: 'bold',
    textTransform: 'uppercase',
    letterSpacing: '1px',
    transition: 'all 0.3s ease',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    border: '2px solid white',
  };

  return (
    <div style={backgroundStyle}>
      <div style={overlayStyle}></div>
      <div style={contentStyle}>
        <h1 style={titleStyle}>Welcome to Dependency Graph Visualizer</h1>
        <Link
          to="/upload"
          style={buttonStyle}
          onMouseOver={(e) => {
            e.target.style.backgroundColor = 'rgba(76, 175, 80, 1)';
            e.target.style.boxShadow = '0 6px 8px rgba(0,0,0,0.2)';
          }}
          onMouseOut={(e) => {
            e.target.style.backgroundColor = 'rgba(76, 175, 80, 0.8)';
            e.target.style.boxShadow = '0 4px 6px rgba(0,0,0,0.1)';
          }}
        >
          Upload Dependency File
        </Link>
      </div>
    </div>
  );
};

export default Home;