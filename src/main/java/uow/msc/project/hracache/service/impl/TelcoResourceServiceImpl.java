package uow.msc.project.hracache.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import uow.msc.project.hracache.exception.ResourceNotFoundException;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.repository.TelcoResourceRepository;
import uow.msc.project.hracache.service.TelcoResourceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TelcoResourceServiceImpl implements TelcoResourceService {

    private final TelcoResourceRepository telcoResourceRepository;
    private final RedisTemplate<String, TelcoResourceEntity> redisTemplate;
    private ArrayList telcoResourceEntityList = new ArrayList();

    @Override
    public void refreshAhead() {
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        List<TelcoResourceEntity> telcoResourceEntities = telcoResourceRepository.findAll();

        // Cache is pre-loaded with fetched data from database
        for (TelcoResourceEntity telcoResourceEntity : telcoResourceEntities) {
            String key = "resource_" + telcoResourceEntity.getId();
            final boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey) {
                operations.set(key, telcoResourceEntity);
                System.out.println("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + telcoResourceEntity);
            }
        }
    }

    @Override
    public TelcoResourceEntity findTelcoResourceById(Long id) throws NullPointerException {

        // Cache key generation
        String key = "resource_" + id;
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = redisTemplate.hasKey(key);

        // Retrieval of data from Cache, if it's a Cache-hit
        if (hasKey) {
            final TelcoResourceEntity post = operations.get(key);
            System.out.println("TELCO-RESOURCE-SERVICE: FROM REDIS: key: " + key + " value: " + post);
            log.info("TelcoResourceServiceImpl.findTelcoResourceById() : cache post >> " + post.toString());
            return post;
        }

        // Retrieval of data from database in case of a Cache-miss
        final Optional<TelcoResourceEntity> telcoResource = telcoResourceRepository.findById(id);
        if (telcoResource.isPresent()) {
            operations.set(key, telcoResource.get());
            System.out.println("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + telcoResource.get());
            log.info("TelcoResourceServiceImpl.findTelcoResourceById() : cache insert >> " + telcoResource.get().toString());
            return telcoResource.get();
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public Page<TelcoResourceEntity> getAllTelcoResources(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("id")));
        return telcoResourceRepository.findAll(pageable);
    }

    @Override
    public TelcoResourceEntity saveTelcoResource(TelcoResourceEntity telcoResourceEntity) {
        return telcoResourceRepository.save(telcoResourceEntity);
    }

    @Override
    public TelcoResourceEntity saveTelcoResourceToCacheAndDb(TelcoResourceEntity telcoResourceEntity) {
        TelcoResourceEntity dbTelcoResource = telcoResourceRepository.save(telcoResourceEntity);
        String key = "resource_" + dbTelcoResource.getId();
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = redisTemplate.hasKey(key);
        if (!hasKey) {
            operations.set(key, dbTelcoResource);
            System.out.println("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + dbTelcoResource);
        }
        return dbTelcoResource;
    }

    @Override
    public TelcoResourceEntity saveTelcoResourceToCacheAndAsyncDb(TelcoResourceEntity telcoResourceEntity, String bufferSize) {
        // Cache key generation
        String key = "resource_" + telcoResourceEntity.getId();

        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = redisTemplate.hasKey(key);

        // Data is written to Cache if unavailable
        if (!hasKey) {
            operations.set(key, telcoResourceEntity);
            System.out.println("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + telcoResourceEntity);
        }

        // Buffering cached data
        telcoResourceEntityList.add(telcoResourceEntity);

        // Data is written to database asynchronously when the default/user-defined buffer size is reached
        if (telcoResourceEntityList.size() == Integer.parseInt(bufferSize)) {
            for (Object resource : telcoResourceEntityList) {
                telcoResourceRepository.save((TelcoResourceEntity) resource);
            }
            telcoResourceEntityList.clear();
        }
        return telcoResourceEntity;
    }

    @Override
    public TelcoResourceEntity updateTelcoResource(Long id, TelcoResourceEntity telcoResourceEntity) throws NullPointerException {
        final String key = "resource_" + id;
        final boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            redisTemplate.delete(key);
            System.out.println("TELCO-RESOURCE-SERVICE: DELETED FROM REDIS: key: " + key + " value: " + telcoResourceEntity);
            log.info("TelcoResourceServiceImpl.updateTelcoResource() : cache delete >> " + telcoResourceEntity.toString());
        }
        telcoResourceEntity.setId(id);
        return telcoResourceRepository.save(telcoResourceEntity);
    }

    @Override
    public void deleteTelcoResource(Long id) throws NullPointerException {
        final String key = "resource_" + id;
        final boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            redisTemplate.delete(key);
            System.out.println("TELCO-RESOURCE-SERVICE: DELETED FROM REDIS: key: " + key);
            log.info("TelcoResourceServiceImpl.deletePost() : cache delete ID >> " + id);
        }
        final Optional<TelcoResourceEntity> telcoResource = telcoResourceRepository.findById(id);
        if (telcoResource.isPresent()) {
            telcoResourceRepository.delete(telcoResource.get());
        } else {
            throw new ResourceNotFoundException();
        }
    }
}
