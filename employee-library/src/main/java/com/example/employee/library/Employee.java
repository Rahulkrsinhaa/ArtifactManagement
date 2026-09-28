package com.example.employee.library;

import java.math.BigDecimal;

/** Shared employee domain type published as a Maven artifact. */
public record Employee(Long id, String fullName, String email, String department, BigDecimal salary) {
    public Employee {
        fullName = requireText(fullName, "fullName");
        email = requireText(email, "email");
        department = requireText(department, "department");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("email must contain @");
        }
        if (salary == null) {
            throw new IllegalArgumentException("salary is required");
        }
        if (salary.signum() < 0) {
            throw new IllegalArgumentException("salary must be zero or greater");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}