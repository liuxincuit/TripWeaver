package com.tripweaver.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class ToolConfig {

    @Bean
    public List<Object> travelTools(WeatherTool weatherTool) {
        return List.of(weatherTool);
    }
}
