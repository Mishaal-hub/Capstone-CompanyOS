package com.companyos.backend.service;

import com.companyos.backend.entity.Employee;

import java.util.List;

public interface EmployeeService {

    Employee saveEmployee(Employee employee);

    List<Employee> getAllEmployees();

    Employee getEmployee(Long id);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);
}