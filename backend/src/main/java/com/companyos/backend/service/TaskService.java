package com.companyos.backend.service;

import com.companyos.backend.dto.TaskCommentDTO;
import com.companyos.backend.dto.TaskDependencyDTO;
import com.companyos.backend.dto.TaskRequestDTO;
import com.companyos.backend.dto.TaskResponseDTO;

import java.util.List;

public interface TaskService {
    List<TaskResponseDTO> getAllTasks(Long projectId, Long assigneeId, String status);
    TaskResponseDTO getTaskById(Long id);
    TaskResponseDTO createTask(TaskRequestDTO request);
    TaskResponseDTO updateTask(Long id, TaskRequestDTO request);
    TaskResponseDTO updateTaskStatus(Long id, String status);
    void deleteTask(Long id);

    TaskDependencyDTO addDependency(Long taskId, Long dependsOnTaskId, String dependencyType);
    void removeDependency(Long taskId, Long dependsOnTaskId);

    TaskCommentDTO addComment(Long taskId, String content);
    List<TaskCommentDTO> getComments(Long taskId);
}
