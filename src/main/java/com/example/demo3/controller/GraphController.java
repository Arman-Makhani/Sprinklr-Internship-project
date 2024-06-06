package com.example.demo3.controller;

import com.example.demo3.service.GraphService;
import com.example.demo3.utils.DependencyParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
@EnableAsync
public class GraphController {

    @Autowired
    private GraphService graphService;

    private List<Map<String, Map<String, List<String>>>> allChunks;
    private Set<String> allDependencies;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/generate")
    public String generateGraph(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "index";
        }

        try {
            // Save the uploaded file
            Path path = Paths.get(file.getOriginalFilename());
            Files.write(path, file.getBytes());

            // Parse and process the file in chunks
            allChunks = DependencyParser.parseDependencies(path.toString(), 500); // Adjust chunk size as needed
            allDependencies = allChunks.stream()
                    .flatMap(chunk -> chunk.values().stream())
                    .flatMap(deps -> deps.keySet().stream())
                    .collect(Collectors.toSet());

            CompletableFuture<List<String>> futureGraphs = graphService.generateGraphsAsync(allChunks);
            List<String> svgGraphs = futureGraphs.get(); // Wait for async processing to complete

            // Combine all SVGs into a single HTML view
            model.addAttribute("graphs", svgGraphs);
            model.addAttribute("filePath", path.toString());

        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
            model.addAttribute("message", "An error occurred while processing the file.");
        }

        return "result";
    }

    @PostMapping("/search")
    public String searchGraph(@RequestParam("searchTerm") String searchTerm, @RequestParam("filePath") String filePath, Model model) {
        try {
            // Process the file in chunks with search term
            List<String> svgGraphs = graphService.generateGraphs(allChunks, searchTerm);

            // Combine all SVGs into a single HTML view
            model.addAttribute("graphs", svgGraphs);
            model.addAttribute("filePath", filePath);
            model.addAttribute("searchTerm", searchTerm);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "An error occurred while processing the file.");
        }

        return "result";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public Set<String> autocomplete(@RequestParam("term") String term) {
        return allDependencies.stream()
                .filter(dep -> dep.toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toSet());
    }

    @GetMapping("/export/png")
    public ResponseEntity<Resource> exportGraphAsPNG(@RequestParam String fileName) {
        try {
            Path file = graphService.exportGraphAsPNG(fileName);
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/circular-dependencies")
    @ResponseBody
    public Set<String> getCircularEdges() {
        return graphService.getCircularEdges();
    }
}
