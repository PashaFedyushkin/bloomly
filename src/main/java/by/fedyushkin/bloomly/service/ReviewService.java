package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    List<ReviewDto> getAll();

    ReviewDto getById(Long reviewId);

    ReviewDto getBySessionId(Long sessionId);

    List<ReviewDto> getByMasterId(Long masterId);

    Page<ReviewDto> getByMasterId(Long masterId, Pageable pageable);

    List<ReviewDto> getByUserId(Long userId);

    Page<ReviewDto> getByUserId(Long userId, Pageable pageable);

    ReviewDto create(Long sessionId, String text, Integer stars, List<MultipartFile> photos);

    ReviewDto update(Long reviewId, String text, Integer stars, List<MultipartFile> photos);

    void delete(Long reviewId);

    void deleteBySessionId(Long sessionId);
}
