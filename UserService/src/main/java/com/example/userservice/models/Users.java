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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String avatar;

    @Column(nullable = false)
    @Pattern(regexp = "male|female", message = "Gender must be either 'male' or 'female'")
    private String gender;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "roleId", nullable = false)
    private Roles role;

    @Override
    public String toString() {
        return "Users{" +
                ", name='" + name + '\'' ;
    }

}