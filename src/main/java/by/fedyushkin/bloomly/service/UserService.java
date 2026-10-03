package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto getById(Long userId);

    UserDto create(UserDto userDto);

    UserDto createMaster(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto updatePhoto(Long userId, MultipartFile photo);

    void deletePhoto(Long userId);

    void delete(Long userId);

    List<String> getUserRoles(Long userId);

    boolean isPhoneExists(String phone);
}
