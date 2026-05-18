package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long> {
    
    Optional<T> findById(Long id);
    
    List<T> findAll();
    
    <S extends T> S save(S entity);
    
    <S extends T> List<S> saveAll(Iterable<S> entities);
    
    boolean existsById(Long id);
    
    long count();
    
    void deleteById(Long id);
    
    void delete(T entity);
    
    void deleteAll(Iterable<? extends T> entities);
    
    void deleteAll();
}

