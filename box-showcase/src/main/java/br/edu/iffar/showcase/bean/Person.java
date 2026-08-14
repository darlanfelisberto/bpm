package br.edu.iffar.showcase.bean;

import java.io.Serializable;
import java.time.LocalDate;

/** Row of the synthetic dataset used to demonstrate b:datatable. */
public class Person implements Serializable {

    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDate hireDate;

    public Person() {
    }

    public Person(Long id, String name, String email, String role, LocalDate hireDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.hireDate = hireDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
