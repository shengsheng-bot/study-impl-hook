package club.shengsheng.bot;

import club.shengsheng.bot.github.PrEvent;
import club.shengsheng.bot.github.WorkflowJobEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SerializeTests {

    @Test
    void prEvent() throws IOException {
        JSONObject jsonObject = JSON.parseObject(this.getClass().getClassLoader().getResourceAsStream("pr.json"));
        Assertions.assertNotNull(jsonObject);
        PrEvent event = jsonObject.toJavaObject(PrEvent.class);
        System.out.println(event);
    }

    @Test
    void actionEvent() throws Exception {
        JSONObject jsonObject = JSON.parseObject(this.getClass().getClassLoader().getResourceAsStream("action.json"));
        Assertions.assertNotNull(jsonObject);
        WorkflowJobEvent event = jsonObject.toJavaObject(WorkflowJobEvent.class);
        System.out.println(event);
    }

}
