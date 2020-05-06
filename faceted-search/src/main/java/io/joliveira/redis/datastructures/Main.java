package io.joliveira.redis.datastructures;

import java.util.List;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

public class Main {
	
	public static void main(String[] args) {
		RedisClient client = RedisClient.create("redis://localhost:6379");
		RedisCommands<String, String> commandExecutor  =  client.connect().sync();
		
		List<String> keeys  =  commandExecutor.keys("*");
		System.out.println(keeys);
	}
	
}
