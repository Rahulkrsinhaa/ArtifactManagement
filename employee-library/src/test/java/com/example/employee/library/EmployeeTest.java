package com.example.employee.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EmployeeTest {
    @Test
    void trimsTextAndAcceptsValidEmployee() {
        Employee employee = new Employee(null, " Ada Lovelace ", "ada@example.com", "Engineering", new BigDecimal("10.00"));

        assertEquals("Ada Lovelace", employee.fullName());
    }

    @Test
    void rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Employee(null, "Ada Lovelace", "invalid", "Engineering", BigDecimal.TEN));
    }
}