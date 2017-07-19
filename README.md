
### 说明

[RabbitMQ Tutorials Java版](http://www.rabbitmq.com/tutorials/tutorial-one-java.html)的解读。

---
[【译】RabbitMQ教程一](http://www.jianshu.com/p/36a328ff8a6b)
* 主要通过Hello Word对RabbitMQ有初步认识



[【译】RabbitMQ教程二](http://www.jianshu.com/p/36a328ff8a6b)
* 工作队列，即一个生产者对多个消费者
* 循环分发、消息确认、消息持久、公平分发


[【译】RabbitMQ教程三](http://www.jianshu.com/p/36a328ff8a6b)
* 如何同一个消息同时发给多个消费者
* 开始引入RabbitMQ消息模型中的重要概念路由器Exchange以及绑定等
* 使用了fanout类型的路由器

[【译】RabbitMQ教程四](http://www.jianshu.com/p/36a328ff8a6b)
* 如何选择性地接收消息
* 使用了direct路由器


[【译】RabbitMQ教程五](http://www.jianshu.com/p/36a328ff8a6b)
* 如何通过多重标准接收消息
* 使用了topic路由器，可通过灵活的路由键和绑定键的设置，
进一步增强消息选择的灵活性


[【译】RabbitMQ教程六](http://www.jianshu.com/p/36a328ff8a6b)
* 如何使用RabbitMQ实现一个简单的RPC系统
* 回调队列`callback queue`和关联标识`correlation id`