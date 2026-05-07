package com.tripweaver.tools;

import org.springframework.stereotype.Component;
import java.util.function.Function;

@Component
public class WeatherTool implements Function<WeatherTool.Request, WeatherTool.Response> {

    public record Request(String city) {}

    public record Response(String city, String weather, String temperature, String suggestion) {}

    @Override
    public Response apply(Request request) {
        // TODO: 接入真实天气 API
        return new Response(
            request.city(),
            "晴天",
            "25°C",
            "天气晴朗，适合户外活动，建议携带防晒用品。"
        );
    }
}
