package com.devmaster.goatfarm.phone.model.entity;

import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Entity
@Table(
        name = "telefone",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ddd", "numero"})         }
)
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ddd", nullable = false)
    private String ddd;

    @Column(name = "numero", nullable = false)
    private String number;

    @ManyToOne
    @JoinColumn(name = "goat_farm_id", referencedColumnName = "id")
    private GoatFarm goatFarm;

    public Phone() {
    }

    public Phone(Long id, String ddd, String number, GoatFarm goatFarm) {
        this.id = id;
        this.ddd = ddd;
        this.number = number;
        this.goatFarm = goatFarm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDdd() {
        return ddd;
    }

    public void setDdd(String ddd) {
        this.ddd = ddd;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public GoatFarm getGoatFarm() {
        return goatFarm;
    }

    public void setGoatFarm(GoatFarm goatFarm) {
        this.goatFarm = goatFarm;
    }
}

