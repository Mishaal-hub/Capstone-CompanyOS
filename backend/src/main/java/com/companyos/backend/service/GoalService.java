package com.companyos.backend.service;

import com.companyos.backend.dto.GoalDTO;
import java.util.List;

public interface GoalService {
    List<GoalDTO> getAllGoals();
    GoalDTO getGoalById(Long id);
    GoalDTO createGoal(GoalDTO goalDTO);
    GoalDTO updateGoalProgress(Long id, Double currentValue);
    GoalDTO updateGoal(Long id, GoalDTO goalDTO);
    void deleteGoal(Long id);
}
