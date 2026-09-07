package com.enoca.flowpilot.service;

import com.enoca.flowpilot.core.entities.Employee;
import java.util.List;

public interface EmployeeService {
    Employee getById(Long id);
    Employee getByEmail(String email);
    List<Employee> getEmployeesByRole(String roleName);
}
