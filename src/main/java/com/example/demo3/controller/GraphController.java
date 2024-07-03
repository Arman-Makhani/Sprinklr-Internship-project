// src/main/java/com/example/demo3/controller/GraphController.java
package com.example.demo3.controller;

import com.example.demo3.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@CrossOrigin(origins = "http://localhost:3000")
public class GraphController {

    @Autowired
    private GraphService graphService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateGraph(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            List<String> svgGraphs = graphService.generateGraphFromFile(file);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/svg+xml").body(String.join("\n", svgGraphs));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the file.");
        }
    }

    @GetMapping("/api/title-nodes")
    public ResponseEntity<String> getTitleNodes() {
        try {
            String titleNodes = graphService.getTitleNodes();
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/svg+xml").body(titleNodes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching title nodes.");
        }
    }


    @GetMapping("/api/graph")
    public ResponseEntity<String> getGraphData(@RequestParam(value = "focusNode", required = false) String focusNode) {
        try {
            String graphData = graphService.getGraphDataWithFocus(focusNode);
            if (graphData == null || graphData.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating graph data");
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/svg+xml").body(graphData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the graph data.");
        }
    }

    @GetMapping("/api/search")
    public ResponseEntity<String> searchGraph(@RequestParam("term") String term) {
        try {
            String graphData = graphService.getGraphDataForSearch(term);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/svg+xml").body(graphData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while searching the graph.");
        }
    }

    @GetMapping("/api/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(@RequestParam("term") String term) {
        try {
            List<String> suggestions = graphService.getAutocompleteSuggestions(term);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/api/children")
    public ResponseEntity<List<String>> getChildren(@RequestParam String title, @RequestParam(required = false) String project) {
        System.out.println("Backend received request for children of: " + title + " in project: " + (project != null ? project : "all projects"));
        List<String> children = graphService.getChildrenForNode(title, project);
        System.out.println("Backend returning children: " + children);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/api/circular-dependencies")
    public ResponseEntity<List<String>> getCircularDependencies() {
        List<String> titleNodes = graphService.getTitleNodesWithCircularDependencies();
        return ResponseEntity.ok(titleNodes);
    }


    @GetMapping("/api/title-nodes-for-dependency")
    public ResponseEntity<List<String>> getTitleNodesForDependency(@RequestParam("dependency") String dependency) {
        List<String> titleNodes = graphService.getTitleNodesForDependency(dependency);
        return ResponseEntity.ok(titleNodes);
    }


}