package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.util.List;

@Data
public class MasterFullDto {

    private Long id;
    private String description;
    private String unp;
    private String vk;
    private String instagram;
    private String telegram;
    private String photoUrl;
    private UserDto userDto;
    private Float rating;
    private Long reviewCount;
    private AddressDto address;
    private List<String> categories;
}
