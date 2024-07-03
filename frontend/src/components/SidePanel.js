import React from 'react';
import './SidePanel.css';

const SidePanel = ({ selectedNode, childrenList, onSelectChild, onToggleGraph, isShowingGraph }) => {
  return (
    <div className="side-panel">
      <h2>Node: {selectedNode || 'None selected'}</h2>
      <button
        onClick={onToggleGraph}
        title={isShowingGraph ? "Hide the current graph and show all configurations" : "Show the graph for the selected configuration"}
      >
        {isShowingGraph ? 'Hide Graph' : 'Show Graph'}
      </button>
      <p>
        <strong>Note:</strong> Red indicates unresolved dependencies,
        green represents dependency constraints, and
        yellow represents omitted dependencies.
      </p>
      <h3>Children:</h3>
      {childrenList.length > 0 ? (
        <ul>
          {childrenList.map((child, index) => (
            <li key={index}>
              <button
                onClick={() => onSelectChild(child)}
                title={`Select ${child} as the current node`}
              >
                {child}
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p>No children available.</p>
      )}
    </div>
  );
};

export default SidePanel;