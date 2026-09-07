package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    Optional<Employee> findByEmail(String email);

    // Belirli bir roldeki tüm çalışanları bulur (onay akışında rol bazlı atama için)
    List<Employee> findByRoleName(String roleName);

    // Bir yöneticinin altındaki çalışanları listeler
    List<Employee> findByManagerId(Long managerId);
}
