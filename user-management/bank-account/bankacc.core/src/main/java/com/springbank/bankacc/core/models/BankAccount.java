package com.springbank.bankacc.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccount {

    @Id
    private String id;

    private String accountHouderId;

    private Date dateCreation;

    private AccountType accountType;

    private double balance;
}
