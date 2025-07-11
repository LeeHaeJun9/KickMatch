package com.example.kickmatch.service;

import com.example.kickmatch.domain.User;
import com.example.kickmatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))  // roles()에는 "USER", "ADMIN"만 넣어야 합니다.
                .build();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    public void updateUserRole(Long userId, String role) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    // 승인 관리용 메서드
    public List<User> findPendingAdmins() {
        return findByRole("ROLE_PENDING_ADMIN");
    }

    public void approveAdmin(Long userId) {
        updateUserRole(userId, "ROLE_ADMIN");
    }

    public void rejectAdmin(Long userId) {
        // 거절 시 삭제하거나 일반 사용자로 변경할 수 있음
        deleteUserById(userId);
        // 또는 아래처럼 권한 변경
        // updateUserRole(userId, "ROLE_USER");
    }

    // 회원 정보 수정 기능 추가
    @Transactional
    public void updateUserInfo(String username, String nickname, String email, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        user.setNickname(nickname);
        user.setEmail(email);

        // 비밀번호가 입력된 경우에만 변경
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user); // 선택사항: @Transactional이면 생략 가능
    }

    public boolean isNicknameDuplicate(String nickname, String currentUsername) {
        Optional<User> existing = userRepository.findByNickname(nickname);
        return existing.isPresent() && !existing.get().getUsername().equals(currentUsername);
    }

    public void deleteByUsername(String username) {
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }


}
