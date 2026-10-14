package com.pactole.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.pactole.entities.Member;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    
    List<Member> findByGroupIdOrderByIdAsc(UUID groupId);

    Optional<Member> findByGroupIdAndAccountId(UUID groupId, UUID accountId);

    boolean existsByGroupIdAndNameIgnoreCase(UUID groupId, String name);

    long countByGroupId(UUID groupId);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.accountId = :accountId where m.id = :memberId and m.groupId = :groupId and m.accountId is null")
    int claimIfFree(UUID memberId, UUID groupId, UUID accountId);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.accountId = null where m.accountId = :accountId")
    int detachUser(UUID accountId);
}
