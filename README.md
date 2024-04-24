# spring-ai-huawei-ai-pangu-spring-boot-starter

 > 基于 [Huawei 盘古大模型](https://www.huaweicloud.com/product/pangu.html) 和 Spring AI 的 Spring Boot Starter 实现

### Huawei 盘古大模型

盘古大模型：解决行业难题，释放AI生产力

![](https://res-static.hc-cdn.cn/cloudbu-site/china/zh-cn/pangu-new/NEW/1018Pangu/1697768781952456063.png)

- 官网地址：[https://www.huaweicloud.com/product/pangu.html](https://www.huaweicloud.com/product/pangu.html)


#### 支持的功能包括：

- 支持文本生成（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持函数调用（Function Calling）
- 支持 Memory 
- ...

#### 资源

- [应用开发SDK使用指南.docx](应用开发SDK使用指南.docx)

#### 模型

盘古大模型平台提供了包括通 盘古NLP大模型、盘古CV大模型、盘古多模态大模型、盘古预测大模型、盘古科学计算大模型 等多种大模型。

- [盘古NLP大模型](https://www.huaweicloud.com/product/pangu/nlp.html)：最贴合行业落地的NLP大模型
- [盘古CV大模型](https://www.huaweicloud.com/product/pangu/cv.html)：基于海量图像、视频数据和盘古独特技术构筑的视觉基础模型，赋能行业客户利用少量场景数据对模型微调即可实现特定场景任务。
- [盘古多模态大模型](https://www.huaweicloud.com/product/pangu/multimodal.html)：融合语言和视觉跨模态信息，实现图像生成、图像理解、3D生成和视频生成等应用，面向产业智能化转型提供跨模态能力底座。
- [盘古预测大模型](https://www.huaweicloud.com/product/pangu/predict.html)：盘古预测大模型是面向结构化数据，基于神经网络Transformer架构，通过任务理解、模型推荐、模型融合技术，构建通用的预测能力
- [盘古科学计算大模型](https://www.huaweicloud.com/product/pangu/scientific-computing.html)：科学计算大模型是面向气象、医药、水务、机械、航天航空等领域，融合AI数据建模和AI方程求解的方法；从海量的数据中提取出数理规律，使用神经网络编码微分方程；使用AI模型更快更准的解决科学计算问题

| 模型     |  描述 |
|--------| ------------ |
| pangu	 | 盘古大模型 |


### Maven

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>spring-ai-huawei-ai-pangu-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```


### Sample

使用示例请参见 [Spring AI Examples](https://github.com/TeachingAI/spring-ai-examples)

