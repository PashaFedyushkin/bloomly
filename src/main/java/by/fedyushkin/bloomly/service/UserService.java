package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.User;

import java.util.List;

public interface UserService {

    List<UserDto> getAll();

    UserDto getById(Long userId);

    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    void delete(Long userId);

    List<String> getUserRoles(Long userId);
}
