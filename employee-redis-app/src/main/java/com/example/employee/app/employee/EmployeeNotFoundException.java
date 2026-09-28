package com.example.employee.app.employee;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee " + id + " was not found");
    }
}