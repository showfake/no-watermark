package com.lauzzl.nowatermark;

import com.lauzzl.nowatermark.factory.Parser;
import com.lauzzl.nowatermark.factory.ParserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NoWatermarkApplicationTests {

    @Autowired
    private ParserFactory parserFactory;

    @Test
    void contextLoads() {
    }

    @Test
    void test() throws Exception {
        Parser parser = parserFactory.setUrl("https://h5.pipix.com/s/l-l3SMSs8kQ/").build();
        System.out.println(parser.execute());
    }

}
