package com.companyos.backend.service.impl;

import com.companyos.backend.entity.Department;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.DepartmentRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public Department saveDepartment(Department department) {
        Long orgId = TenantContext.getRequiredTenantId();
        // Always stamp organizationId from the authenticated context — never trust client input
        department.setOrganizationId(orgId);
        department.setCompanyId(orgId);
        return departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        Long orgId = TenantContext.getRequiredTenantId();
        return departmentRepository.findByOrganizationId(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartment(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        return departmentRepository.findByDepartmentIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Long orgId = TenantContext.getRequiredTenantId();
        Department existing = departmentRepository.findByDepartmentIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        existing.setDepartmentName(department.getDepartmentName());
        if (department.getDescription() != null) {
            existing.setDescription(department.getDescription());
        }
        if (department.getManagerId() != null) {
            existing.setManagerId(department.getManagerId());
        }
        // Never allow changing the organizationId through an update
        return departmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Department existing = departmentRepository.findByDepartmentIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        departmentRepository.delete(existing);
    }
}
