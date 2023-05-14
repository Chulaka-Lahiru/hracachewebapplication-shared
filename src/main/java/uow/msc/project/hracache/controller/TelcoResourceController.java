package uow.msc.project.hracache.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.TelcoResourceService;
import uow.msc.project.hracache.util.ResponseUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Objects;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@RestController
@RequestMapping("/hracachecontroller")
@Slf4j
public class TelcoResourceController {

    @Autowired
    TelcoResourceService telcoResourceService;

    @PostConstruct
    public void cacheRefreshAhead() {
        telcoResourceService.refreshAhead();
    }

    @GetMapping("/telco-resources")
    public ResponseEntity<Page<TelcoResourceEntity>> getAllTelcoResources(
            @RequestParam(value = "page", defaultValue = ResponseUtils.DEFAULT_PAGE_NUM) Integer page,
            @RequestParam(value = "size", defaultValue = ResponseUtils.DEFAULT_PAGE_SIZE) Integer size) {

        try {
            long start = System.currentTimeMillis();
            Page<TelcoResourceEntity> telcoResources = telcoResourceService.getAllTelcoResources(page, size);
            if (telcoResources.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nGET ALL: READ TIME: " + elapsedTime + "ms\n");

            return new ResponseEntity<>(telcoResources, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/read-through-telco-resources/{id}")
    public ResponseEntity<TelcoResourceEntity> getTelcoResourceById(@PathVariable("id") long id) {

        long start = System.currentTimeMillis();
        TelcoResourceEntity telcoResourceEntity = telcoResourceService.findTelcoResourceById(id);
        log.info("TelcoResourceEntity TelcoResourceController {}", telcoResourceEntity);
        if (Objects.nonNull(telcoResourceEntity)) {
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nGET: READ-THROUGH-CACHE TIME: " + elapsedTime + "ms\n");

            return new ResponseEntity<>(telcoResourceEntity, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    @PostMapping("/telco-resources")
    public ResponseEntity<TelcoResourceEntity> createTelcoResource(@RequestBody TelcoResourceEntity telcoResourceEntity) {

        try {
            long start = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResource(telcoResourceEntity);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nPOST: WRITE-TO-DATABASE TIME: " + elapsedTime + "ms\n");

            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/write-through-telco-resources")
    public ResponseEntity<TelcoResourceEntity> createTelcoResourceWithWriteThrough(@RequestBody TelcoResourceEntity telcoResourceEntity) {

        try {
            long start = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResourceToCacheAndDb(telcoResourceEntity);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nPOST: WRITE-THROUGH-CACHE TIME: " + elapsedTime + "ms\n");

            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/write-back-telco-resources")
    public ResponseEntity<TelcoResourceEntity> createTelcoResourceWithWriteBack(
            @RequestParam(value = "buffer-size", defaultValue = ResponseUtils.DEFAULT_WRITE_BACK_BUFFER) String bufferSize,
            @RequestBody TelcoResourceEntity telcoResourceEntity) {

        try {
            long start = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResourceToCacheAndAsyncDb(telcoResourceEntity, bufferSize);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nPOST: WRITE-BACK-CACHE TIME: " + elapsedTime + "ms\n");

            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/telco-resources/{id}")
    public ResponseEntity<TelcoResourceEntity> updateTelcoResource(@PathVariable("id") long id, @RequestBody TelcoResourceEntity telcoResourceEntity) {

        long start = System.currentTimeMillis();
        try {
            TelcoResourceEntity telcoResourceEntityUpdated = telcoResourceService.updateTelcoResource(id, telcoResourceEntity);
//            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
//                    .buildAndExpand(telcoResourceEntityUpdated.getId()).toUri();
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nPUT: DATABASE RECORD UPDATE TIME: " + elapsedTime + "ms\n");

            return ResponseEntity.accepted().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/telco-resources/{id}")
    public ResponseEntity<HttpStatus> deleteTelcoResource(@PathVariable("id") long id) {
        try {
            long start = System.currentTimeMillis();
            telcoResourceService.deleteTelcoResource(id);
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("\nDELETE: DATABASE AND CACHE RECORD DELETE TIME: " + elapsedTime + "ms\n");

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
