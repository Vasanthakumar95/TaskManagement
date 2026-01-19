package com.learning.taskmanagement.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    void addCorsMappings_configuresApiPath() {
        CorsConfig config = new CorsConfig();
        CorsRegistry registry = mock(CorsRegistry.class);
        when(registry.addMapping("/api/**")).thenReturn(
                new CorsRegistry().addMapping("/dummy") // not used; chain is what matters
        );

        config.addCorsMappings(registry);

        verify(registry).addMapping("/api/**");
    }
}