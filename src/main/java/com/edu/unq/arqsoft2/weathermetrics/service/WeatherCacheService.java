package com.edu.unq.arqsoft2.weathermetrics.service;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherCache;
import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import com.edu.unq.arqsoft2.weathermetrics.repository.WeatherCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class WeatherCacheService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherCacheService.class);
    private static final long DEFAULT_CACHE_TTL_MINUTES = 1;
    
    private final WeatherCacheRepository cacheRepository;

    @Autowired
    public WeatherCacheService(WeatherCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public WeatherData getCachedOrFetch(String cacheKey, Supplier<WeatherData> dataFetcher, Long ttlMinutes) {
        long cacheTtl = ttlMinutes != null ? ttlMinutes : DEFAULT_CACHE_TTL_MINUTES;
        
        // Intentar obtener de caché
        Optional<WeatherCache> cachedData = cacheRepository.findByCacheKey(cacheKey);
        
        if (cachedData.isPresent() && !cachedData.get().isExpired()) {
            logger.info("Cache hit for key: {}", cacheKey);
            //long minutesUntilExpiry = Duration.between(LocalDateTime.now(), cachedData.get().getExpiryTime()).toMinutes();
            //logger.info("Cache hit for key: {}, expires in: {} minutes", cacheKey, minutesUntilExpiry);
            return cachedData.get().getData();
        }
        
        logger.info("Cache miss for key: {}", cacheKey);
        try {
            // Obtener datos frescos
            WeatherData freshData = dataFetcher.get();
            
            // Crear o actualizar la entrada de caché
            WeatherCache cacheEntry = cachedData.orElseGet(() -> new WeatherCache(cacheKey, freshData, cacheTtl));
            
            // Actualizar los datos y tiempos
            cacheEntry.setData(freshData);
            cacheEntry.setLastUpdated(LocalDateTime.now());
            cacheEntry.setExpiryTime(cacheEntry.getLastUpdated().plusMinutes(cacheTtl));
            
            // Guardar la entrada (insertará o actualizará según corresponda)
            cacheRepository.save(cacheEntry);
            
            return freshData;
        } catch (Exception e) {
            logger.error("Error fetching fresh data for key: " + cacheKey, e);
            
            // Si hay un error al obtener datos frescos, devolver datos en caché aunque estén expirados
            if (cachedData.isPresent()) {
                logger.warn("Using expired cache data for key: {}", cacheKey);
                WeatherData dataCache = cachedData.get().getData();
                dataCache.setDescription("Usando datos en caché expirados debido a un error al obtener datos frescos");
                return dataCache;
            }
            
            throw new RuntimeException("No se pudo obtener datos y no hay caché disponible", e);
        }
    }
    
    public void invalidateCache(String cacheKey) {
        cacheRepository.deleteByCacheKey(cacheKey);
        logger.info("Cache invalidated for key: {}", cacheKey);
    }
}
