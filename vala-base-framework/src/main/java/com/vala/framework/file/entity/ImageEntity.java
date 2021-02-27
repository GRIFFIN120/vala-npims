package com.vala.framework.file.entity;

import com.vala.base.entity.FileColumn;
import lombok.Data;
import lombok.ToString;

import javax.persistence.MappedSuperclass;
@Data
@MappedSuperclass
@ToString(callSuper = true)
public class ImageEntity extends FileEntity {

    @FileColumn
    public String thumb;



}
