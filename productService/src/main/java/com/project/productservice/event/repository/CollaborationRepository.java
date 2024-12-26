package com.project.productservice.event.repository;

import com.project.productservice.event.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
}
