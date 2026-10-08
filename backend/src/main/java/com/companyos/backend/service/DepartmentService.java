package com.companyos.backend.service;

import com.companyos.backend.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department saveDepartment(Department department);

    List<Department> getAllDepartments();

    Department getDepartment(Long id);

    Department updateDepartment(Long id, Department department);

    void deleteDepartment(Long id);
}