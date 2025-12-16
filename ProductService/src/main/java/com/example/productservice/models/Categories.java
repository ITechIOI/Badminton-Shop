package com.example.productservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "categories")
@Getter
@Setter
public class Categories extends AbstractModel {
    @NotBlank(message = "Category name is required")
    @Column(nullable = false)
    private String name;

    @Positive(message = "Count must be a positive number")
    @Column(nullable = true)
    private Integer count;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Products> products;

    public Categories() {}

    public Categories(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Categories{" +
                "name='" + name + '\'' +
                '}';
    }

    public Categories(Long id, String name) {
        this.setId(id);
        this.name = name;
        this.count = 0;
    }

}
