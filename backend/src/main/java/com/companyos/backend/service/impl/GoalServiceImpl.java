package com.companyos.backend.service.impl;

import com.companyos.backend.dto.GoalDTO;
import com.companyos.backend.entity.Goal;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.DepartmentRepository;
import com.companyos.backend.repository.EmployeeRepository;
import com.companyos.backend.repository.GoalRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.GoalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public GoalServiceImpl(GoalRepository goalRepository, DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.goalRepository = goalRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalDTO> getAllGoals() {
        Long orgId = TenantContext.getRequiredTenantId();
        return goalRepository.findByOrganizationId(orgId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDTO getGoalById(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Goal goal = goalRepository.findByGoalIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        return mapToDTO(goal);
    }

    @Override
    @Transactional
    public GoalDTO createGoal(GoalDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        Goal goal = new Goal();
        goal.setOrganizationId(orgId);
        goal.setTitle(dto.getTitle().trim());
        goal.setDescription(dto.getDescription());
        goal.setLevel(dto.getLevel() != null ? dto.getLevel() : "ORGANIZATION");
        goal.setDepartmentId(dto.getDepartmentId());
        goal.setOwnerEmployeeId(dto.getOwnerEmployeeId());
        goal.setTargetValue(dto.getTargetValue() != null ? dto.getTargetValue() : 100.0);
        goal.setCurrentValue(dto.getCurrentValue() != null ? dto.getCurrentValue() : 0.0);
        goal.setUnit(dto.getUnit() != null ? dto.getUnit() : "%");
        goal.setDeadline(dto.getDeadline());
        goal.setStatus(dto.getStatus() != null ? dto.getStatus() : "IN_PROGRESS");

        return mapToDTO(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalDTO updateGoalProgress(Long id, Double currentValue) {
        Long orgId = TenantContext.getRequiredTenantId();
        Goal goal = goalRepository.findByGoalIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        goal.setCurrentValue(currentValue);
        return mapToDTO(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalDTO updateGoal(Long id, GoalDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        Goal goal = goalRepository.findByGoalIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));

        goal.setTitle(dto.getTitle().trim());
        goal.setDescription(dto.getDescription());
        if (dto.getLevel() != null) goal.setLevel(dto.getLevel());
        goal.setDepartmentId(dto.getDepartmentId());
        goal.setOwnerEmployeeId(dto.getOwnerEmployeeId());
        if (dto.getTargetValue() != null) goal.setTargetValue(dto.getTargetValue());
        if (dto.getCurrentValue() != null) goal.setCurrentValue(dto.getCurrentValue());
        if (dto.getUnit() != null) goal.setUnit(dto.getUnit());
        goal.setDeadline(dto.getDeadline());
        if (dto.getStatus() != null) goal.setStatus(dto.getStatus());

        return mapToDTO(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Goal goal = goalRepository.findByGoalIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        goalRepository.delete(goal);
    }

    private GoalDTO mapToDTO(Goal goal) {
        GoalDTO dto = new GoalDTO();
        dto.setGoalId(goal.getGoalId());
        dto.setOrganizationId(goal.getOrganizationId());
        dto.setTitle(goal.getTitle());
        dto.setDescription(goal.getDescription());
        dto.setLevel(goal.getLevel());
        dto.setDepartmentId(goal.getDepartmentId());
        dto.setOwnerEmployeeId(goal.getOwnerEmployeeId());
        dto.setTargetValue(goal.getTargetValue());
        dto.setCurrentValue(goal.getCurrentValue());
        dto.setUnit(goal.getUnit());
        dto.setDeadline(goal.getDeadline());
        dto.setStatus(goal.getStatus());
        dto.setProgress(goal.getProgress());
        dto.setCreatedAt(goal.getCreatedAt());

        if (goal.getDepartmentId() != null) {
            departmentRepository.findById(goal.getDepartmentId()).ifPresent(d -> dto.setDepartmentName(d.getDepartmentName()));
        }

        if (goal.getOwnerEmployeeId() != null) {
            employeeRepository.findById(goal.getOwnerEmployeeId()).ifPresent(emp -> {
                dto.setOwnerEmployeeName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            });
        }

        return dto;
    }
}
