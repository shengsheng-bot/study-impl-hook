package club.shengsheng.bot.controller;

import club.shengsheng.bot.handler.GitHubEventHandler;
import club.shengsheng.bot.handler.Github;
import club.shengsheng.bot.handler.UnknownHandler;
import org.kohsuke.github.GHEvent;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@RestController
@RequestMapping("/hook")
public class HookController {


    private Map<GHEvent, GitHubEventHandler> handlerMap;

    private final UnknownHandler unknownHandler;

    public HookController(UnknownHandler unknownHandler) {
        this.unknownHandler = unknownHandler;
    }


    @PostMapping
    public String hook(@RequestHeader("X-GitHub-Event") String event,
                       @RequestBody String payload) throws Exception {
        GHEvent ghEvent = GHEvent.valueOf(event.toUpperCase(Locale.ROOT));
        System.out.println(ghEvent);
        System.out.println(payload);
        GitHubEventHandler handler = handlerMap.getOrDefault(ghEvent, unknownHandler);
        handler.handle(ghEvent, payload.substring("payload=".length()));
        return "ok";
    }


    @Autowired
    public void setEventHandlerMap(List<GitHubEventHandler> eventHandler) {
        handlerMap = new EnumMap<>(GHEvent.class);
        for (GitHubEventHandler handler : eventHandler) {
            Class<?> handlerClass = handler.getClass();
            if (AopUtils.isAopProxy(handler)) {
                handlerClass = AopUtils.getTargetClass(handler);
            }
            Github annotation = handlerClass.getAnnotation(Github.class);
            if (annotation != null) {
                handlerMap.put(annotation.value(), handler);
            }
        }
    }


}
