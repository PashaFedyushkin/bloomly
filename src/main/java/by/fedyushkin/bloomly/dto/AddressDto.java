package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class AddressDto {

    private Long id;
    private String country;
    private String region;
    private String locality;
    private String district;
    private String place;
    private String street;
    private String house;
    private String apartment;
}
