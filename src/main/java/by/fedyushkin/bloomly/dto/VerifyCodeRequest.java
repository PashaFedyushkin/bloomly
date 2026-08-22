package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class VerifyCodeRequest {

    private String phone;
    private String code;
}
