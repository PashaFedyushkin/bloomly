package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class MasterServiceImpl implements MasterService {

    private final MasterRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MasterDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MasterDto getById(Long masterId) {
        return toDto(getMasterOrThrow(masterId));
    }

    @Override
    public MasterDto create(MasterDto masterDto) {
        User user = resolveUser(masterDto.getUserId());
        if (repository.existsById(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Master already exists for user: " + user.getId());
        }

        Master master = new Master();
        master.setUser(user);
        master.setUnp(masterDto.getUnp());
        return toDto(repository.save(master));
    }

    @Override
    public MasterDto update(Long masterId, MasterDto masterDto) {
        Master master = getMasterOrThrow(masterId);
        master.setUnp(masterDto.getUnp());
        return toDto(repository.save(master));
    }

    @Override
    public void delete(Long masterId) {
        if (!repository.existsById(masterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId);
        }
        repository.deleteById(masterId);
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private Master getMasterOrThrow(Long masterId) {
        return repository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private MasterDto toDto(Master master) {
        MasterDto dto = new MasterDto();
        dto.setId(master.getId());
        dto.setUnp(master.getUnp());
        if (master.getUser() != null) {
            dto.setUserId(master.getUser().getId());
        }
        return dto;
    }
}
