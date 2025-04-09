package com.devmaster.goatfarm.farm.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "capril")
@EntityListeners(AuditingEntityListener.class)
public class GoatFarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String name;
    @Column(name = "TOD", nullable = false)
    private String tod;

    @CreatedDate
    @Column(name = "criado", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "atualizado", nullable = false)
    private LocalDateTime updatedAt;

    public GoatFarm() {
    }

    public GoatFarm(Long id, String name, String tod) {

        this.id = id;
        this.name = name;
        this.tod = tod;

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

    public String getTod() {
        return tod;
    }

    public void setTod(String tod) {
        this.tod = tod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
