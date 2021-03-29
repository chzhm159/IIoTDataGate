package org.idw.core.demo;


import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    @RequestMapping("/")
    public String index() {
        logger.info("request /");
        TagsDefineFileProcessor tdfp = new TagsDefineFileProcessor();
        tdfp.load("config/tags.json");
        return "Greetings from Spring Boot  sdfsd!";
    }
}
