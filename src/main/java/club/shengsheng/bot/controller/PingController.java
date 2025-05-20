package club.shengsheng.bot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@RestController
public class PingController {


    @GetMapping("/ping")
    public int ping() {
        return 1;
    }
}
