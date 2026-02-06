package com.union.brainrush.repository;

import com.union.brainrush.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    @Transactional
    void deleteByUuid(String uuid);
}
