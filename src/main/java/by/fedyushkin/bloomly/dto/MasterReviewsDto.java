package by.fedyushkin.bloomly.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.Map;

@Data
public class MasterReviewsDto {

    private Page<ReviewDto> reviews;
    private Map<Integer, Long> starCounts;
}
