package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AddressDto;
import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.dto.MasterFullDto;
import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.Address;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Role;
import by.fedyushkin.bloomly.entity.Session;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.AddressRepository;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.RoleRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@AllArgsConstructor
@Transactional
public class MasterServiceImpl implements MasterService {

    private final MasterRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PortfolioService portfolioService;
    private final RoleRepository roleRepository;
    private final MinioStorageService minioStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<MasterDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MasterFullDto> getMasters() {
        return repository.findAll().stream()
                .map(this::toFullDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MasterFullDto getById(Long masterId) {
        return toFullDto(getMasterOrThrow(masterId));
    }

    @Override
    public MasterDto create(MasterDto masterDto) {
        User user = resolveUser(masterDto.getUserId());
        if (repository.existsById(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Master already exists for user: " + user.getId());
        }
        Role role = roleRepository.findByName("MASTER_ROLE").orElse(null);
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        if (role != null && !user.getRoles().contains(role)) {
            user.getRoles().add(role);
        }
        Master master = new Master();
        master.setUser(user);
        master.setUnp(masterDto.getUnp());
        master.setVk(masterDto.getVk());
        master.setInstagram(masterDto.getInstagram());
        master.setTelegram(masterDto.getTelegram());
        master.setAddress(resolveAddress(masterDto.getAddressId()));
        return toDto(repository.save(master));
    }

    @Override
    public MasterDto update(Long masterId, MasterDto masterDto) {
        Master master = getMasterOrThrow(masterId);
        master.setUnp(masterDto.getUnp());
        master.setVk(masterDto.getVk());
        master.setInstagram(masterDto.getInstagram());
        master.setTelegram(masterDto.getTelegram());
        master.setAddress(resolveAddress(masterDto.getAddressId()));
        return toDto(repository.save(master));
    }

    @Override
    public void delete(Long masterId) {
        if (!repository.existsById(masterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId);
        }
        portfolioService.deleteByMasterId(masterId);
        repository.deleteById(masterId);
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private Address resolveAddress(Long addressId) {
        if (addressId == null) {
            return null;
        }
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found: " + addressId));
    }

    private Master getMasterOrThrow(Long masterId) {
        return repository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private MasterDto toDto(Master master) {
        MasterDto dto = new MasterDto();
        dto.setId(master.getId());
        dto.setUnp(master.getUnp());
        dto.setVk(master.getVk());
        dto.setInstagram(master.getInstagram());
        dto.setTelegram(master.getTelegram());
        if (master.getUser() != null) {
            dto.setUserId(master.getUser().getId());
        }
        if (master.getAddress() != null) {
            dto.setAddressId(master.getAddress().getId());
            dto.setAddress(toAddressDto(master.getAddress()));
        }
        return dto;
    }

    private MasterFullDto toFullDto(Master master) {
        MasterFullDto dto = new MasterFullDto();
        dto.setId(master.getId());
        dto.setUnp(master.getUnp());
        dto.setVk(master.getVk());
        dto.setInstagram(master.getInstagram());
        dto.setTelegram(master.getTelegram());
        if (master.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(master.getUser().getId());
            userDto.setName(master.getUser().getName());
            userDto.setLastName(master.getUser().getLastName());
            userDto.setCreationDate(master.getUser().getCreationDate());
            userDto.setPhone(master.getUser().getPhone());
            if (master.getUser().getPhotoKey() != null) {
                userDto.setPhotoUrl(minioStorageService.presignedUrl(master.getUser().getPhotoKey()));
            }
            dto.setUserDto(userDto);
        }
        if (master.getAddress() != null) {
            dto.setAddress(toAddressDto(master.getAddress()));
        }
        dto.setReviewCount(master.getSessions() == null ? 0 : master.getSessions().stream().map(Session::getReview).filter(Objects::nonNull).count());
        dto.setRating(dto.getReviewCount() > 0 ? 0 : 4.6F);
        return dto;
    }

    private AddressDto toAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setCountry(address.getCountry());
        dto.setRegion(address.getRegion());
        dto.setLocality(address.getLocality());
        dto.setDistrict(address.getDistrict());
        dto.setPlace(address.getPlace());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());
        dto.setApartment(address.getApartment());
        return dto;
    }
}
