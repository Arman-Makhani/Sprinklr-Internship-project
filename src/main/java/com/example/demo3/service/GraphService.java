package com.example.demo3.service;

import com.example.demo3.utils.DependencyParser;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

@Service
public class GraphService {

    private List<Map<String, Map<String, List<String>>>> allChunks;
    private final Map<String, DependencyParser.DependencyDetails> dependencyDetailsMap = new HashMap<>();
    private final Set<String> circularEdges = new HashSet<>();



    public List<String> generateGraphFromFile(MultipartFile file) throws IOException {
        Path path = Paths.get(file.getOriginalFilename());
        file.transferTo(path);
        allChunks = DependencyParser.parseDependencies(path.toString(), 1000);
        populateDependencyDetailsMap(allChunks);
        detectCircularDependencies();
        return generateGraphs(allChunks, null);
    }

    public String getTitleNodes() {
        try {
            MutableGraph g = mutGraph("dependencies").setDirected(true).graphAttrs().add("rankdir", "TB");
            for (Map<String, Map<String, List<String>>> chunk : allChunks) {
                for (String title : chunk.keySet()) {
                    MutableNode titleNode = mutNode(title).add(Shape.RECTANGLE, Label.of(title));
                    g.add(titleNode);
                }
            }
            return Graphviz.fromGraph(g).width(1000).render(Format.SVG).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating title nodes";
        }
    }

    public List<String> getAutocompleteSuggestions(String term) {
        List<String> allNodes = getAllNodesExcludingTitles();
        return allNodes.stream()
                .filter(node -> node.toLowerCase().contains(term.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());
    }





    public String getGraphDataWithFocus(String focusNode) {
        if (allChunks == null || allChunks.isEmpty()) {
            return "";
        }
        try {
            Map<String, Map<String, List<String>>> targetChunk = findChunkForTitle(focusNode);
            if (targetChunk == null) {
                return "Node not found";
            }
            List<String> graphData = generateGraphs(Collections.singletonList(targetChunk), focusNode);
            String svgData = graphData.get(0);

            // Add circular edges information to the SVG
            StringBuilder svgBuilder = new StringBuilder(svgData);
            int insertIndex = svgBuilder.indexOf("</svg>");
            svgBuilder.insert(insertIndex, "<metadata id='circular-edges'>" + String.join(",", circularEdges) + "</metadata>");

            return svgBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error generating graph data";
        }
    }


    public String getGraphDataForSearch(String searchTerm) {
        if (allChunks == null || allChunks.isEmpty()) {
            return "";
        }
        String titleNode = findTitleNodeForSearchTerm(searchTerm);
        if (titleNode == null) {
            return "Node not found";
        }
        return getGraphDataWithFocus(titleNode);
    }

    private Map<String, Map<String, List<String>>> findChunkForTitle(String title) {
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            if (chunk.containsKey(title)) {
                return chunk;
            }
        }
        return null;
    }

    private String findTitleNodeForSearchTerm(String searchTerm) {
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> entry : chunk.entrySet()) {
                Map<String, List<String>> dependencies = entry.getValue();
                if (dependencies.containsKey(searchTerm) || dependencies.values().stream().anyMatch(list -> list.contains(searchTerm))) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void generateSubGraph(Map.Entry<String, Map<String, List<String>>> titleEntry, MutableGraph g, Map<String, MutableNode> nodes, Set<String> uniqueEdges, String focusNode) {
        String title = titleEntry.getKey();
        MutableNode titleNode = mutNode(title).add(Shape.RECTANGLE, Label.of(title));
        g.add(titleNode);

        Map<String, List<String>> dependencies = titleEntry.getValue();
        List<String> directChildren = dependencies.get(title);

        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
            String parent = entry.getKey();
            if (parent.equals(title)) continue;

            MutableNode parentNode = nodes.computeIfAbsent(parent, k -> mutNode(parent).add(Shape.DOUBLE_OCTAGON));
            applyColor(parentNode, parent, focusNode);

            DependencyParser.DependencyDetails parentDetails = dependencyDetailsMap.get(parent);
            if (parentDetails != null) {
                parentNode.add(Label.of(parentDetails.toString()));
            }

            if (directChildren != null && directChildren.contains(parent)) {
                String titleToParentKey = title + "->" + parent;
                if (uniqueEdges.add(titleToParentKey)) {
                    titleNode.addLink(parentNode);
                }
            }

            for (String child : entry.getValue()) {
                MutableNode childNode = nodes.computeIfAbsent(child, k -> mutNode(child).add(Shape.DOUBLE_OCTAGON));
                applyColor(childNode, child, focusNode);

                String parentToChildKey = parent + "->" + child;
                if (uniqueEdges.add(parentToChildKey)) {
                    if (circularEdges.contains(parentToChildKey)) {
                        parentNode.addLink(childNode).add(Color.RED, guru.nidi.graphviz.attribute.Style.BOLD);
                    } else {
                        parentNode.addLink(childNode).add(guru.nidi.graphviz.attribute.Style.SOLID);
                    }
                }

                DependencyParser.DependencyDetails childDetails = dependencyDetailsMap.get(child);
                if (childDetails != null) {
                    childNode.add(Label.of(childDetails.toString()));
                }
            }
        }


    }

