package com.example.employee.app.employee;

import com.example.employee.library.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "employees")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    protected EmployeeEntity() {
    }

    EmployeeEntity(Employee employee) {
        updateFrom(employee);
    }

    void updateFrom(Employee employee) {
        fullName = employee.fullName();
        email = employee.email();
        department = employee.department();
        salary = employee.salary();
    }

    Employee toModel() {
        return new Employee(id, fullName, email, department, salary);
    }
}