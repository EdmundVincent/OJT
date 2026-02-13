package com.example.staffassign.service;

import com.example.staffassign.entity.CustomerMaster;
import com.example.staffassign.repository.CustomerMasterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerMasterMapper customerMasterMapper;

    public CustomerService(CustomerMasterMapper customerMasterMapper) {
        this.customerMasterMapper = customerMasterMapper;
    }

    public List<CustomerMaster> findAll() {
        return customerMasterMapper.findAll();
    }

    public CustomerMaster findById(Long id) {
        return customerMasterMapper.findById(id).orElse(null);
    }

    @Transactional
    public void create(CustomerMaster customer) {
        customerMasterMapper.insert(customer);
    }

    @Transactional
    public void update(Long id, CustomerMaster customer) {
        customer.setId(id);
        customerMasterMapper.update(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerMasterMapper.delete(id);
    }
}
