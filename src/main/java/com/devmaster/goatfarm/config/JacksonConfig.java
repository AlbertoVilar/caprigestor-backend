package com.devmaster.goatfarm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuração do Jackson para resolver problemas de serialização
 * especialmente com relações circulares e entidades JPA.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar módulo para suporte a Java 8 Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Desabilitar falha em beans vazios
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Desabilitar escrita de datas como timestamps
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        return mapper;
    }
}