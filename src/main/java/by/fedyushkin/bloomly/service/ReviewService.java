package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ReviewDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    List<ReviewDto> getAll();

    ReviewDto getById(Long reviewId);

    ReviewDto getBySessionId(Long sessionId);

    List<ReviewDto> getByMasterId(Long masterId);

    List<ReviewDto> getByUserId(Long userId);

    ReviewDto create(Long sessionId, String text, Integer stars, List<MultipartFile> photos);

    ReviewDto update(Long reviewId, String text, Integer stars, List<MultipartFile> photos);

    void delete(Long reviewId);

    void deleteBySessionId(Long sessionId);
}
