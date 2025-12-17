package hello.springblog.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name ="email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname" , unique = true)
    private String nickname;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override // 사용자의 id를 반환(고유한 값)
    public String getUsername(){
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되어있는지 확인하는 로직
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        // 패스워드가 만료되었는지 확인하는 로직
        return true;
    }


    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true;
    }

    // 사용자 이름 변경
    public User update(String nickname){
        this.nickname = nickname;

        return this;
    }
}
