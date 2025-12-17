package hello.springblog.service;

import hello.springblog.domain.User;
import hello.springblog.dto.AddUserRequest;
import hello.springblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public Long save(AddUserRequest dto){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .build()).getId();
    }

    public User findById(Long userid){
        return userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
    // 메서드 추가
    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
}
