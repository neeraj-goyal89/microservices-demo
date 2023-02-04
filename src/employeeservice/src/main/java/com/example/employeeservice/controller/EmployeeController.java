package com.example.employeeservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import com.example.employeeservice.model.Employee;
import com.example.employeeservice.repository.EmployeeRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    private static MeterRegistry _registry;
    private static HashMap<String, Integer> _employee_search_count_map;
    @Autowired
    EmployeeRepository repository;

    public Supplier<Number> fetchUserCount(String id) {
        return ()->_employee_search_count_map.get(id);
    }

    EmployeeController(MeterRegistry registry)
    {
        _registry = registry;
        _employee_search_count_map = new HashMap<>();
    }

    @PostMapping("/")
    public Employee add(@RequestBody Employee employee) {
        LOGGER.info("Employee add: {}", employee);

        return repository.save(employee);
    }

    @GetMapping("/{id}")
    public Employee findById(@PathVariable("id") String id) {
        LOGGER.info("Employee find: id={}", id);
        if(_employee_search_count_map.containsKey(id))
        {
            Integer employeeSearchCount = _employee_search_count_map.get(id);
            _employee_search_count_map.put(id, employeeSearchCount + 1);
        }

        Gauge.builder("employeecontoller.employeesearchcount",fetchUserCount(id)).
                tag("version","v1").
                description("employeecontroller descrip").
                register(_registry);
        return repository.findById(id).get();
    }

    @Timed(value="employee.get.value",description="time to retrieve all employees",percentiles={0.75,0.9,0.95})
    @GetMapping("/")
    public Iterable<Employee> findAll() {
        LOGGER.info("Employee find");
        return repository.findAll();
    }

    @GetMapping("/department/{departmentId}")
    public List<Employee> findByDepartment(@PathVariable("departmentId") String departmentId) {
        LOGGER.info("Employee find: departmentId={}", departmentId);
        return repository.findByDepartmentId(departmentId);
    }

    @GetMapping("/organization/{organizationId}")
    public List<Employee> findByOrganization(@PathVariable("organizationId") String organizationId) {
        LOGGER.info("Employee find: organizationId={}", organizationId);
        return repository.findByOrganizationId(organizationId);
    }

}