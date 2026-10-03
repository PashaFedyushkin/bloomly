package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {

    private Long id;
    private String name;
    private String lastName;
    private LocalDateTime creationDate;
    private String phone;
    private List<String> roles;
    private String photoUrl;
}
