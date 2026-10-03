package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDto {

    private Long id;
    private Long sessionId;
    private String text;
    private Integer stars;
    private LocalDateTime date;
    private String userName;
    private List<String> photoUrls;
}
