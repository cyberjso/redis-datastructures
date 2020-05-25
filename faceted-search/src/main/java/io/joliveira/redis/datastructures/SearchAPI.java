package io.joliveira.redis.datastructures;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.KeyValue;
import io.lettuce.core.Range;
import io.lettuce.core.api.sync.RedisCommands;

@Controller
public class SearchAPI {
	@Autowired private RedisCommands<String, String> redisCommandExecutor;
	@Autowired private ObjectMapper mapper;
	
	@GetMapping("/search")
	@ResponseBody
	public List<Product> search(String query) {
		List<String> productKeys = redisCommandExecutor.zrangebyscore("products:search:relvanceIndex", Range.create(0D, 10D));
		
		List<KeyValue<String, String>> result =  redisCommandExecutor.mget(productKeys.stream().toArray(String[]::new));
		
		return result.stream().map(keyValue -> parse(keyValue.getValue(), Product.class).orElse(new Product())).collect(Collectors.toList());
	}
	
	private <T> Optional<T> parse(String content, Class<T> valueType) {
		try {
			return Optional.of(mapper.readValue(content, valueType));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	
	
}
