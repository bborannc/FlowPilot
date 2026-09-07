package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.exception.ResourceNotFoundException;
import com.enoca.flowpilot.repository.EmployeeRepository;
import com.enoca.flowpilot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Çalışan bulunamadı: " + id));
    }

    @Override
    public Employee getByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Çalışan bulunamadı: " + email));
    }

    @Override
    public List<Employee> getEmployeesByRole(String roleName) {
        return employeeRepository.findByRoleName(roleName);
    }
}