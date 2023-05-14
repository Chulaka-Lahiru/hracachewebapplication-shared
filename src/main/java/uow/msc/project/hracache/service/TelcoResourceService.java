package uow.msc.project.hracache.service;

import org.springframework.data.domain.Page;
import uow.msc.project.hracache.model.TelcoResourceEntity;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
public interface TelcoResourceService {

    void refreshAhead();

    TelcoResourceEntity findTelcoResourceById(Long id);

    Page<TelcoResourceEntity> getAllTelcoResources(Integer page, Integer size);

    TelcoResourceEntity saveTelcoResource(TelcoResourceEntity telcoResourceEntity);

    TelcoResourceEntity saveTelcoResourceToCacheAndDb(TelcoResourceEntity telcoResourceEntity);

    TelcoResourceEntity saveTelcoResourceToCacheAndAsyncDb(TelcoResourceEntity telcoResourceEntity, String bufferSize);

    TelcoResourceEntity updateTelcoResource(Long id, TelcoResourceEntity telcoResourceEntity);

    void deleteTelcoResource(Long id);

}
