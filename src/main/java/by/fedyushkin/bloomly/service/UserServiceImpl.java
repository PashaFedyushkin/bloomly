package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.Role;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.UserRepository;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long userId) {
        return toDto(getUserOrThrow(userId));
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setPhone(PhoneNumbers.normalize(userDto.getPhone()));
        user.setCreationDate(LocalDateTime.now());
        return toDto(repository.save(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = getUserOrThrow(userId);
        user.setName(userDto.getName());
        user.setPhone(PhoneNumbers.normalize(userDto.getPhone()));
        return toDto(repository.save(user));
    }

    @Override
    public void delete(Long userId) {
        if (!repository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        repository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoles(Long userId) {
        User user = getUserOrThrow(userId);
        if (user.getRoles() == null) {
            return List.of();
        }
        return user.getRoles().stream().map(Role::getName).toList();
    }

    private User getUserOrThrow(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setCreationDate(user.getCreationDate());
        dto.setPhone(user.getPhone());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(Role::getName).toList());
        } else {
            dto.setRoles(List.of());
        }
        return dto;
    }
}
