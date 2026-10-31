package com.yessinCoding.security;

import com.yessinCoding.entity.User;
import com.yessinCoding.repository.RoleRepository;
import com.yessinCoding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault("name", "OAuth User");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getName();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email is required for OAuth2 login");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    var userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));
                    String[] splitName = name.split(" ", 2);
                    String firstName = splitName.length > 0 ? splitName[0] : "OAuth";
                    String lastName = splitName.length > 1 ? splitName[1] : "User";

                    return User.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .password(UUID.randomUUID().toString())
                            .provider(provider)
                            .providerId(providerId)
                            .accountLocked(false)
                            .enabled(true)
                            .roles(java.util.List.of(userRole))
                            .build();
                });

        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEnabled(true);
        user.setAccountLocked(false);
        userRepository.save(user);

        return oauth2User;
    }
}
