package io.joliveira.redis.datastructures;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

@Configuration
public class Config {

	@Bean
	public RedisCommands<String, String> redisClient() {
		RedisClient client = RedisClient.create("redis://localhost:6379");
		RedisCommands<String, String> commandExecutor  =  client.connect().sync();
		
		return commandExecutor;

	}
	
	@Bean 
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
