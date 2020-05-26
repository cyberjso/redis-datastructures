package io.joliveira.redis.datastructures;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.KeyValue;
import io.lettuce.core.Range;
import io.lettuce.core.api.sync.RedisCommands;

@Controller
public class SearchAPI {
	@Autowired private RedisCommands<String, String> redisCommandExecutor;
	@Autowired private ObjectMapper mapper;
	
	@RequestMapping(value  = "/search", method = RequestMethod.GET)
	@ResponseBody
	public List<Product> search(@RequestParam(name  = "query", defaultValue = "") String query, 
							    @RequestParam(name  = "from", defaultValue = "0") String from, 
							    @RequestParam(name  = "offset", defaultValue = "10") String offset) {
		List<String> productKeys = redisCommandExecutor.zrangebyscore("products:search:relvanceIndex", Range.create(new Double(from), new Double(offset)));
		
		List<KeyValue<String, String>> result =  redisCommandExecutor.mget(productKeys.stream().map(key -> "products:" +  key).toArray(String[]::new));
		
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
