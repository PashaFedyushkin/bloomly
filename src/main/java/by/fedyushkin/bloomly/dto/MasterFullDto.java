package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class MasterFullDto {

    private Long id;
    private String unp;
    private String vk;
    private String instagram;
    private String telegram;
    private UserDto userDto;
    private Float rating;
    private Long reviewCount;
    private AddressDto address;
}
