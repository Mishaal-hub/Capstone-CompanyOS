package com.companyos.backend.controller;

import com.companyos.backend.dto.TaskCommentDTO;
import com.companyos.backend.dto.TaskDependencyDTO;
import com.companyos.backend.dto.TaskRequestDTO;
import com.companyos.backend.dto.TaskResponseDTO;
import com.companyos.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(taskService.getAllTasks(projectId, assigneeId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO request) {
        TaskResponseDTO created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusMap) {
        String status = statusMap.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/dependencies")
    public ResponseEntity<TaskDependencyDTO> addDependency(
            @PathVariable Long id,
            @RequestParam Long dependsOnTaskId,
            @RequestParam(defaultValue = "BLOCKS") String dependencyType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.addDependency(id, dependsOnTaskId, dependencyType));
    }

    @DeleteMapping("/{id}/dependencies/{dependsOnTaskId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable Long id,
            @PathVariable Long dependsOnTaskId) {
        taskService.removeDependency(id, dependsOnTaskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TaskCommentDTO> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> commentMap) {
        String content = commentMap.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(id, content));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TaskCommentDTO>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getComments(id));
    }
}
