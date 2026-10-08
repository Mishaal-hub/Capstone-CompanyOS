package com.companyos.backend.service.impl;

import com.companyos.backend.entity.Company;
import com.companyos.backend.repository.CompanyRepository;
import com.companyos.backend.service.CompanyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @Override
    public Company updateCompany(Long id, Company company) {

        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        existingCompany.setCompanyName(company.getCompanyName());
        existingCompany.setEmail(company.getEmail());
        existingCompany.setPhone(company.getPhone());
        existingCompany.setAddress(company.getAddress());
        existingCompany.setWebsite(company.getWebsite());
        existingCompany.setIndustry(company.getIndustry());
        existingCompany.setLogoUrl(company.getLogoUrl());

        return companyRepository.save(existingCompany);
    }
    @Override
public void deleteCompany(Long id) {

    if (!companyRepository.existsById(id)) {
        throw new RuntimeException("Company not found");
    }

    companyRepository.deleteById(id);
}
}