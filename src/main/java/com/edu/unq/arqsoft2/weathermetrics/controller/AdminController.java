package com.edu.unq.arqsoft2.weathermetrics.controller;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherCache;
import com.edu.unq.arqsoft2.weathermetrics.repository.WeatherCacheRepository;
import com.edu.unq.arqsoft2.weathermetrics.service.WeatherCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Endpoints de administración")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final WeatherCacheRepository cacheRepository;

    @Autowired
    public AdminController(WeatherCacheRepository cacheRepository, WeatherCacheService cacheService) {
        this.cacheRepository = cacheRepository;
    }

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
    
    @DeleteMapping("/cache")
    @Operation(summary = "Eliminar toda la caché", 
               description = "Elimina todas las entradas de la caché")
    @ApiResponse(responseCode = "200", description = "Caché eliminada exitosamente")
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = cacheRepository.count();
            cacheRepository.deleteAll();
            
            response.put("status", "success");
            response.put("message", "Toda la caché ha sido eliminada");
            response.put("entriesDeleted", count);
            
            logger.info("Toda la caché ha sido eliminada. Entradas eliminadas: {}", count);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al intentar eliminar la caché", e);
            response.put("status", "error");
            response.put("message", "Error al intentar eliminar la caché: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
