package com.quick.aws.samplespringboot.samplespringboot.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {
    

    private static final Logger logger = LogManager.getLogger(LogController.class);
    private List<Integer> num = Arrays.asList(1, 2, 3, 4, 5);

    /**
     * Create a sample log output.
     * @return
     */
    @GetMapping(value = "/log/create/one")
    public boolean createOneLog()
    {
        logger.trace("Hello from Log4j 2 - num : {}", num);
        logger.debug("Hello from Log4j 2 - num : {}", num);
        logger.info("Hello from Log4j 2 - num : {}", num);
        logger.warn("Hello from Log4j 2 - num : {}", num);
        logger.error("Hello from Log4j 2 - num : {}", num);
        logger.fatal("Hello from Log4j 2 - num : {}", num);

        return true;
    }
}
