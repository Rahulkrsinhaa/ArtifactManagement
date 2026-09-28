package com.example.employee.app.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.employee.library.Employee;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @Test
    void updateChangesPersistedEmployee() {
        EmployeeEntity entity = new EmployeeEntity(employee("Old Name"));
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        Employee updated = service.update(7L, employee("New Name"));

        assertEquals("New Name", updated.fullName());
        verify(repository).save(entity);
    }

    @Test
    void updateMissingEmployeeFails() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> service.update(404L, employee("Nobody")));
    }

    private static Employee employee(String name) {
        return new Employee(null, name, "person@example.com", "Engineering", new BigDecimal("85000.00"));
    }
}