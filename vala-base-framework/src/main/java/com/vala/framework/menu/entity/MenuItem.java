package com.vala.framework.menu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.base.entity.TreeEntity;
import com.vala.framework.user.entity.RoleBasic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuItem extends TreeEntity {
    private String icon;
    private String path;


    @JsonIgnore
    @ManyToMany(mappedBy="menus",fetch = FetchType.LAZY)
    private List<RoleBasic> roles;

}
