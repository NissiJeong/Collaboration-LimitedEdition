package com.project.orderservice.event.repository;

import com.project.orderservice.event.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
}
