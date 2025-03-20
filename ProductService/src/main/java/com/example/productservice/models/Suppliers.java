package com.example.productservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Suppliers extends AbstractModel {
    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = true)
    private String address;

//    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Users> users;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<GRN> grn;

    public Suppliers() {}

    public Suppliers(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Suppliers{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}