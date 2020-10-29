package com.vala.commons.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseResult<T> {

    private Integer code = 200;
    private String message ;
    private T data;

    public ResponseResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseResult(String message) {
        this.message = message;
    }
    public ResponseResult(T data) {
        this.data = data;
    }
    public ResponseResult(String message, T data) {
        this.data = data;
        this.message = message;
    }
}
