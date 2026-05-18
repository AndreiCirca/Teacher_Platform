package com.example.TeacherPlatform.service.generic;

import com.example.TeacherPlatform.model.BaseEntity;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public abstract class GenericService<T extends BaseEntity, REQ, RES> {

    // Each concrete service will inject its own repository
    protected abstract BaseRepository<T> getRepository();

    protected abstract T toEntity(REQ request);
    protected abstract RES toResponse(T entity);
    protected abstract void updateEntity(T entity, REQ request);

    //Generic CRUD

    @Transactional(readOnly = true)
    public List<RES> findAll() {
        return getRepository().findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RES findById(Long id) {
        T entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource with id " + id + " was not found"));
        return toResponse(entity);
    }

    @Transactional
    public RES create(REQ request) {
        T entity = toEntity(request);
        T saved = getRepository().save(entity);
        return toResponse(saved);
    }

    @Transactional
    public RES update(Long id, REQ request) {
        T entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource with id " + id + " was not found"));
        updateEntity(entity, request);
        T saved = getRepository().save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        T entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource with id " + id + " was not found"));
        getRepository().delete(entity);
    }
}