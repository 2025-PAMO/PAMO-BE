package com.example.demo.exception.handler;


import com.example.demo.apiPayload.code.BaseErrorCode;
import com.example.demo.apiPayload.exception.CustomException;

public class UserException  extends CustomException {
    public UserException(BaseErrorCode code){super(code);}

}