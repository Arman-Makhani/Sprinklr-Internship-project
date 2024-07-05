/* frontend/src/pages/GraphPage.js */
import React, { useEffect } from 'react';
import Graph from '../components/Graph';

const GraphPage = ({ onNodeClick, graphData, onTitleNodeClick }) => {
  useEffect(() => {
  }, [graphData]);

  const handleNodeClick = (nodeId) => {
    if (nodeId.includes('Group:')) {
      onTitleNodeClick(nodeId);
    } else {
      onNodeClick(nodeId);
    }
  };

  return <Graph data={graphData} onNodeClick={handleNodeClick} />;
};
export default GraphPage;
