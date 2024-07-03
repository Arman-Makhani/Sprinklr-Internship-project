// frontend/src/components/Graph.js
import React, { useEffect, useRef, useImperativeHandle, forwardRef, useState } from 'react';
import * as d3 from 'd3';
import './Graph.css';

const Graph = forwardRef(({ data, onNodeClick, highlightedNode, circularEdges, selectedProject, isTitleNodesOnly, isLevelWiseArranged }, ref) => {
  const svgRef = useRef(null);
  const [layout, setLayout] = useState('default');
  const [focusedNode, setFocusedNode] = useState(null);
  let simulation;

  useImperativeHandle(ref, () => ({
    toggleGridArrangement: () => {
      setLayout(prev => prev === 'grid' ? 'default' : 'grid');
    },
    toggleLevelWiseArrangement: () => {
      setLayout(prev => prev === 'levelWise' ? 'default' : 'levelWise');
    },
    setFocusedNode: (nodeId) => {
      setFocusedNode(nodeId);
    }
  }));

  const applyLevelWiseLayout = (nodes, links, width, height) => {
    const levelMap = new Map();
    const visited = new Set();

    function dfs(node, level) {
      if (visited.has(node.id)) return;
      visited.add(node.id);

      if (!levelMap.has(level)) {
        levelMap.set(level, []);
      }
      levelMap.get(level).push(node);

      links.forEach(link => {
        if (link.source.id === node.id) {
          dfs(link.target, level + 1);
        }
      });
    }

    const rootNodes = nodes.filter(node => !links.some(link => link.target.id === node.id));
    rootNodes.forEach(root => dfs(root, 0));

    const levelHeight = height / (levelMap.size + 1);
    levelMap.forEach((nodesInLevel, level) => {
      const levelWidth = width / (nodesInLevel.length + 1);
      nodesInLevel.forEach((node, index) => {
        node.x = (index + 1) * levelWidth;
        node.y = (level + 1) * levelHeight;
      });
    });

    const horizontalSpacing = 10000;
    nodes.forEach(node => {
      node.x *= (1 + horizontalSpacing / width);
    });
  };

  const applyGridLayout = (nodes, width, height) => {
    const columns = Math.ceil(Math.sqrt(nodes.length));
    const cellWidth = width / columns;
    const cellHeight = height / columns;
    const horizontalSpacing=30000;

    nodes.forEach((node, index) => {
      const row = Math.floor(index / columns);
      const col = index % columns;
      node.x = col * cellWidth + cellWidth / 2;
      node.y = row * cellHeight + cellHeight / 2;
    });
  };

  useEffect(() => {
    if (!data) return;

    const width = 2000;
    const height = 2000;

    const svg = d3.select(svgRef.current)
      .attr('width', width)
      .attr('height', height)
      .html(null);

    const g = svg.append('g');

    const zoom = d3.zoom()
      .scaleExtent([0.1, 4])
      .on('zoom', (event) => {
        g.attr('transform', event.transform);
      });

    svg.call(zoom);

    const parser = new DOMParser();
    const svgDoc = parser.parseFromString(data, 'image/svg+xml');

    const nodes = Array.from(svgDoc.querySelectorAll('.node')).map(node => ({
      id: node.querySelector('title').textContent,
      label: node.querySelector('text').textContent
    }));

    const links = Array.from(svgDoc.querySelectorAll('.edge')).map(edge => ({
      source: edge.querySelector('title').textContent.split('->')[0],
      target: edge.querySelector('title').textContent.split('->')[1]
    }));

    // Extract circular edges from metadata
    const circularEdgesMetadata = svgDoc.querySelector('metadata#circular-edges');
    const circularEdges = circularEdgesMetadata ? circularEdgesMetadata.textContent.split(',') : [];

    svg.append('defs').append('marker')
      .attr('id', 'arrowhead')
      .attr('viewBox', '-0 -5 10 10')
      .attr('refX', 15)
      .attr('refY', 0)
      .attr('orient', 'auto')
      .attr('markerWidth', 13)
      .attr('markerHeight', 13)
      .attr('xoverflow', 'visible')
      .append('svg:path')
      .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
      .attr('fill', '#999')
      .style('stroke', 'none');

    const link = g.append('g')
      .attr('class', 'links')
      .selectAll('line')
      .data(links)
      .enter().append('line')
      .attr('stroke', '#999')
      .attr('stroke-opacity', 0.6)
      .attr('stroke-width', d => Math.sqrt(d.value))
      .attr('marker-end', 'url(#arrowhead)');

    const node = g.append('g')
      .attr('class', 'nodes')
      .selectAll('g')
      .data(nodes)
      .enter().append('g')
      .attr('class', 'node')
      .call(d3.drag()
        .on('start', dragstarted)
        .on('drag', dragged)
        .on('end', dragended));

    node.append('circle')
      .attr('r', 20)
      .attr('fill', d => getNodeColor(d.id));

    node.append('text')
      .attr('dy', 30)
      .text(d => d.label)
      .attr('text-anchor', 'middle')
      .attr('font-size', '12px');

    node.on('click', function(event, d) {
      onNodeClick(d.id, selectedProject);
      setFocusedNode(d.id);
      moveNodeToTop(d.id);
    });

    // Apply blinking effect to circular edges
    link.filter(d => {
      const edgeKey = `${d.source}->${d.target}`;
      return circularEdges.includes(edgeKey);
    }).classed('blinking', true);

    function getNodeColor(id) {
      if (id === focusedNode) return 'pink';
      if (id.includes('(n)')) return 'lightcoral';
      if (id.includes('(c)')) return 'greenyellow';
      if (id.includes('(*)')) return 'yellow';
      return 'lightblue';
    }

    function moveNodeToTop(id) {
      node.filter(d => d.id === id).each(function() {
        this.parentNode.appendChild(this);
      });
    }

    function updateLayout() {
      if (layout === 'levelWise') {
        applyLevelWiseLayout(nodes, links, width, height);
      } else if (layout === 'grid' && isTitleNodesOnly) {
        applyGridLayout(nodes, width, height);
      } else {
        simulation.alpha(1).restart();
      }

      node.attr('transform', d => `translate(${d.x},${d.y})`)
         .select('circle')
         .attr('fill', d => getNodeColor(d.id));

      if (focusedNode) {
        moveNodeToTop(focusedNode);
      }

      link
        .attr('x1', d => d.source.x)
        .attr('y1', d => d.source.y)
        .attr('x2', d => d.target.x)
        .attr('y2', d => d.target.y);
    }

    function dragstarted(event, d) {
      if (!event.active) simulation.alphaTarget(0.3).restart();
      d.fx = d.x;
      d.fy = d.y;
    }

    function dragged(event, d) {
      d.fx = event.x;
      d.fy = event.y;
    }

    function dragended(event, d) {
      if (!event.active) simulation.alphaTarget(0);
      d.fx = null;
      d.fy = null;
    }

    simulation = d3.forceSimulation(nodes)
      .force('link', d3.forceLink(links).id(d => d.id).distance(100))
      .force('charge', d3.forceManyBody().strength(-300))
      .force('center', d3.forceCenter(width / 2, height / 2))
      .force('collision', d3.forceCollide().radius(50))
      .on('tick', updateLayout);

    updateLayout();

    return () => {
      simulation.stop();
    };

  }, [data, onNodeClick, highlightedNode, selectedProject, isTitleNodesOnly, layout, focusedNode]);

  return <svg ref={svgRef} />;
});

export default Graph;
