package com.edu.unq.arqsoft2.weathermetrics.controller;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherCache;
import com.edu.unq.arqsoft2.weathermetrics.repository.WeatherCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private WeatherCacheRepository cacheRepository;

    @GetMapping("/clean-duplicates")
    public ResponseEntity<Map<String, Object>> cleanDuplicates() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Obtener todos los cachés
            List<WeatherCache> allCaches = cacheRepository.findAll();
            
            // Agrupar por cacheKey y mantener solo el más reciente
            Map<String, Optional<WeatherCache>> latestByKey = allCaches.stream()
                .collect(Collectors.groupingBy(
                    WeatherCache::getCacheKey,
                    Collectors.maxBy(Comparator.comparing(WeatherCache::getExpiryTime))
                ));
            
            // Obtener los IDs a mantener
            Set<String> idsToKeep = latestByKey.values().stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getId())
                .collect(Collectors.toSet());
            
            // Contar duplicados
            long totalEntries = allCaches.size();
            long duplicatesRemoved = totalEntries - idsToKeep.size();
            
            // Eliminar duplicados
            if (duplicatesRemoved > 0) {
                List<WeatherCache> toDelete = allCaches.stream()
                    .filter(cache -> !idsToKeep.contains(cache.getId()))
                    .collect(Collectors.toList());
                
                cacheRepository.deleteAll(toDelete);
                
                // Crear índice único para prevenir futuros duplicados
                // Esto se manejará automáticamente por la anotación @Indexed
            }
            
            response.put("status", "success");
            response.put("totalEntriesBefore", totalEntries);
            response.put("duplicatesRemoved", duplicatesRemoved);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Cleaned {} duplicate cache entries", duplicatesRemoved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cleaning duplicates", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<WeatherCache> allCaches = cacheRepository.findAll();
            
            // Agrupar por cacheKey para encontrar duplicados
            Map<String, List<WeatherCache>> cachesByKey = allCaches.stream()
                .collect(Collectors.groupingBy(WeatherCache::getCacheKey));
            
            // Contar duplicados
            long duplicateKeys = cachesByKey.values().stream()
                .filter(list -> list.size() > 1)
                .count();
            
            response.put("totalEntries", allCaches.size());
            response.put("uniqueKeys", cachesByKey.size());
            response.put("duplicateKeys", duplicateKeys);
            response.put("keysWithDuplicates", cachesByKey.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting cache stats", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