    private void applyLevelWiseLayout(MutableGraph g) {
        g.graphAttrs().add("rankdir", "TB");
        g.graphAttrs().add("ranksep", "2");
        g.graphAttrs().add("nodesep", "500");
        g.graphAttrs().add("splines", "ortho");
        g.graphAttrs().add("concentrate", "true");
    }

    private List<String> generateGraphs(List<Map<String, Map<String, List<String>>>> chunks, String focusNode) throws IOException {
        GraphvizCmdLineEngine engine = new GraphvizCmdLineEngine();
        engine.timeout(600, TimeUnit.SECONDS);
        Graphviz.useEngine(engine);

        List<String> svgGraphs = new ArrayList<>();

        for (Map<String, Map<String, List<String>>> chunk : chunks) {
            MutableGraph g = mutGraph("dependencies").setDirected(true).graphAttrs().add("rankdir", "TB");
            Map<String, MutableNode> nodes = new HashMap<>();
            Set<String> uniqueEdges = new HashSet<>();

            if (focusNode == null || focusNode.isEmpty()) {
                for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                    generateSubGraph(titleEntry, g, nodes, uniqueEdges, null);
                }
            } else {
                Map.Entry<String, Map<String, List<String>>> titleEntry = chunk.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(focusNode))
                        .findFirst()
                        .orElse(null);
                if (titleEntry != null) {
                    generateSubGraph(titleEntry, g, nodes, uniqueEdges, focusNode);
                }
            }
            applyLevelWiseLayout(g);
            svgGraphs.add(Graphviz.fromGraph(g).width(1000).render(Format.SVG).toString());
        }

        return svgGraphs;
    }

    private void populateDependencyDetailsMap(List<Map<String, Map<String, List<String>>>> allChunks) {
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                String title = titleEntry.getKey();
                for (Map.Entry<String, List<String>> entry : titleEntry.getValue().entrySet()) {
                    String parent = entry.getKey();
                    dependencyDetailsMap.putIfAbsent(parent, DependencyParser.parseDependencyDetails(parent, determineConfiguration(title)));

                    for (String child : entry.getValue()) {
                        dependencyDetailsMap.putIfAbsent(child, DependencyParser.parseDependencyDetails(child, determineConfiguration(title)));
                    }
                }
            }
        }
    }

    private String determineConfiguration(String title) {
        if (title.contains("implementation")) {
            return "implementation";
        } else if (title.contains("api")) {
            return "api";
        } else if (title.contains("runtimeOnly")) {
            return "runtimeOnly";
        } else if (title.contains("testImplementation")) {
            return "testImplementation";
        } else if (title.contains("testRuntimeOnly")) {
            return "testRuntimeOnly";
        }
        return "implementation";
    }

    private List<String> getAllNodesExcludingTitles() {
        Set<String> allNodes = new HashSet<>();
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                allNodes.addAll(titleEntry.getValue().keySet());
                for (List<String> children : titleEntry.getValue().values()) {
                    allNodes.addAll(children);
                }
            }
        }
        return new ArrayList<>(allNodes);
    }

    private void applyColor(MutableNode node, String name, String searchTerm) {
        if (name.contains("(n)")) {
            node.add(Shape.DOUBLE_OCTAGON, Color.named("lightcoral"));
        } else if (name.contains("(c)")) {
            node.add(Shape.DOUBLE_OCTAGON, Color.named("greenyellow"));
        } else if (name.contains("(*)")) {
            node.add(Shape.DOUBLE_OCTAGON, Color.named("yellow"));
        } else {
            node.add(Shape.DOUBLE_OCTAGON, Color.named("lightblue2"));
        }

        if (searchTerm != null && name.toLowerCase().replaceAll("\\s+", "").contains(searchTerm)) {
            node.add(Color.RED.fill());
        }
    }

    public List<String> getChildrenForNode(String node, String project) {
        System.out.println("Searching for children of node: " + node + " in project: " + (project != null ? project : "all projects"));
        List<String> children = new ArrayList<>();

        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                String titleNode = titleEntry.getKey();
                Map<String, List<String>> dependencies = titleEntry.getValue();

                // Check if the node is a title node or matches the search term
                if ((titleNode.equals(node) || titleNode.contains(node)) && (project == null || titleNode.startsWith(project))) {
                    children.addAll(dependencies.getOrDefault(titleNode, Collections.emptyList()));
                    System.out.println("Found children for title node " + node + ": " + children);
                    return children;
                }

                // Check if the node is any dependency node
                for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
                    if (entry.getKey().equals(node) || entry.getKey().contains(node)) {
                        children.addAll(entry.getValue());
                        System.out.println("Found children for dependency node " + node + ": " + children);
                        return children;
                    }
                }
            }
        }

        System.out.println("No children found for: " + node);
        return children;
    }

    public List<String> getTitleNodesWithCircularDependencies() {
        List<String> titleNodesWithCircularDependencies = new ArrayList<>();
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                String titleNode = titleEntry.getKey();
                for (Map.Entry<String, List<String>> entry : titleEntry.getValue().entrySet()) {
                    String parent = entry.getKey();
                    for (String child : entry.getValue()) {
                        String edge = parent + "->" + child;
                        if (circularEdges.contains(edge)) {
                            titleNodesWithCircularDependencies.add(titleNode);
                            break;
                        }
                    }
                }
            }
        }
        return titleNodesWithCircularDependencies;
    }

    private void detectCircularDependencies() {
        Map<String, List<String>> graph = new HashMap<>();
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                for (Map.Entry<String, List<String>> entry : titleEntry.getValue().entrySet()) {
                    graph.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
        }
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (String node : graph.keySet()) {
            detectCycles(node, graph, visited, recStack, new ArrayList<>());
        }
    }

    private void detectCycles(String node, Map<String, List<String>> graph, Set<String> visited, Set<String> recStack, List<String> path) {
        if (recStack.contains(node)) {
            int index = path.indexOf(node);
            if (index != -1) {
                List<String> cycle = path.subList(index, path.size());
                for (int i = 0; i < cycle.size(); i++) {
                    circularEdges.add(cycle.get(i) + "->" + cycle.get((i + 1) % cycle.size()));
                }
            }
            return;
        }
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        recStack.add(node);
        path.add(node);
        List<String> children = graph.getOrDefault(node, Collections.emptyList());
        for (String child : children) {
            detectCycles(child, graph, visited, recStack, path);
        }
        recStack.remove(node);
        path.remove(path.size() - 1);
    }

    public List<String> getTitleNodesForDependency(String dependency) {
        List<String> titleNodes = new ArrayList<>();
        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            for (Map.Entry<String, Map<String, List<String>>> entry : chunk.entrySet()) {
                String titleNode = entry.getKey();
                Map<String, List<String>> dependencies = entry.getValue();
                if (dependencies.containsKey(dependency) || dependencies.values().stream().anyMatch(list -> list.contains(dependency))) {
                    titleNodes.add(titleNode);
                }
            }
        }
        return titleNodes;
    }
}