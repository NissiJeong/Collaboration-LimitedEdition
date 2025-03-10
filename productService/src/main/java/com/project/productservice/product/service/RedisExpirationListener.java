package com.project.productservice.product.service;

import com.project.common.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class RedisExpirationListener extends KeyExpirationEventMessageListener {

    private final ProductService productService;
    private final RedisRepository redisRepository;

    public RedisExpirationListener(RedisMessageListenerContainer listenerContainer, ProductService productService, RedisRepository redisRepository) {
        super(listenerContainer);
        this.productService = productService;
        this.redisRepository = redisRepository;
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        log.info("redis key expiration: {}",expiredKey);
        // 예약이 만료되면 Redis 에서 복구
        if(expiredKey.startsWith("reservation:order:")) {
            List<String> productList = redisRepository.getEntireList("backup:"+expiredKey);

            if(!productList.isEmpty())
                productService.restockProductStock(productList);

            redisRepository.deleteData("backup:"+expiredKey);
        }
    }
}
