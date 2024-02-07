# MKBi-backend

> 作者：某mikufans

## 项目介绍 
本项目是基于React+Spring Boot+RabbitMQ+AIGC的智能BI数据分析平台。

访问地址：待上线

来源自[编程导航](https://yupi.icu)
> AIGC ：Artificial Intelligence Generation Content(AI 生成内容)

区别于传统的BI，数据分析者只需要导入最原始的数据集，输入想要进行分析的目标，就能利用AI自动生成一个符合要求的图表以及分析结论。此外，还会有图表管理、异步生成、AI对话等功能。只需输入分析目标、原始数据和原始问题，利用AI就能一键生成可视化图表、分析结论和问题解答，大幅降低人工数据分析成本。

## 项目功能 

1. 用户登录、注册
2. 智能分析（同步）。调用AI根据用户上传csv文件生成对应的 JSON 数据，并使用 ECharts图表 将分析结果可视化展示
3. 智能分析（异步）。使用了线程池异步生成图表，最后将线程池改造成使用 RabbitMQ消息队列 保证消息的可靠性，实现消息重试机制
4. 用户限流。本项目使用到令牌桶限流算法，使用Redisson实现简单且高效分布式限流，限制用户每秒只能调用两次数据分析接口，防止用户恶意占用系统资源
5. 调用AI进行数据分析，并控制AI的输出，后端自定义 Prompt 预设模板并封装用户输入的数据和分析诉求，通过对接 AIGC 接口生成可视化图表 JSON 配置和分析结论，返回给前端渲染。
6. 由于AIGC的输入 Token 限制，使用 Easy Excel 解析用户上传的 XLSX 表格数据文件并压缩为CSV，实测提高了20%的单次输入数据量、并节约了成本。

## 项目背景

以ChatGPT、Midjourney为首的AIGC软件出现至今，不过短短半年时间，已经从方方面面浸入到人们的生活里，与此同时，大量专业术语涌入我们视野。
AI、AIGC、AGI、ChatGPT……这些字母缩写到底是什么？有什么区别？
### AI
AI，全称Artificial Intelligence，人工智能。顾名思义，让机器发展出像人一样的智能，可以看到、听到、思考、判断，然后根据经验作出决策。
而AI之所以能够走向现实生活，影响到多个行业领域的生产工作，离不开三个重要技术的支撑：深度学习、神经网络以及生成式对抗网络（GAN）。
### AIGC
AIGC是指由AI自动创作生成的内容（ AI Generated Content），即AI接收到人下达的任务指令，通过处理人的自然语言，自动生成图片、视频、音频等。
AIGC的出现，就像是打开了一个全新的创作世界，为人们提供了无尽的可能性。从用户生成内容（UGC），到专业生成内容（PGC），再到现在的人工智能生成内容（AIGC），我们看到了内容创作方式的巨大变革和进步。
### AIGC和ChatGPT的关系
AIGC是AI大模型，特别是自然语言处理模型的一种重要应用；ChatGPT则是AIGC在聊天对话场景的一个具体应用。
可以把AIGC看作是一个大的范畴，而ChatGPT是其中一个类别的小应用。
### AIGC可以生成的内容
文字：最基本的AIGC内容，可以与人类进行实时对话，生成不同风格的文字，诗歌、故事，甚至计算机代码等。
图像：可以由文字或者图片，直接生成各种类型的图片。可以辅助人类进行绘画设计和发散想象力，大致可以分为图像自主生成工具和图像编辑工具两类。
视频：可以通过文字描述，生成一段情节连贯的视频。比如广告片、电影预告片、教学视频、音乐视频等。也可以当作视频的剪辑工具。
音频：可以生成逼真的音效，包括语音克隆、语音合成、文本生成特定音，音乐生成、声音效果等。
游戏：游戏的剧情设计、角色设计、配音和音乐、美术原画设计、游戏动画、3D模型、地图编辑器等都可以让AIGC帮助完成。
虚拟人：可以生成虚拟明星、虚拟恋人、虚拟助手、虚拟朋友等。指存在于非物理世界(如图片、视频、直播、一体服务机、VR)中，并具有多重人类特征的综合产物。

## 快速启动 
1. 下载/拉取本项目到本地
2. 通过 IDEA 代码编辑器进行打开项目，等待依赖的下载
3. 修改配置文件 `application.yaml` 的信息，比如数据库、Redis、RabbitMQ等
4. 修改信息完成后，通过 `Application` 程序进行运行项目

## 项目架构图
### 基础架构
基础架构：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端利用AI服务处理客户端数据，保持到数据库，并生成图表。处理后的数据由业务后端发送给AI服务，AI服务生成结果并返回给后端，最终将结果返回给客户端展示。
![image](https://github.com/dionico/MKBi-backend/assets/100508941/c557d75a-444a-4a90-aef5-d4464586e2dd)

### 优化项目架构-异步化处理
客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端将请求事件放入消息队列，让要生成图表的客户端去排队
任务处理模块调用AI服务处理客户端数据，AI 服务异步生成结果返回给后端并保存到数据库，当后端的AI工服务生成完毕后，可以通过向前端发送通知的方式，或者通过业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示。在此期间，用户可以去做自己的事情。
![image](https://github.com/dionico/MKBi-backend/assets/100508941/2b0bf369-9729-4fb6-b1f8-a253382f26e6)


## 项目技术栈
1. Spring Boot 2.7.2
2. Spring MVC
3. MyBatis + MyBatis Plus 数据访问
4. Spring Boot 调试工具和项目处理器
5. Spring AOP 切面编程
6. Spring 事务注解
7. Redis：Redisson限流控制
8. MyBatis-Plus 数据库访问结构
9. IDEA插件 MyBatisX ： 根据数据库表自动生成
10. RabbitMQ：消息队列
11. AI SDK：鱼聪明AI接口开发
12. JDK 线程池及异步化
13. Swagger + Knife4j 项目文档
14. Easy Excel：表格数据处理、Hutool工具库 、Apache Common Utils、Gson 解析库、Lombok 注解
