package com.scboanda.demo.service.impl;


import com.scboanda.demo.util.Timer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Chut
 * @since 2022/2/11 16:37
 */
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {



    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("启动成功");
//        Timer timer = new Timer();
//        timer.wgsj();
    }
}
