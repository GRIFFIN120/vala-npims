package com.vala.base.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@MappedSuperclass//实体集成映射
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public Integer id;

    @Column
    public String domain;

    @Column
    public String name;
    @Column
    public String description;
    @Column
    public String date;
    @Column
    public String code;

    // 用于排序
    @JsonFormat( pattern="yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    @Column
    public Date timestamp;


//    @JsonFormat( pattern="yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    @JsonIgnore
    @CreatedDate
    @Column
    public Date createTime;

    @JsonIgnore
//    @JsonFormat( pattern="yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    @LastModifiedDate
    @Column
    public Date modifyTime;

}
