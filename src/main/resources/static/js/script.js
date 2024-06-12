$(document).ready(function() {
    console.log("JavaScript is loaded and running");

    // Initial visibility setup
    d3.selectAll('g.node').each(function() {
        const node = d3.select(this);
        if (node.attr('data-root') !== 'true') {
            node.style('display', 'none').attr('data-visible', 'false');
        } else {
            node.style('display', 'block').attr('data-visible', 'true');
        }
        console.log(`Node processed: ${node.attr('data-id')}, Visible: ${node.attr('data-visible')}`);
    });

    d3.selectAll('g.edge').style('display', 'none').attr('data-visible', 'false');

    window.toggleNodes = function(nodeId) {
        console.log(`Toggling node: ${nodeId}`);
        const nodeElement = d3.select(`g.node[data-id='${nodeId}']`);
        const isVisible = nodeElement.attr('data-visible') === 'true';

        if (isVisible) {
            hideChildNodes(nodeId);
            nodeElement.attr('data-visible', 'false');
        } else {
            showChildNodes(nodeId);
            nodeElement.attr('data-visible', 'true');
        }
    };

    function hideChildNodes(nodeId) {
        console.log(`Hiding child nodes of: ${nodeId}`);
        d3.selectAll(`g.edge > title`).each(function() {
            const edgeText = d3.select(this).text();
            if (edgeText.startsWith(`${nodeId} ->`)) {
                const childId = edgeText.split(' -> ')[1];
                console.log(`Hiding child node: ${childId}`);
                d3.select(`g.node[data-id='${childId}']`).style('display', 'none').attr('data-visible', 'false');
                d3.select(`g.edge[data-edge-id='${nodeId} -> ${childId}']`).style('display', 'none').attr('data-visible', 'false');
                hideChildNodes(childId);
            }
        });
    }

    function showChildNodes(nodeId) {
        console.log(`Showing child nodes of: ${nodeId}`);
        d3.selectAll(`g.edge > title`).each(function() {
            const edgeText = d3.select(this).text();
            if (edgeText.startsWith(`${nodeId} ->`)) {
                const childId = edgeText.split(' -> ')[1];
                console.log(`Showing child node: ${childId}`);
                d3.select(`g.node[data-id='${childId}']`).style('display', 'block').attr('data-visible', 'true');
                d3.select(`g.edge[data-edge-id='${nodeId} -> ${childId}']`).style('display', 'block').attr('data-visible', 'true');
            }
        });
    }

    $('#searchForm').submit(function(event) {
        event.preventDefault();
        const searchTerm = $('#searchTerm').val().trim().toLowerCase();
        console.log(`Searching for: ${searchTerm}`);
        searchGraph(searchTerm);
    });

    function searchGraph(searchTerm) {
        console.log(`Searching for: ${searchTerm}`);
        if (searchTerm === '') {
            d3.selectAll('g.node').style('display', 'block').attr('data-visible', 'true');
            d3.selectAll('g.edge').style('display', 'block').attr('data-visible', 'true');
        } else {
            d3.selectAll('g.node').each(function() {
                const node = d3.select(this);
                const label = node.select('title').text().toLowerCase();
                if (label.includes(searchTerm)) {
                    console.log(`Node matched search: ${node.attr('data-id')}`);
                    node.style('display', 'block').attr('data-visible', 'true');
                } else {
                    node.style('display', 'none').attr('data-visible', 'false');
                }
            });
            d3.selectAll('g.edge').each(function() {
                const edge = d3.select(this);
                const edgeText = edge.select('title').text().toLowerCase();
                if (edgeText.includes(searchTerm)) {
                    console.log(`Edge matched search: ${edgeText}`);
                    edge.style('display', 'block').attr('data-visible', 'true');
                } else {
                    edge.style('display', 'none').attr('data-visible', 'false');
                }
            });
        }
    }

    window.resetGraph = function() {
        console.log("Resetting graph");
        $('#searchTerm').val('');
        searchGraph('');
    };
});

window.onerror = function(message, source, lineno, colno, error) {
    console.error(`Error: ${message} at ${source}:${lineno}:${colno}`);
};
