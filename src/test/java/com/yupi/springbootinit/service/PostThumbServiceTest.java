package com.yupi.springbootinit.service;

import javax.annotation.Resource;

import com.yupi.springbootinit.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子点赞服务测试
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@SpringBootTest
class PostThumbServiceTest {

    @Resource
    private PostThumbService chartThumbService;

    private static final User loginUser = new User();

    @BeforeAll
    static void setUp() {
        loginUser.setId(1L);
    }

    @Test
    void doPostThumb() {
        int i = chartThumbService.doPostThumb(1L, loginUser);
        Assertions.assertTrue(i >= 0);
    }
}
