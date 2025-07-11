package com.example.kickmatch.repository;

import com.example.kickmatch.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
