package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class MasterDto {

    private Long id;
    private String unp;
    private String vk;
    private String instagram;
    private String telegram;
    private String photoUrl;
    private Long userId;
    private Long addressId;
    private AddressDto address;
}
