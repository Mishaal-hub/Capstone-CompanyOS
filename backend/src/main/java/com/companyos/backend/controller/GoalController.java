package com.companyos.backend.controller;

import com.companyos.backend.dto.GoalDTO;
import com.companyos.backend.service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalDTO>> getAllGoals() {
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDTO> getGoal(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @PostMapping
    public ResponseEntity<GoalDTO> createGoal(@RequestBody GoalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDTO> updateGoal(@PathVariable Long id, @RequestBody GoalDTO dto) {
        return ResponseEntity.ok(goalService.updateGoal(id, dto));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<GoalDTO> updateProgress(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        Double currentValue = body.get("currentValue");
        if (currentValue == null) throw new IllegalArgumentException("currentValue is required");
        return ResponseEntity.ok(goalService.updateGoalProgress(id, currentValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
