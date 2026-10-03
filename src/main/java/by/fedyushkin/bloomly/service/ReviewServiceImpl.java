package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ReviewDto;
import by.fedyushkin.bloomly.entity.Review;
import by.fedyushkin.bloomly.entity.Session;
import by.fedyushkin.bloomly.repository.ReviewRepository;
import by.fedyushkin.bloomly.repository.SessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;
    private static final int MIN_PHOTOS = 1;
    private static final int MAX_PHOTOS = 3;
    private static final int MAX_TEXT_LENGTH = 1000;

    private final ReviewRepository repository;
    private final SessionRepository sessionRepository;
    private final MinioStorageService minioStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getById(Long reviewId) {
        return toDto(getReviewOrThrow(reviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getBySessionId(Long sessionId) {
        return repository.findBySessionId(sessionId)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found for session: " + sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getByMasterId(Long masterId) {
        return repository.findBySessionMasterId(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getByUserId(Long userId) {
        return repository.findBySessionUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ReviewDto create(Long sessionId, String text, Integer stars, List<MultipartFile> photos) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + sessionId));
        if (repository.existsBySessionId(sessionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for session: " + sessionId);
        }

        String normalizedText = requireText(text);
        int normalizedStars = requireStars(stars);
        List<MultipartFile> imageFiles = requirePhotos(photos, true);

        List<String> uploadedKeys = uploadPhotos(sessionId, imageFiles);
        try {
            Review review = new Review();
            review.setSession(session);
            review.setText(normalizedText);
            review.setStars(normalizedStars);
            review.setDate(LocalDateTime.now());
            review.setPhotoKeys(new ArrayList<>(uploadedKeys));
            return toDto(repository.save(review));
        } catch (RuntimeException e) {
            uploadedKeys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    @Override
    public ReviewDto update(Long reviewId, String text, Integer stars, List<MultipartFile> photos) {
        Review review = getReviewOrThrow(reviewId);
        review.setText(requireText(text));
        review.setStars(requireStars(stars));

        List<MultipartFile> imageFiles = requirePhotos(photos, false);
        if (imageFiles.isEmpty()) {
            return toDto(repository.save(review));
        }

        List<String> oldKeys = new ArrayList<>(review.getPhotoKeys());
        List<String> uploadedKeys = uploadPhotos(review.getSession().getId(), imageFiles);
        try {
            review.setPhotoKeys(new ArrayList<>(uploadedKeys));
            ReviewDto dto = toDto(repository.save(review));
            oldKeys.forEach(minioStorageService::delete);
            return dto;
        } catch (RuntimeException e) {
            uploadedKeys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    @Override
    public void delete(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        deleteReview(review);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        repository.findBySessionId(sessionId).ifPresent(this::deleteReview);
    }

    private void deleteReview(Review review) {
        List<String> keys = new ArrayList<>(review.getPhotoKeys());
        repository.delete(review);
        keys.forEach(minioStorageService::delete);
    }

    private List<String> uploadPhotos(Long sessionId, List<MultipartFile> photos) {
        List<String> keys = new ArrayList<>();
        try {
            for (MultipartFile photo : photos) {
                keys.add(minioStorageService.uploadReviewPhoto(sessionId, photo));
            }
            return keys;
        } catch (RuntimeException e) {
            keys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text is required");
        }
        String trimmed = text.trim();
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text must be at most 1000 characters");
        }
        return trimmed;
    }

    private int requireStars(Integer stars) {
        if (stars == null || stars < MIN_STARS || stars > MAX_STARS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Колличество звёзд должно быть от 1 до 5");
        }
        return stars;
    }

    private List<MultipartFile> requirePhotos(List<MultipartFile> photos, boolean required) {
        List<MultipartFile> files = photos == null
                ? List.of()
                : photos.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (required && files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 1 photo is required");
        }
        if (files.size() > MAX_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 3 photos are allowed");
        }
        if (!files.isEmpty() && files.size() < MIN_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 1 photo is required");
        }
        return files;
    }

    private Review getReviewOrThrow(Long reviewId) {
        return repository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + reviewId));
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setText(review.getText());
        dto.setStars(review.getStars());
        dto.setDate(review.getDate());
        if (review.getSession() != null) {
            dto.setSessionId(review.getSession().getId());
            if (review.getSession().getUser() != null) {
                var user = review.getSession().getUser();
                String firstName = user.getName() == null ? "" : user.getName();
                String lastName = user.getLastName() == null ? "" : user.getLastName();
                String fullName = (firstName + " " + lastName).trim();
                dto.setUserName(fullName.isEmpty() ? null : fullName);
            }
        }
        dto.setPhotoUrls(review.getPhotoKeys().stream()
                .map(minioStorageService::presignedUrl)
                .toList());
        return dto;
    }
}
