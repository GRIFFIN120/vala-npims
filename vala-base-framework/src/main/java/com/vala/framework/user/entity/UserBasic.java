package com.vala.framework.user.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vala.base.convertor.EntityListSerializer;
import com.vala.base.convertor.PasswordSerializer;
import com.vala.framework.file.entity.ImageEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class UserBasic extends ImageEntity {
    @Column
    public String username;

    @JsonSerialize(using = PasswordSerializer.class)
    @Column
    public String password;

    public String affiliation;

    public String gender;

    public String phone;


    @Override
    public String toString() {
        return "UserBasic{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

    @Transient
    private String token;

    @JsonSerialize(using = EntityListSerializer.ListSerializer.class)
    @JsonDeserialize(using = EntityListSerializer.ListDeserializer.class)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_role",
            joinColumns={@JoinColumn(name="user_id")},inverseJoinColumns={@JoinColumn(name="role_id")}
    )
    public List<RoleBasic> roles;


}
