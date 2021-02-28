package com.vala.commons.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class KV {

    public Object id;
    public Object name;
    public Object code;
    public Object label;
    public Object prop;

}
