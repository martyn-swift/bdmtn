package dev.martynswift.bdmtn.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BdmtnSessionRepository extends JpaRepository<BdmtnSession, Long> {
    Optional<BdmtnSession> findBySessionId(String sessionId);
} 