package com.example.orderservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "discounts")
@Getter
@Setter
public class Discounts extends AbstractModel {
    @Column(nullable = false)
    private String code;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private Integer percent;

    @Column(nullable = true)
    private Integer minOrderValue;

    @Column(nullable = true)
    private Integer count;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(nullable = true)
    @Pattern(regexp = "available|unavailable", message = "Status of discount must be in [available or unavailable]")
    private String status;

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Orders> orders;

    public Discounts() {}

}
