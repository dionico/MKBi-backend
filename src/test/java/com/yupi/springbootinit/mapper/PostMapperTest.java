package com.yupi.springbootinit.mapper;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子数据库操作测试
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@SpringBootTest
class PostMapperTest {

    @Resource
    private PostMapper postMapper;

    @Test
    void listPostWithDelete() {
        List<Post> postList = postMapper.listPostWithDelete(new Date());
        Assertions.assertNotNull(postList);
    }
}