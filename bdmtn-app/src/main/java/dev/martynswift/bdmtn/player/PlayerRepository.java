package dev.martynswift.bdmtn.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.checkedIn = :checkedIn")
    int countAllByCheckedIn(@Param("checkedIn") boolean checkedIn);

    @Query("SELECT p FROM Player p WHERE p.checkedIn = :checkedIn")
    List<Player> findAllByCheckedIn(@Param("checkedIn") boolean checkedIn);
}
