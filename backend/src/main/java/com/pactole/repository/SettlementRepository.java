package com.pactole.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pactole.entities.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByGroupIdOrderBySettledOnDescIdDesc(UUID groupId);
}
