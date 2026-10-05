package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class ServiceDto {

    private Long id;
    private String name;
    private String description;
    private Float price;
    private Integer durationMinutes;
    private Long masterId;
    private Long categoryId;
    private CategoryDto category;
}
