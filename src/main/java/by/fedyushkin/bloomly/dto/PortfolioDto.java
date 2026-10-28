package by.fedyushkin.bloomly.dto;

import lombok.Data;

@Data
public class PortfolioDto {

    private Long id;
    private Long masterId;
    private String description;
    private String photoUrl;
}
