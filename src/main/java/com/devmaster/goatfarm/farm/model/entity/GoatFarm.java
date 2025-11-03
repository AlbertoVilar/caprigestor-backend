package com.devmaster.goatfarm.farm.model.entity;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private User user;

        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

        @OneToMany(mappedBy = "goatFarm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones = new ArrayList<>();

        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "capril_id")
    private List<Goat> goats = new ArrayList<>();

    @CreatedDate
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime updatedAt;

    public GoatFarm() {}

    public GoatFarm(Long id, String name, String tod, User user, Address address) {
        this.id = id;
        this.name = name;
        this.tod = tod;
        this.user = user;
        this.address = address;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
        if (phones != null) {
            phones.forEach(phone -> phone.setGoatFarm(this));
        }
    }

    public List<Goat> getGoats() {
        return goats;
    }

    public void setGoats(List<Goat> goats) {
        this.goats = goats;
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

