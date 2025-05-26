package com.api.musiconnect.repository;

import com.api.musiconnect.model.entity.Follow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerUserIdAndFollowedUserUserId(Long followerId, Long followedUserId);

    boolean existsByFollowerUserIdAndFollowedBandBandId(Long followerId, Long followedBandId);

    List<Follow> findByFollowerUserId(Long userId);

    Optional<Follow> findByFollowerUserIdAndFollowedUserUserId(Long followerId, Long followedUserId);
    
    Optional<Follow> findByFollowerUserIdAndFollowedBandBandId(Long followerId, Long followedBandId);

}
