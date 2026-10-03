package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.ReviewDto;
import by.fedyushkin.bloomly.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController extends BaseController{

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewDto> getAll(
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long masterId
    ) {
        if (sessionId != null) {
            return List.of(reviewService.getBySessionId(sessionId));
        }
        if (masterId != null) {
            return reviewService.getByMasterId(masterId);
        }
        return reviewService.getAll();
    }

    @GetMapping("/master/my")
    public List<ReviewDto> getMasterReviews() {
        return reviewService.getByMasterId(getCurrentUserId());
    }

    @GetMapping("/user/my")
    public List<ReviewDto> getUserReviews() {
        return reviewService.getByUserId(getCurrentUserId());
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(
            @RequestParam Long sessionId,
            @RequestParam String text,
            @RequestParam Integer stars,
            @RequestParam("photos") List<MultipartFile> photos
    ) {
        return reviewService.create(sessionId, text, stars, photos);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReviewDto update(
            @PathVariable Long id,
            @RequestParam String text,
            @RequestParam Integer stars,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos
    ) {
        return reviewService.update(id, text, stars, photos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}
