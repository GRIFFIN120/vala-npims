package com.vala.jsoup.example;

import com.vala.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Cncp extends BaseEntity {
    private String name;
    private String date;
    private Double deal;
    private Double amount;
    private Double num;
    private String uptime;
}
