package com.vala.commons.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class KV {

    public Object id;
    public String name;
    public Object code;
    public String label;
    public String prop;

}
