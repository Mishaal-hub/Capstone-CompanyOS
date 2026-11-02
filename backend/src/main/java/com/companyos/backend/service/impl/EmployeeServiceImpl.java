package com.companyos.backend.service.impl;

import com.companyos.backend.entity.Employee;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.EmployeeRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Employee saveEmployee(Employee employee) {
        Long orgId = TenantContext.getRequiredTenantId();
        // Always stamp the organizationId from the authenticated context — never trust client input
        employee.setOrganizationId(orgId);
        employee.setCompanyId(orgId);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        Long orgId = TenantContext.getRequiredTenantId();
        return employeeRepository.findByOrganizationId(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployee(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        return employeeRepository.findByEmployeeIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        Long orgId = TenantContext.getRequiredTenantId();
        Employee existing = employeeRepository.findByEmployeeIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Update only the fields that came in; never change org ownership
        if (employee.getFirstName()  != null) existing.setFirstName(employee.getFirstName());
        if (employee.getLastName()   != null) existing.setLastName(employee.getLastName());
        if (employee.getEmail()      != null) existing.setEmail(employee.getEmail());
        if (employee.getPhone()      != null) existing.setPhone(employee.getPhone());
        if (employee.getDesignation()!= null) existing.setDesignation(employee.getDesignation());
        if (employee.getPosition()   != null) existing.setPosition(employee.getPosition());
        if (employee.getDepartmentId()!= null) existing.setDepartmentId(employee.getDepartmentId());
        if (employee.getGender()     != null) existing.setGender(employee.getGender());
        if (employee.getSalary()     != null) existing.setSalary(employee.getSalary());
        if (employee.getHireDate()   != null) existing.setHireDate(employee.getHireDate());
        if (employee.getStatus()     != null) existing.setStatus(employee.getStatus());
        // Legacy fields kept for backward compatibility
        if (employee.getEmployeeName()!= null) existing.setEmployeeName(employee.getEmployeeName());
        if (employee.getDepartment() != null) existing.setDepartment(employee.getDepartment());

        return employeeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Employee existing = employeeRepository.findByEmployeeIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(existing);
    }
}
