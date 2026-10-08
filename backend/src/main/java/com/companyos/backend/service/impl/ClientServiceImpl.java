package com.companyos.backend.service.impl;

import com.companyos.backend.dto.ClientDTO;
import com.companyos.backend.dto.DealDTO;
import com.companyos.backend.entity.Client;
import com.companyos.backend.entity.Deal;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.ClientRepository;
import com.companyos.backend.repository.DealRepository;
import com.companyos.backend.repository.EmployeeRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.ClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final DealRepository dealRepository;
    private final EmployeeRepository employeeRepository;

    public ClientServiceImpl(ClientRepository clientRepository, DealRepository dealRepository, EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.dealRepository = dealRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        Long orgId = TenantContext.getRequiredTenantId();
        return clientRepository.findByOrganizationId(orgId).stream().map(this::mapClientToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Client client = clientRepository.findByClientIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
        return mapClientToDTO(client);
    }

    @Override
    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        Client client = new Client();
        client.setOrganizationId(orgId);
        client.setCompanyName(dto.getCompanyName().trim());
        client.setContactName(dto.getContactName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setIndustry(dto.getIndustry());
        client.setAddress(dto.getAddress());
        client.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        client.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        client.setNotes(dto.getNotes());

        Client saved = clientRepository.save(client);
        return mapClientToDTO(saved);
    }

    @Override
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        Client client = clientRepository.findByClientIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        client.setCompanyName(dto.getCompanyName().trim());
        client.setContactName(dto.getContactName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setIndustry(dto.getIndustry());
        client.setAddress(dto.getAddress());
        if (dto.getStatus() != null) client.setStatus(dto.getStatus());
        client.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        client.setNotes(dto.getNotes());

        return mapClientToDTO(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Client client = clientRepository.findByClientIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
        clientRepository.delete(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DealDTO> getAllDeals(String stage) {
        Long orgId = TenantContext.getRequiredTenantId();
        List<Deal> deals;
        if (stage != null && !stage.isBlank()) {
            deals = dealRepository.findByOrganizationIdAndStage(orgId, stage.toUpperCase());
        } else {
            deals = dealRepository.findByOrganizationId(orgId);
        }
        return deals.stream().map(this::mapDealToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DealDTO createDeal(DealDTO dto) {
        Long orgId = TenantContext.getRequiredTenantId();
        clientRepository.findByClientIdAndOrganizationId(dto.getClientId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", dto.getClientId()));

        Deal deal = new Deal();
        deal.setOrganizationId(orgId);
        deal.setClientId(dto.getClientId());
        deal.setTitle(dto.getTitle().trim());
        deal.setValue(dto.getValue() != null ? dto.getValue() : 0.0);
        deal.setStage(dto.getStage() != null ? dto.getStage().toUpperCase() : "LEAD");
        deal.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        deal.setExpectedCloseDate(dto.getExpectedCloseDate());
        deal.setProbability(dto.getProbability() != null ? dto.getProbability() : 50);

        return mapDealToDTO(dealRepository.save(deal));
    }

    @Override
    @Transactional
    public DealDTO updateDealStage(Long id, String stage) {
        Long orgId = TenantContext.getRequiredTenantId();
        Deal deal = dealRepository.findByDealIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", "id", id));
        deal.setStage(stage.toUpperCase().trim());
        return mapDealToDTO(dealRepository.save(deal));
    }

    @Override
    @Transactional
    public void deleteDeal(Long id) {
        Long orgId = TenantContext.getRequiredTenantId();
        Deal deal = dealRepository.findByDealIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal", "id", id));
        dealRepository.delete(deal);
    }

    private ClientDTO mapClientToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(client.getClientId());
        dto.setOrganizationId(client.getOrganizationId());
        dto.setCompanyName(client.getCompanyName());
        dto.setContactName(client.getContactName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setIndustry(client.getIndustry());
        dto.setAddress(client.getAddress());
        dto.setStatus(client.getStatus());
        dto.setAssignedEmployeeId(client.getAssignedEmployeeId());
        dto.setNotes(client.getNotes());
        dto.setCreatedAt(client.getCreatedAt());

        if (client.getAssignedEmployeeId() != null) {
            employeeRepository.findById(client.getAssignedEmployeeId()).ifPresent(emp -> {
                dto.setAssignedEmployeeName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            });
        }
        return dto;
    }

    private DealDTO mapDealToDTO(Deal deal) {
        DealDTO dto = new DealDTO();
        dto.setDealId(deal.getDealId());
        dto.setOrganizationId(deal.getOrganizationId());
        dto.setClientId(deal.getClientId());
        dto.setTitle(deal.getTitle());
        dto.setValue(deal.getValue());
        dto.setStage(deal.getStage());
        dto.setAssignedEmployeeId(deal.getAssignedEmployeeId());
        dto.setExpectedCloseDate(deal.getExpectedCloseDate());
        dto.setProbability(deal.getProbability());
        dto.setCreatedAt(deal.getCreatedAt());

        clientRepository.findById(deal.getClientId()).ifPresent(c -> dto.setClientName(c.getCompanyName()));

        if (deal.getAssignedEmployeeId() != null) {
            employeeRepository.findById(deal.getAssignedEmployeeId()).ifPresent(emp -> {
                dto.setAssignedEmployeeName((emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim());
            });
        }
        return dto;
    }
}
