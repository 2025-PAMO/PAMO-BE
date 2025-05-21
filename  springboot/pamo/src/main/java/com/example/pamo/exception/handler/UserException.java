package com.example.pamo.exception.handler;


import com.example.pamo.apiPayload.code.BaseErrorCode;
import com.example.pamo.apiPayload.exception.CustomException;

public class UserException  extends CustomException {
    public UserException(BaseErrorCode code){super(code);}

}