package io.joliveira.redis.datastructures;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.api.sync.RedisCommands;
import io.netty.util.internal.ThreadLocalRandom;

@Component
public class DataLoader implements  ApplicationListener<ContextRefreshedEvent> {
	@Autowired private RedisCommands<String, String> redisCommandExecutor;
	@Autowired private ObjectMapper mapper;
	
	private Logger logger = LoggerFactory.getLogger(DataLoader.class);
	public static List<String> categories =  Arrays.asList("health_care", "eletronics", "books");
	public static List<String> statuses =  Arrays.asList("not_available", "available", "reserved");
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		redisCommandExecutor.flushdb();
		
		for (int i = 0; i < 1000; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(0, 2);
			Product product = new Product();
			product.setId(UUID.randomUUID().toString());
			product.setName("product_"  + i);
			product.setPrice(new BigDecimal(23));
			product.setStatus(categories.get(randomIndex));
			product.setCategory(statuses.get(0));
		
			try {
				Double productRelevance = new Double(i);
				
				redisCommandExecutor.zadd("products:search:relvanceIndex",  productRelevance, product.getId());
				redisCommandExecutor.zadd("products:search:facets:" + product.getStatus(), productRelevance, product.getId());
				redisCommandExecutor.zadd("products:search:facets:" + product.getCategory(), productRelevance, product.getId());
				redisCommandExecutor.set("products:" + product.getId(), mapper.writeValueAsString(product));

			} catch (Exception e) {
				throw new RuntimeException("Error when inserting data into redis", e);
			}
		}
		
		logger.info("Data loaded into REDIS. Application is ready! ");
	}

}
