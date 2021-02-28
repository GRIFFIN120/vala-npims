package com.vala.framework.data.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.base.entity.TreeEntity;
import lombok.Data;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class DataGroupEntity extends TreeEntity {


    @JsonIgnore
    @OneToMany(mappedBy="group", cascade = {CascadeType.REMOVE},fetch = FetchType.LAZY)
    public List<DataBean> data = new ArrayList<>();

    @Transient
    public Integer count;

}
