package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortfolioDto {

    private Long id;
    private Long masterId;
    private String description;
    private List<String> photoUrls;
}
