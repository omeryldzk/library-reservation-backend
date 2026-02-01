package com.example.libraryweek1.config;

import com.example.libraryweek1.jobs.dto.BreakMonitorInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("server", 6379));
    }


    @Bean
    @Qualifier("breakMonitorRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 1. Create the template
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 2. Set the connection factory (links to your application.properties settings)
        template.setConnectionFactory(connectionFactory);

        // 3. Define Serializers
        // Use String serialization for Keys (makes them readable in Redis CLI)
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serialization for Values (allows storing Objects as JSON)
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(BreakMonitorInfo.class));
        template.setHashValueSerializer(new JacksonJsonRedisSerializer<>(BreakMonitorInfo.class));

        // 4. Initialize and return
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Qualifier("activeUsersRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
