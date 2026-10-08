package com.companyos.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Incoming payload for POST /api/employees and PUT /api/employees/{id}.
 *
 * employeeCode is not accepted from the client - it is generated server-side.
 * Role / privilege escalation is prevented at the controller level.
 */
public class EmployeeRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Pattern(
        regexp = "^[+]?[0-9\\s\\-().]{7,20}$|^$",
        message = "Phone number format is invalid"
    )
    private String phone;

    private String gender;
    private Long   departmentId;
    private Long   companyId;
    private String designation;
    private String jobTitle;
    private Double salary;
    private LocalDate hireDate;

    /** Accepted values: ACTIVE | INACTIVE | ON_LEAVE | TERMINATED */
    private String status;

    public EmployeeRequestDTO() {}

    public String    getFirstName()                    { return firstName; }
    public void      setFirstName(String v)            { this.firstName = v; }

    public String    getLastName()                     { return lastName; }
    public void      setLastName(String v)             { this.lastName = v; }

    public String    getEmail()                        { return email; }
    public void      setEmail(String v)                { this.email = v; }

    public String    getPhone()                        { return phone; }
    public void      setPhone(String v)                { this.phone = v; }

    public String    getGender()                       { return gender; }
    public void      setGender(String v)               { this.gender = v; }

    public Long      getDepartmentId()                 { return departmentId; }
    public void      setDepartmentId(Long v)           { this.departmentId = v; }

    public Long      getCompanyId()                    { return companyId; }
    public void      setCompanyId(Long v)              { this.companyId = v; }

    public String    getDesignation()                  { return designation; }
    public void      setDesignation(String v)          { this.designation = v; }

    public String    getJobTitle()                     { return jobTitle; }
    public void      setJobTitle(String v)             { this.jobTitle = v; }

    public Double    getSalary()                       { return salary; }
    public void      setSalary(Double v)               { this.salary = v; }

    public LocalDate getHireDate()                     { return hireDate; }
    public void      setHireDate(LocalDate v)          { this.hireDate = v; }

    public String    getStatus()                       { return status; }
    public void      setStatus(String v)               { this.status = v; }
}
