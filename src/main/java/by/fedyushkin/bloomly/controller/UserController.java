package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController extends BaseController{

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/current")
    public UserDto getCurrent() {
        return userService.getById(getCurrentUserId());
    }

    @GetMapping("/roles")
    public List<String> getUserRoles() {
        return userService.getUserRoles(getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PutMapping
    public UserDto update(@RequestBody UserDto userDto) {
        userDto.setId(getCurrentUserId());
        return userService.update(getCurrentUserId(), userDto);
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto updatePhoto(@RequestParam("photo") MultipartFile photo) {
        return userService.updatePhoto(getCurrentUserId(), photo);
    }

    @DeleteMapping("/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto() {
        userService.deletePhoto(getCurrentUserId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.delete(getCurrentUserId());
    }
}
