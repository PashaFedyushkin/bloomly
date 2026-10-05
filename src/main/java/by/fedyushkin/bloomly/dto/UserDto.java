package by.fedyushkin.bloomly.dto;

import jakarta.persistence.Column;
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
    private String vk;
    private String instagram;
    private String telegram;
}
