package io.joliveira.redis.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
							    @RequestParam(name  = "offset", defaultValue = "10") String offset,
							    @RequestParam(name = "statusFacet", defaultValue = "") String statusFacet, 
							    @RequestParam(name = "categoryFacets", defaultValue = "") String categoryFacets) {
		
		List<String> productKeys = new ArrayList<String>();

		if (statusFacet.isEmpty() && categoryFacets.isEmpty()) {
			productKeys = redisCommandExecutor.zrangebyscore("products:search:relvanceIndex", Range.create(new Double(from), new Double(offset)));
			
		} else {
			String filterKey = "filter_destination:" +UUID.randomUUID().toString();
			redisCommandExecutor.zinterstore(filterKey, buildFacets(statusFacet.toLowerCase(), categoryFacets.toLowerCase()).stream().toArray(String[]::new));
			
			productKeys = redisCommandExecutor.zrangebyscore(filterKey, Range.create(new Double(from), new Double(offset)));
			redisCommandExecutor.del(filterKey);
		}
		
		List<KeyValue<String, String>> result =  redisCommandExecutor.mget(productKeys.stream().map(key -> "products:" +  key).toArray(String[]::new));
		
		return result.stream().map(keyValue -> parse(keyValue.getValue(), Product.class).orElse(new Product())).collect(Collectors.toList());
	}
	
	private List<String> buildFacets(String statusRequestedFacet, String categoryRequestedFacets) {
		List<String> facets = new ArrayList<String>();
		
		if (!categoryRequestedFacets.isEmpty()) {
			List<String> categoryFacets = Arrays.asList(categoryRequestedFacets);
			for (String catetgoryFacet : categoryFacets) {
				if (!DataLoader.categories.contains(catetgoryFacet)) {
					throw new RuntimeException(String.format("Category %s is invalid for filtering", catetgoryFacet));
				}
				
				facets.add("products:search:facets:" + catetgoryFacet.toLowerCase());
			}			
		}
		
		if  (!statusRequestedFacet.isEmpty() && !DataLoader.statuses.contains(statusRequestedFacet))
			throw new RuntimeException(String.format("Status %s is invalid", statusRequestedFacet));
		else 
			facets.add("products:search:facets:"  + statusRequestedFacet.toLowerCase());
		
		return facets;
	}
	
	private <T> Optional<T> parse(String content, Class<T> valueType) {
		try {
			return Optional.of(mapper.readValue(content, valueType));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
}
