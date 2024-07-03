// frontend/src/components/GraphContainer.js
import React from 'react';
import Graph from './Graph';

const GraphContainer = ({ graphData, onNodeClick, searchedNode, circularEdges, selectedProject, isTitleNodesOnly, isLevelWiseArranged }) => {
  const graphRef = React.useRef();

  return (
    <div id="graph-container" style={{ overflow: 'auto', width: '100%', height: '100%' }}>
      <Graph
        data={graphData}
        onNodeClick={onNodeClick}
        ref={graphRef}
        highlightedNode={searchedNode}
        circularEdges={circularEdges}
        selectedProject={selectedProject}
        isTitleNodesOnly={isTitleNodesOnly}
        isLevelWiseArranged={isLevelWiseArranged}
      />
    </div>
  );
};

export default GraphContainer;
