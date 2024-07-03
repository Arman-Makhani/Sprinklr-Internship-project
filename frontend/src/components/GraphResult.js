import React, { useState, useEffect, useRef } from 'react';
import Graph from './Graph';
import SidePanel from './SidePanel';
import SearchBar from './SearchBar';
import './GraphResult.css';

const GraphResult = () => {
  const [graphData, setGraphData] = useState(null);
  const [titleNodes, setTitleNodes] = useState([]);
  const [circularDependencyNodes, setCircularDependencyNodes] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);
  const [childrenList, setChildrenList] = useState([]);
  const [isShowingGraph, setIsShowingGraph] = useState(false);
  const [searchedNode, setSearchedNode] = useState(null);
  const [showCircularDependencies, setShowCircularDependencies] = useState(false);
  const [circularEdges, setCircularEdges] = useState([]);
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [titleNodesForDependency, setTitleNodesForDependency] = useState([]);
  const [showTitleNodeSelector, setShowTitleNodeSelector] = useState(false);
  const [isTitleNodesOnly, setIsTitleNodesOnly] = useState(true);
  const [isGridArranged, setIsGridArranged] = useState(false);
  const [isLevelWiseArranged, setIsLevelWiseArranged] = useState(false);
  const [isGraphGenerated, setIsGraphGenerated] = useState(false);

  const [isArranged, setIsArranged] = useState(false);

  const searchBarRef = useRef();
  const graphRef = useRef();


  useEffect(() => {
    fetchTitleNodes();
  }, []);

  const fetchTitleNodes = async () => {
      try {
        const response = await fetch('/api/title-nodes');
        if (!response.ok) {
          throw new Error('Failed to fetch title nodes');
        }
        const data = await response.text();
        setTitleNodes(data);
        setGraphData(data);
        setSelectedNode(null);
        setChildrenList([]);
        setIsShowingGraph(false);
        setSearchedNode(null);
      } catch (error) {
        console.error('Error fetching title nodes:', error);
      }
    };

   const handleNodeClick = async (nodeId, project) => {
       console.log("1. handleNodeClick called with nodeId:", nodeId, "project:", project);

       let nodeName = nodeId;
       if (nodeId.includes('Group:')) {
         const parts = nodeId.split(',');
         nodeName = parts[0].split(':')[1].trim() + ':' + parts[1].split(':')[1].trim();
       }

       setSelectedNode(nodeId);
       console.log("2. selectedNode set to:", nodeId);
       await fetchChildren(nodeName, project);
       console.log("3. fetchChildren completed");
     };

   const fetchChildren = async (nodeId, project) => {
      console.log("4. fetchChildren started for nodeId:", nodeId, "project:", project);
      try {
        const url = `/api/children?title=${encodeURIComponent(nodeId)}&project=${encodeURIComponent(project || '')}`;
        console.log("5. Fetching from URL:", url);
        const response = await fetch(url);
        console.log("6. API response received:", response);
        if (!response.ok) {
          throw new Error('Failed to fetch children');
        }
        const children = await response.json();
        console.log("7. Children data parsed:", children);
        setChildrenList(children);
        console.log("8. childrenList state updated");
      } catch (error) {
        console.error('9. Error fetching children:', error);
        setChildrenList([]);
      }
    };
      const handleSearch = async (searchTerm) => {
          try {
            const titleNodesResponse = await fetch(`/api/title-nodes-for-dependency?dependency=${encodeURIComponent(searchTerm)}`);
            if (!titleNodesResponse.ok) {
              throw new Error('Failed to fetch title nodes for dependency');
            }
            const titleNodes = await titleNodesResponse.json();

            if (titleNodes.length > 1) {
              setTitleNodesForDependency(titleNodes);
              setShowTitleNodeSelector(true);
              setGraphData(null);
              setSearchedNode(searchTerm);
            } else {
              await fetchGraphData(searchTerm, titleNodes[0]);
            }
          } catch (error) {
            console.error('Error fetching title nodes for dependency:', error);
          }
        };

        const handleTitleNodeSelection = (titleNode) => {
          fetchGraphData(searchedNode, titleNode);
          setShowTitleNodeSelector(false);
        };

        const fetchGraphData = async (searchTerm, selectedTitleNode) => {
          try {
            const response = await fetch(`/api/search?term=${encodeURIComponent(searchTerm)}&project=${encodeURIComponent(selectedTitleNode)}`);
            if (!response.ok) {
              throw new Error('Failed to fetch graph data');
            }
            const data = await response.text();
            setGraphData(data);
            setSelectedNode(searchTerm);
            setSearchedNode(searchTerm);
            setIsShowingGraph(true);
            setIsGraphGenerated(true);
            setIsTitleNodesOnly(false);
            setIsGridArranged(false);
            setIsLevelWiseArranged(false);
            await fetchChildren(searchTerm, selectedTitleNode);
            setShowTitleNodeSelector(false);
          } catch (error) {
            console.error('Error fetching graph data:', error);
          }
        };


      const handleChildClick = (childNode, project) => {
        handleNodeClick(childNode, project);
        if (graphRef.current && graphRef.current.setFocusedNode) {
          graphRef.current.setFocusedNode(childNode);
        }
      };





  const handleToggleGraph = async () => {
      if (!isShowingGraph) {
        try {
          const response = await fetch(`/api/graph?focusNode=${encodeURIComponent(selectedNode)}&project=${encodeURIComponent(selectedProject)}`);
          if (!response.ok) {
            throw new Error('Failed to fetch graph data');
          }
          const data = await response.text();
          setGraphData(data);
          setIsShowingGraph(true);
          setIsTitleNodesOnly(false);
          setIsGraphGenerated(true);
          setIsGridArranged(false);
          setIsLevelWiseArranged(false);
        } catch (error) {
          console.error('Error fetching graph data:', error);
        }
      } else {
        fetchTitleNodes(selectedProject);
        setIsShowingGraph(false);
        setIsTitleNodesOnly(true);
        setIsGraphGenerated(false);
        setIsGridArranged(false);
        setIsLevelWiseArranged(false);
      }
    };



  const fetchCircularDependencies = async () => {
    try {
      const response = await fetch('/api/circular-dependencies');
      if (!response.ok) {
        throw new Error('Failed to fetch circular dependencies');
      }
      const data = await response.json();
      setCircularDependencyNodes([...new Set(data)]); // Ensure unique title nodes
    } catch (error) {
      console.error('Error fetching circular dependencies:', error);
    }
  };




  const handleCircularDependenciesToggle = () => {
    if (showCircularDependencies) {
      setCircularDependencyNodes([]);
      setCircularEdges([]);
    } else {
      fetchCircularDependencies();
    }
    setShowCircularDependencies(!showCircularDependencies);
  };

  const handleCircularDependencyNodeClick = async (nodeId, project) => {
    setSelectedNode(nodeId);
    try {
      const response = await fetch(`/api/graph?focusNode=${encodeURIComponent(nodeId)}&project=${encodeURIComponent(project)}`);
      if (!response.ok) {
        throw new Error('Failed to fetch graph data');
      }
      const data = await response.text();
      setGraphData(data);
      setIsShowingGraph(true);
      await fetchChildren(nodeId, project);

      // Fetch circular edges for the selected node
      const circularEdgesResponse = await fetch(`/api/circular-edges?title=${encodeURIComponent(nodeId)}&project=${encodeURIComponent(project)}`);
      if (!circularEdgesResponse.ok) {
        throw new Error('Failed to fetch circular edges');
      }
      const edges = await circularEdgesResponse.json();
      setCircularEdges(edges);
    } catch (error) {
      console.error('Error fetching graph data:', error);
    }
    setIsGraphGenerated(true);
        setIsGridArranged(false);
        setIsLevelWiseArranged(false);
  };

   const handleArrangeGrid = () => {
      if (graphRef.current && isTitleNodesOnly) {
        graphRef.current.arrangeInGrid();
      }
    };



    const handleToggleGridArrangement = () => {
        if (graphRef.current && isTitleNodesOnly) {
          graphRef.current.toggleGridArrangement();
          setIsGridArranged(!isGridArranged);
          setIsLevelWiseArranged(false);
        }
      };

     const handleSettleLevelWise = () => {
       if (graphRef.current && isGraphGenerated) {
         graphRef.current.toggleLevelWiseArrangement();
         setIsLevelWiseArranged(!isLevelWiseArranged);
         setIsGridArranged(false);
       }
     };

     const handleReset = () => {
         fetchTitleNodes(selectedProject);
         if (searchBarRef.current) {
           searchBarRef.current.resetSearch();
         }
         setShowTitleNodeSelector(false);
         setTitleNodesForDependency([]);
         setGraphData(null);
         setIsShowingGraph(false);
         setIsGraphGenerated(false);
         setIsGridArranged(false);
         setIsLevelWiseArranged(false);
         setIsTitleNodesOnly(true);  // Add this line to ensure title nodes are shown
       };


    return (
        <div className="graph-result-container">
          <div className="search-and-controls">
            <div className="search-bar">
              <SearchBar onSearch={(term) => handleSearch(term, selectedProject)} ref={searchBarRef} />
              <button className="reset-button" onClick={handleReset} title="Reset the graph to initial state">Reset</button>
            </div>
            <div className="control-buttons">
              {isTitleNodesOnly && (
                <button
                  onClick={handleToggleGridArrangement}
                  className="control-button"
                  title="Arrange nodes in a grid-like structure"
                >
                  {isGridArranged ? 'Reset Layout' : 'Arrange in Grid'}
                </button>
              )}
              {isGraphGenerated && (
                <button
                  onClick={handleSettleLevelWise}
                  className="control-button"
                  title="Arrange the graph in a level-wise structure"
                >
                  {isLevelWiseArranged ? 'Reset Layout' : 'Settle Levelwise'}
                </button>
              )}
              <button
                onClick={handleCircularDependenciesToggle}
                className="control-button"
                title="Show all configurations having circular dependencies"
              >
                {showCircularDependencies ? 'Hide Circular Dependencies' : 'Show Circular Dependencies'}
              </button>
            </div>
          </div>

         <div className="graph-and-panel">
           <div id="graph-container" style={{ width: '100%', height: 'calc(100vh - 200px)' }}>
             {graphData && (
               <Graph
                 data={graphData}
                 onNodeClick={(nodeId) => handleNodeClick(nodeId, selectedProject)}
                 ref={graphRef}
                 highlightedNode={searchedNode}
                 circularEdges={circularEdges}
                 selectedProject={selectedProject}
                 isTitleNodesOnly={isTitleNodesOnly}
                 isLevelWiseArranged={isLevelWiseArranged}
               />
             )}
           </div>
           <SidePanel
             selectedNode={selectedNode}
             childrenList={childrenList}
             onSelectChild={(childNode) => handleChildClick(childNode, selectedProject)}
             onToggleGraph={handleToggleGraph}
             isShowingGraph={isShowingGraph}
           />
         </div>

         {showCircularDependencies && circularDependencyNodes.length > 0 && (
           <div className="circular-dependencies-list">
             <h3>Circular Dependencies Found in Title Nodes:</h3>
             <ul>
               {circularDependencyNodes.map((node, index) => (
                 <li key={index} onClick={() => handleCircularDependencyNodeClick(node, selectedProject)}>
                   {node}
                 </li>
               ))}
             </ul>
           </div>
         )}

         {showTitleNodeSelector && (
           <div className="title-node-selector">
             <h3>Select a scope for the dependency:</h3>
             <ul>
               {titleNodesForDependency.map((titleNode, index) => (
                 <li key={index} onClick={() => handleTitleNodeSelection(titleNode)}>
                   {titleNode}
                 </li>
               ))}
             </ul>
           </div>
         )}
       </div>
     );};

     export default GraphResult;