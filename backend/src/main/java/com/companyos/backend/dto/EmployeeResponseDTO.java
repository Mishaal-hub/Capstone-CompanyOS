package com.companyos.backend.dto;

import com.companyos.backend.entity.Employee;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outgoing payload for all employee API responses.
 * Also used by ProjectResponseDTO to describe project members.
 */
public class EmployeeResponseDTO {

    private Long      employeeId;
    private String    employeeCode;
    private String    firstName;
    private String    lastName;
    private String    fullName;
    private String    email;
    private String    phone;
    private String    gender;
    private Long      departmentId;
    private Long      companyId;
    private String    designation;
    private String    jobTitle;
    private Double    salary;
    private LocalDate hireDate;
    private String    status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeResponseDTO() {}

    /** Build a response DTO from an Employee entity. */
    public static EmployeeResponseDTO from(Employee e) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.employeeId   = e.getEmployeeId();
        dto.employeeCode = e.getEmployeeCode();
        dto.firstName    = e.getFirstName();
        dto.lastName     = e.getLastName();
        dto.fullName     = buildFullName(e.getFirstName(), e.getLastName());
        dto.email        = e.getEmail();
        dto.phone        = e.getPhone();
        dto.gender       = e.getGender();
        dto.departmentId = e.getDepartmentId();
        dto.companyId    = e.getCompanyId();
        dto.designation  = e.getDesignation();
        dto.jobTitle     = e.getPosition();   // position maps to job-title in the legacy schema
        dto.salary       = e.getSalary();
        dto.hireDate     = e.getHireDate();
        dto.status       = e.getStatus();
        // createdAt / updatedAt not on old entity - set to null gracefully
        dto.createdAt    = null;
        dto.updatedAt    = null;
        return dto;
    }

    private static String buildFullName(String first, String last) {
        if (first == null) return "";
        if (last == null || last.isBlank()) return first.trim();
        return first.trim() + " " + last.trim();
    }

    // ── Getters & setters ─────────────────────────────────────────────────

    public Long   getEmployeeId()                       { return employeeId; }
    public void   setEmployeeId(Long v)                 { this.employeeId = v; }

    public String getEmployeeCode()                     { return employeeCode; }
    public void   setEmployeeCode(String v)             { this.employeeCode = v; }

    public String getFirstName()                        { return firstName; }
    public void   setFirstName(String v)                { this.firstName = v; }

    public String getLastName()                         { return lastName; }
    public void   setLastName(String v)                 { this.lastName = v; }

    public String getFullName()                         { return fullName; }
    public void   setFullName(String v)                 { this.fullName = v; }

    public String getEmail()                            { return email; }
    public void   setEmail(String v)                    { this.email = v; }

    public String getPhone()                            { return phone; }
    public void   setPhone(String v)                    { this.phone = v; }

    public String getGender()                           { return gender; }
    public void   setGender(String v)                   { this.gender = v; }

    public Long   getDepartmentId()                     { return departmentId; }
    public void   setDepartmentId(Long v)               { this.departmentId = v; }

    public Long   getCompanyId()                        { return companyId; }
    public void   setCompanyId(Long v)                  { this.companyId = v; }

    public String getDesignation()                      { return designation; }
    public void   setDesignation(String v)              { this.designation = v; }

    public String getJobTitle()                         { return jobTitle; }
    public void   setJobTitle(String v)                 { this.jobTitle = v; }

    public Double getSalary()                           { return salary; }
    public void   setSalary(Double v)                   { this.salary = v; }

    public LocalDate getHireDate()                      { return hireDate; }
    public void      setHireDate(LocalDate v)           { this.hireDate = v; }

    public String getStatus()                           { return status; }
    public void   setStatus(String v)                   { this.status = v; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)  { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v)  { this.updatedAt = v; }
}
