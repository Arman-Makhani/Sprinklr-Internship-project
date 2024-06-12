package com.example.demo3.service;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import com.example.demo3.utils.DependencyParser;
import com.example.demo3.utils.DependencyParser.DependencyDetails;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

@Service
public class GraphService {

    private List<Map<String, Map<String, List<String>>>> allChunks;
    private Map<String, DependencyDetails> dependencyDetailsMap = new HashMap<>();
    private Set<String> circularEdges = new HashSet<>();

    @Async
    public CompletableFuture<List<String>> generateGraphsAsync(List<Map<String, Map<String, List<String>>>> allChunks) throws IOException {
        this.allChunks = allChunks;
        populateDependencyDetailsMap(allChunks);
        detectCircularDependencies();
        return CompletableFuture.completedFuture(generateGraphs(allChunks, null));
    }

    public List<String> generateGraphs(List<Map<String, Map<String, List<String>>>> allChunks, String searchTerm) throws IOException {
        GraphvizCmdLineEngine engine = new GraphvizCmdLineEngine();
        engine.timeout(600, TimeUnit.SECONDS);
        Graphviz.useEngine(engine);

        List<String> svgGraphs = new ArrayList<>();
        searchTerm = (searchTerm != null) ? searchTerm.toLowerCase().replaceAll("\\s+", "") : null;

        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            MutableGraph g = mutGraph("dependencies").setDirected(true).graphAttrs().add("rankdir", "LR");
            Map<String, MutableNode> nodes = new HashMap<>();
            Set<String> addedEdges = new HashSet<>();

            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                String title = titleEntry.getKey();
                MutableNode titleNode = mutNode(title).add(Shape.RECTANGLE, Label.of(title));
                g.add(titleNode);

                for (Map.Entry<String, List<String>> entry : titleEntry.getValue().entrySet()) {
                    String parent = entry.getKey();
                    MutableNode parentNode = nodes.computeIfAbsent(parent, k -> mutNode(parent).add(Shape.DOUBLE_OCTAGON));
                    applyColor(parentNode, parent, searchTerm);
                    titleNode.addLink(parentNode);
                    DependencyDetails parentDetails = dependencyDetailsMap.get(parent);
                    if (parentDetails != null) {
                        String label = String.format("%s:%s:%s", parentDetails.group, parentDetails.name, parentDetails.version);
                        if (parentDetails.conflictVersion != null) {
                            label += " ⚠";
                            parentNode.add("data-conflict", String.format("Conflict: Requires versions %s and %s. Resolution: Align to version %s.", parentDetails.conflictVersion, parentDetails.version, parentDetails.version));
                            label += " (" + parentDetails.conflictVersion + " -> " + parentDetails.version + ")";
                        }
                        parentNode.add(Label.of(label)).add("data-configuration", parentDetails.configuration);
                    }

                    for (String child : entry.getValue()) {
                        String formattedChild = child.replaceAll("\\s+", "");
                        MutableNode childNode = nodes.computeIfAbsent(child, k -> mutNode(child).add(Shape.DOUBLE_OCTAGON));
                        applyColor(childNode, formattedChild, searchTerm);

                        String edgeKey = parent + "->" + child;
                        if (!addedEdges.contains(edgeKey)) {
                            if (circularEdges.contains(edgeKey)) {
                                parentNode.addLink(childNode).add(Color.RED);
                            } else {
                                parentNode.addLink(childNode);
                            }
                            addedEdges.add(edgeKey);
                        }
                        DependencyDetails childDetails = dependencyDetailsMap.get(child);
                        if (childDetails != null) {
                            String label = String.format("%s:%s:%s", childDetails.group, childDetails.name, childDetails.version);
                            if (childDetails.conflictVersion != null) {
                                label += " ⚠";
                                childNode.add("data-conflict", String.format("Conflict: Requires versions %s and %s. Resolution: Align to version %s.", childDetails.conflictVersion, childDetails.version, childDetails.version));
                                label += " (" + childDetails.conflictVersion + " -> " + childDetails.version + ")";
                            }
                            childNode.add(Label.of(label)).add("data-configuration", childDetails.configuration);
                        }
                    }
                }
            }

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

    public Set<String> getCircularEdges() {
        return circularEdges;
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

    private void applyColor(MutableNode node, String name, String searchTerm) {
        node.add(Color.WHITE.fill());
        if (name.contains("(n)")) {
            node.add(Color.named("lightcoral"));
        } else if (name.contains("(c)")) {
            node.add(Color.named("greenyellow"));
        } else if (name.contains("(*)")) {
            node.add(Color.named("yellow"));
        } else {
            node.add(Color.named("lightblue2"));
        }

        if (searchTerm != null && name.toLowerCase().replaceAll("\\s+", "").contains(searchTerm)) {
            if (name.contains("(n)")) {
                node.add(Color.named("lightcoral").fill());
            } else if (name.contains("(c)")) {
                node.add(Color.named("greenyellow").fill());
            } else if (name.contains("(*)")) {
                node.add(Color.named("yellow").fill());
            } else {
                node.add(Color.named("lightblue2").fill());
            }
            node.add(Style.BOLD);
        }
    }

    public Path exportGraphAsPNG(String fileName) throws IOException {
        return exportGraph(fileName, Format.PNG);
    }

    private Path exportGraph(String fileName, Format format) throws IOException {
        GraphvizCmdLineEngine engine = new GraphvizCmdLineEngine();
        engine.timeout(600, TimeUnit.SECONDS);
        Graphviz.useEngine(engine);

        MutableGraph g = mutGraph("dependencies").setDirected(true);

        for (Map<String, Map<String, List<String>>> chunk : allChunks) {
            Map<String, MutableNode> nodes = new HashMap<>();
            Set<String> addedEdges = new HashSet<>();

            for (Map.Entry<String, Map<String, List<String>>> titleEntry : chunk.entrySet()) {
                String title = titleEntry.getKey();
                MutableNode titleNode = mutNode(title).add(Shape.RECTANGLE, Label.of(title));
                g.add(titleNode);

                for (Map.Entry<String, List<String>> entry : titleEntry.getValue().entrySet()) {
                    String parent = entry.getKey();
                    MutableNode parentNode = nodes.computeIfAbsent(parent, k -> mutNode(parent).add(Shape.DOUBLE_OCTAGON));
                    applyColorForExport(parentNode, parent);
                    titleNode.addLink(parentNode);

                    for (String child : entry.getValue()) {
                        String formattedChild = child.replaceAll("\\s+", "");
                        MutableNode childNode = nodes.computeIfAbsent(child, k -> mutNode(child).add(Shape.DOUBLE_OCTAGON));
                        applyColorForExport(childNode, formattedChild);

                        String edgeKey = parent + "->" + child;
                        if (!addedEdges.contains(edgeKey)) {
                            parentNode.addLink(childNode);
                            addedEdges.add(edgeKey);
                        }
                    }
                }
            }
        }

        Path filePath = Paths.get(fileName);
        Graphviz.fromGraph(g).render(format).toFile(filePath.toFile());
        return filePath;
    }

    private void applyColorForExport(MutableNode node, String name) {
        if (name.contains("(n)")) {
            node.add(Color.named("lightcoral"));
        } else if (name.contains("(c)")) {
            node.add(Color.named("greenyellow"));
        } else if (name.contains("(*)")) {
            node.add(Color.named("yellow"));
        } else {
            node.add(Color.named("lightblue2"));
        }
    }
}
