package org.example.imagebackend.repository;

import org.example.imagebackend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ImageRepository extends JpaRepository<Image,Long> {
@Query("SELECT i FROM Image i WHERE LOWER(CONCAT(' ', i.description, ' ')) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Image> findByDescription(String description);
}
