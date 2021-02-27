package com.vala.framework.file.entity;

import com.vala.base.entity.BaseEntity;
import com.vala.base.entity.FileColumn;
import com.vala.base.entity.TreeEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@ToString(callSuper = true)
public class FileEntity extends TreeEntity {

    @FileColumn
    public String url;

    public String fileName;

    public Long size;

    public String extension;

    public String server;
}
