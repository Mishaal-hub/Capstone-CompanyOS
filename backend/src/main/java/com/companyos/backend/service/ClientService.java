package com.companyos.backend.service;

import com.companyos.backend.dto.ClientDTO;
import com.companyos.backend.dto.DealDTO;

import java.util.List;

public interface ClientService {
    List<ClientDTO> getAllClients();
    ClientDTO getClientById(Long id);
    ClientDTO createClient(ClientDTO clientDTO);
    ClientDTO updateClient(Long id, ClientDTO clientDTO);
    void deleteClient(Long id);

    List<DealDTO> getAllDeals(String stage);
    DealDTO createDeal(DealDTO dealDTO);
    DealDTO updateDealStage(Long id, String stage);
    void deleteDeal(Long id);
}
