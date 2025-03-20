package com.example.productservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "grn")
@Getter
@Setter
public class GRN extends AbstractModel {

    @Column(nullable = false)
    private String receiptId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date receiptDate;

    @Column(nullable = true)
    private int totalPrice;

    @Column(nullable = true)
    @Pattern(regexp = "pending|completed|cancelled", message = "")
    private String status;

    @ManyToOne()
    @JoinColumn(name = "supplier_id", nullable = false)
    private Suppliers supplier;

    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GRN_Details> details;

    @Column(nullable = true)
    private Long userId;

    public GRN() {}

    @Override
    public String toString() {
        return "GRN{" +
                "receiptId='" + receiptId + '\'' +
                ", receiptDate=" + receiptDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", supplier=" + supplier +
                ", userId=" + userId +
                '}';
    }

}
