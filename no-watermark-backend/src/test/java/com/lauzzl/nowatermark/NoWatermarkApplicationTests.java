package com.lauzzl.nowatermark;

import com.lauzzl.nowatermark.factory.Parser;
import com.lauzzl.nowatermark.factory.ParserFactory;
import com.lauzzl.nowatermark.factory.parser.WeChatMP;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NoWatermarkApplicationTests {

    @Autowired
    private ParserFactory parserFactory;

    @Test
    void contextLoads() {
    }

    @Test
    void test() throws Exception {
        Parser parser = parserFactory.createParser("https://h5.pipix.com/s/l-l3SMSs8kQ/");
        System.out.println(parser.execute("https://h5.pipix.com/s/l-l3SMSs8kQ/"));
    }

}
