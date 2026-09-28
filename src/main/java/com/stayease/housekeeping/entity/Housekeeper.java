package com.stayease.housekeeping.entity;

import java.util.ArrayList;
import java.util.List;

import com.stayease.housekeeping.enums.HousekeeperStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;



@Entity
@Table(name = "housekeepers")
public class Housekeeper {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String phone;
    @Column(nullable = false, unique = true)
    private String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private HousekeeperStatus status = HousekeeperStatus.AVAILABLE;

    @OneToMany(mappedBy = "housekeeper")
    private List<CleaningTask> tasks = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public HousekeeperStatus getStatus() { return status; }
    public void setStatus(HousekeeperStatus status) { this.status = status; }
    public List<CleaningTask> getTasks() { return tasks; }
    public void setTasks(List<CleaningTask> tasks) { this.tasks = tasks; }
}
