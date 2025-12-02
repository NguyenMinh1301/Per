package com.per.auth;

import java.util.TimeZone;

// @SpringBootTest
class AuthApplicationTests {

    // @BeforeAll
    static void setUpTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    // @Test
    void contextLoads() {}
}
