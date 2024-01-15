package zk.aws.springboot.jedis.zkawsspringbootjedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/jedis")
    public String index(@RequestParam("key") String key, @RequestParam("value") String value){

        if(key == null || key.isEmpty() || value == null || value.isEmpty())
        {
            return "key and value param required.";
        }

        if(redisTemplate.hasKey(key)){
            return "already exists";
        }
        redisTemplate.opsForValue().set(key, value, 1);
        return redisTemplate.hasKey(key).toString();
    }

    // refer to : https://ost.51cto.com/posts/2333

//    cn-northwest-1.console.amazonaws
}