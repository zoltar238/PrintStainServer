package com.github.zoltar238.PrintStainServer.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "person")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personId;

    @NotBlank
    @Size(max = 80)
    private String name;


    @NotBlank
    @Size(max = 80)
    private String surname;

    @NotBlank
    @Size(max = 80)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Size(max = 80)
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @jdk.jfr.Timestamp
    @NotNull
    private Timestamp createDate;

    //fetch eager -> get every single role at once
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "person_role", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles;

    //fetch lazy -> get sales when needed
    //cascade persist -> persist changes, cascade merge -> merge changes when updated
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = SaleEntity.class, cascade = CascadeType.DETACH)
    @JoinTable(name = "person_sale", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "sale_id"))
    private Set<SaleEntity> sales;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<ItemEntity> items;
}
