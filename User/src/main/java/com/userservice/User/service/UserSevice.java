package com.userservice.User.service;

import com.userservice.User.dto.UserRequestDTO;
import com.userservice.User.dto.UserResponseDTO;
import com.userservice.User.grpc.BillingServiceGrpcClient;
import com.userservice.User.mapper.UserMapper;
import com.userservice.User.model.user;
import com.userservice.User.repository.UserRepository;
import com.userservice.kafka.kafkaproducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserSevice {
    private UserRepository userRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final kafkaproducer kafkaProducer;
    private final RatingService ratingService;
    private final ReliabilityService reliabilityService;

    public UserSevice(UserRepository userRepository,
                      BillingServiceGrpcClient billingServiceGrpcClient,
                      kafkaproducer kafkaProducer,
                      RatingService ratingService,
                      ReliabilityService reliabilityService) {
        this.userRepository = userRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
        this.ratingService = ratingService;
        this.reliabilityService = reliabilityService;
    }
    public List<UserResponseDTO> getUsers (){
        List<user> users = userRepository.findAll();
        List<UserResponseDTO> userResponseDTOs = users.stream().map(UserMapper::toDTO).toList();
        return userResponseDTOs;
    }
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        user newUser = userRepository.save(UserMapper.toModel(userRequestDTO));

        billingServiceGrpcClient.createBillingAccount(
                String.valueOf(newUser.getId()),
                newUser.getUsername(),
                newUser.getEmail());

        kafkaProducer.sendUserCreatedEvent(newUser);

        return UserMapper.toDTO(newUser);
    }
    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO) {
        user User = userRepository.findById(id)
                .orElseThrow(() -> new com.userservice.User.exception.UserNotFoundException("User with id " + id + " does not exist"));
        User.setEmail(userRequestDTO.getEmail());
        User.setPhoneNumber(Integer.parseInt(userRequestDTO.getPhonenumber()));
        User.setUsername(userRequestDTO.getName());
        User.setPassword(userRequestDTO.getPassword());
        user updatedUser = userRepository.save(User);
        return UserMapper.toDTO(updatedUser);
    }
    @Transactional
    public void deleteUser(UUID id) {
        // Remove the player's ratings and attendance history along with the account.
        ratingService.deleteAllForUser(id);
        reliabilityService.deleteAllForUser(id);
        userRepository.deleteById(id);
    }
}
