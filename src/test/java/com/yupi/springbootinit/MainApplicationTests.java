package com.yupi.springbootinit;

import javax.annotation.Resource;

import com.yupi.springbootinit.config.CosClientConfig;
import com.yupi.springbootinit.manager.AiManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private CosClientConfig cosClientConfig;

    @Test
    void contextLoads() {
        System.out.println(cosClientConfig);
    }

}
