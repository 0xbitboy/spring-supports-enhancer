# spring-event-enhancer
&emsp;&emsp; 本项目是对spring中的事件驱动编程进行的拓展，从ApplicationMultiCaster下手，将ApplicationEvent与RabbitMQ结合，实现可靠的事件传递与延时事件的目的。
当然也可以使用其他消息队列去承载。

## 应用场景

1. 你的项目中大量使用的ApplicationEvent ，但是在做服务拆分的时候想做最小的改动。
2. 想引入异步延时消息，但是又不想在业务代码中直接使用@RabbitListener等（要配置各种Key，exchange，queue等）。
3. 主要还是第一点，如果一开始就有考虑到分布式之类的可以直接使用[JMS(Java Message Service)](https://zh.wikipedia.org/wiki/Java%E6%B6%88%E6%81%AF%E6%9C%8D%E5%8A%A1)