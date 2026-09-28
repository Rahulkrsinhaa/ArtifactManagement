package com.example.employee.app.employee;

import com.example.employee.library.Employee;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "employee", key = "#p0")
    public Employee getById(Long id) {
        return repository.findById(id)
                .map(EmployeeEntity::toModel)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Cacheable(cacheNames = "employees")
    public List<Employee> getAll() {
        return repository.findAll().stream().map(EmployeeEntity::toModel).toList();
    }

    @Transactional
    @Caching(put = @CachePut(cacheNames = "employee", key = "#result.id"),
            evict = @CacheEvict(cacheNames = "employees", allEntries = true))
    public Employee create(Employee employee) {
        return repository.save(new EmployeeEntity(employee)).toModel();
    }

    @Transactional
    @Caching(put = @CachePut(cacheNames = "employee", key = "#p0"),
            evict = @CacheEvict(cacheNames = "employees", allEntries = true))
    public Employee update(Long id, Employee employee) {
        EmployeeEntity entity = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        entity.updateFrom(employee);
        return repository.save(entity).toModel();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "employee", key = "#p0"),
            @CacheEvict(cacheNames = "employees", allEntries = true)
    })
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }
}