package com.project.collaboration.event.repository;

import com.project.collaboration.event.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
}
