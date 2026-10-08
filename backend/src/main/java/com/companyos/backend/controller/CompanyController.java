package com.companyos.backend.controller;

import com.companyos.backend.entity.Company;
import com.companyos.backend.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PostMapping("/companies")
    public Company createCompany(@RequestBody Company company) {
        return service.createCompany(company);
    }

    @GetMapping("/companies")
    public List<Company> getAllCompanies() {
        return service.getAllCompanies();
    }

    @GetMapping("/companies/{id}")
    public Company getCompany(@PathVariable Long id) {
        return service.getCompanyById(id);
    }

    @PutMapping("/companies/{id}")
    public Company updateCompany(
            @PathVariable Long id,
            @RequestBody Company company) {
        return service.updateCompany(id, company);
    }

    @DeleteMapping("/companies/{id}")
    public String deleteCompany(@PathVariable Long id) {
        service.deleteCompany(id);
        return "Company deleted successfully";
    }
}