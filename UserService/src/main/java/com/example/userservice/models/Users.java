package com.example.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users extends AbstractModel {
//
//    // đặt giá trị mặc định chho thuộc tính
//    @Column(nullable = true, columnDefinition = "varchar(255) default ''")
//    private String name;
//
//    @Column(nullable = true, columnDefinition = "varchar(255) default ''")
//    private String avatar;
//
//    @Column(nullable = true)
//    @Pattern(regexp = "male|female", message = "Gender must be either 'male' or 'female'")
//    private String gender;
//
//    @Column(nullable = true, columnDefinition = "varchar(255) default ''")
//    private String email;
//
//    @Column(nullable = true, columnDefinition = "varchar(255) default ''")
//    private String username;
//
//    @Column(nullable = true, columnDefinition = "varchar(255) default ''")
//    private String password;

//    @ManyToOne
//    @JoinColumn(name = "roleId", nullable = false)
//    private Roles role;

    @Column(nullable = false)
    private String keycloakId;

    @Override
    public String toString() {
        return "Users{" +
                ", keycloakId='" + this.keycloakId + '\'' ;
    }

}