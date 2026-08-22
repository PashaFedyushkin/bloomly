package by.fedyushkin.bloomly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResponse {

    private String message;
    private long expiresInSeconds;
}
