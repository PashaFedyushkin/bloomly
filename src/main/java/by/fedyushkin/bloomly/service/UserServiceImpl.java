package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.Role;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.RoleRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import by.fedyushkin.bloomly.util.PhoneNumbers;
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
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final MinioStorageService minioStorageService;

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
        User user = repository.findByPhone(userDto.getPhone()).orElse(new User());
        if (user.getId() == null) {
            user.setName(userDto.getName());
            user.setLastName(userDto.getLastName());
            user.setPhone(PhoneNumbers.normalize(userDto.getPhone()));
            user.setCreationDate(LocalDateTime.now());
        }
        userDto.getRoles().stream().forEach( roleName -> {
            Role role = roleRepository.findByName(roleName).orElse(null);
            if (user.getRoles() == null) {
                user.setRoles(new ArrayList<>());
            }
            if (role != null && user.getRoles().contains(role)) {
                throw new RuntimeException("record exists");
            }
            if (role != null && !user.getRoles().contains(role)) {
                user.getRoles().add(role);
            }
        });
        return toDto(repository.save(user));
    }

    @Override
    public UserDto createMaster(UserDto userDto) {
        User user = getUserOrThrow(userDto.getId());
        Role role = roleRepository.findByName("MASTER_ROLE").orElse(null);
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        if (role != null && !user.getRoles().contains(role)) {
            user.getRoles().add(role);
        }
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
    public UserDto updatePhoto(Long userId, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo is required");
        }
        User user = getUserOrThrow(userId);
        String oldKey = user.getPhotoKey();
        String newKey = minioStorageService.uploadProfilePhoto(userId, photo);
        user.setPhotoKey(newKey);
        UserDto dto = toDto(repository.save(user));
        if (oldKey != null) {
            minioStorageService.delete(oldKey);
        }
        return dto;
    }

    @Override
    public void deletePhoto(Long userId) {
        User user = getUserOrThrow(userId);
        String oldKey = user.getPhotoKey();
        if (oldKey == null) {
            return;
        }
        user.setPhotoKey(null);
        repository.save(user);
        minioStorageService.delete(oldKey);
    }

    @Override
    public void delete(Long userId) {
        if (!repository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        repository.findById(userId).ifPresent(user -> {
            if (user.getPhotoKey() != null) {
                minioStorageService.delete(user.getPhotoKey());
            }
        });
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

    @Override
    public boolean isPhoneExists(String phone) {
        return repository.findByPhone(phone).isPresent();
    }

    private User getUserOrThrow(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setCreationDate(user.getCreationDate());
        dto.setPhone(user.getPhone());
        if (user.getPhotoKey() != null) {
            dto.setPhotoUrl(minioStorageService.presignedUrl(user.getPhotoKey()));
        }
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(Role::getName).toList());
        } else {
            dto.setRoles(List.of());
        }
        return dto;
    }
}
