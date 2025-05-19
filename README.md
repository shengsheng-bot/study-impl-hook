## 生生机器人

> 是一个GitHub WebHook 负责自动处理GitHub课后题的机器人


## 流程
1. 通过PR触发，通过`shengsheng.yml`文件进行初步校验，如校验失败直接拒绝CI流程  
2. 通过流程1之后，自动添加`ci-approve`标签，触发`ci.yml`流程
3. CI通过，合并之后会自动revert
