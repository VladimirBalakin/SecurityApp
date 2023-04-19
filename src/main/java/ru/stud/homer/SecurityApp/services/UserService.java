package ru.stud.homer.SecurityApp.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stud.homer.SecurityApp.models.Role;
import ru.stud.homer.SecurityApp.models.User;
import ru.stud.homer.SecurityApp.repositories.UserRepository;
import ru.stud.homer.SecurityApp.security.UserDetailsImpl;

import java.util.*;

@Service
@Getter
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private RoleService roleService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional
    public void add(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        Role role = roleService.findRoleByName("ROLE_USER");
        Set<Role> roleSet = new HashSet<>(Set.of(role));
        user.setRoles(roleSet);
        Set<User> userSet = new HashSet<>(Set.of(user));
        role.setUsers(userSet);
        userRepository.save(user);
    }

    @Transactional
    public void update(Long id, User user) {
        User userToBeUpdated = findUserById(id);
        userToBeUpdated.setName(user.getName());
        userToBeUpdated.setEmail(user.getEmail());
        userToBeUpdated.setAge(user.getAge());
        if (!Objects.equals(user.getPassword(), "write_new_password")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            userToBeUpdated.setPassword(encodedPassword);
        }
    }

    @Transactional
    public void delete(Long id) {
        User userToDelete = userRepository.findUserById(id);
        if (userToDelete == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            userRepository.delete(userToDelete);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User userToLoad = userRepository.findUserByEmail(s);
        if (userToLoad == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            return new UserDetailsImpl(userToLoad);
        }
    }
}
