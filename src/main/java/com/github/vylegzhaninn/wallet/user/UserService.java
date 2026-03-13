package com.github.vylegzhaninn.wallet.user;

import com.github.vylegzhaninn.wallet.exception.AlreadyExistsException;
import com.github.vylegzhaninn.wallet.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(UserDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException("User with email " + request.email() + " already exists");
        }
        if (userRepository.existsByName(request.name())) {
            throw new AlreadyExistsException("User with name " + request.name() + " already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .build();
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User update(Long id, UserDto request) {
        User user = getById(id);

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }

        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
