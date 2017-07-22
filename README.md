
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

----

---
### RabbitMQ 一般工作流程
生产者和RabbitMQ服务器建立连接和通道，声明路由器，同时为消息设置路由键，这样，所有的消息就会以特定的路由键发给路由器，具体路由器会发送到哪个或哪几个队列，生产者在大部分场景中都不知道。（1个路由器，但不同的消息可以有不同的路由键）。
消费者和RabbitMQ服务器建立连接和通道，然后声明队列，声明路由器，然后通过设置绑定键（或叫路由键）为队列和路由器指定绑定关系，这样，消费者就可以根据绑定键的设置来接收消息。（1个路由器，1个队列，但不同的消费者可以设置不同的绑定关系）。

----
### 主要方法
* 声明队列（创建队列）：可以生产者和消费者都声明，也可以消费者声明生产者不声明，也可以生产者声明而消费者不声明。最好是都声明。（生产者未声明，消费者声明这种情况如果生产者先启动，会出现消息丢失的情况，因为队列未创建）
```
channel.queueDeclare(String queue, //队列的名字
                       boolean durable, //该队列是否持久化（即是否保存到磁盘中）
                       boolean exclusive,//该队列是否为该通道独占的，即其他通道是否可以消费该队列
                       boolean autoDelete,//该队列不再使用的时候，是否让RabbitMQ服务器自动删除掉
                       Map<String, Object> arguments)//其他参数
```
* 声明路由器（创建路由器）：生产者、消费者都要声明路由器---如果声明了队列，可以不声明路由器。
```
channel.exchangeDeclare(String exchange,//路由器的名字
                          String type,//路由器的类型：topic、direct、fanout、header
                          boolean durable,//是否持久化该路由器
                          boolean autoDelete,//是否自动删除该路由器
                          boolean internal,//是否是内部使用的，true的话客户端不能使用该路由器
                          Map<String, Object> arguments) //其他参数
```
* 绑定队列和路由器：只用在消费者
```
channel.queueBind(String queue, //队列
                    String exchange, //路由器
                    String routingKey, //路由键，即绑定键
                    Map<String, Object> arguments) //其他绑定参数
```

* 发布消息：只用在生产者
```
channel.basicPublish(String exchange, //路由器的名字，即将消息发到哪个路由器
                       String routingKey, //路由键，即发布消息时，该消息的路由键是什么
                       BasicProperties props, //指定消息的基本属性
                       byte[] body)//消息体，也就是消息的内容，是字节数组
```
 * `BasicProperties props`：指定消息的基本属性，如`deliveryMode`为2时表示消息持久，2以外的值表示不持久化消息
```
//BasicProperties介绍
String corrId = "";
String replyQueueName = "";
Integer deliveryMode = 2;
String contentType = "application/json";
AMQP.BasicProperties props = new AMQP.BasicProperties
            .Builder()
            .correlationId(corrId)
            .replyTo(replyQueueName)
            .deliveryMode(deliveryMode)
            .contentType(contentType)
            .build();
```
* 接收消息：只用在消费者
```
channel.basicConsume(String queue, //队列名字，即要从哪个队列中接收消息
                      boolean autoAck, //是否自动确认，默认true
                      Consumer callback)//消费者，即谁接收消息
```
 * 消费者中一般会有回调方法来消费消息
```
Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, //该消费者的标签
                                       Envelope envelope,//字面意思为信封：packaging data for the message
                                       AMQP.BasicProperties properties, //message content header data 
                                       byte[] body) //message body
                                       throws IOException {
                    //获取消息示例
                    String message = new String(body, "UTF-8");
                    //接下来就可以根据消息处理一些事情
            }
        };
```

---
### 路由器类型
* fanout：会忽视绑定键，每个消费者都可以接受到所有的消息（前提是每个消费者都要有各自单独的队列，而不是共有同一队列）。
* direct：只有绑定键和路由键完全匹配时，才可以接受到消息。
* topic：可以设置多个关键词作为路由键，在绑定键中可以使用`*`和`#`来匹配
* headers：（可以忽视它的存在）
---
### [教程一 HelloWorld](http://www.jianshu.com/p/36a328ff8a6b)
看主要代码
```
//生产者
channel.queueDeclare(QUEUE_NAME, false, false, false, null); ----①
String message = "Hello World!";
channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
//消费者
channel.queueDeclare(QUEUE_NAME, false, false, false, null);
channel.basicConsume(QUEUE_NAME, true, consumer);
```
这里，生产者和消费者都没有声明路由器，而是声明了同名的队列。生产者发布消息时，使用了默认的无名路由器（`""`），并以队列的名字作为了路由键。消费者在消费时，由于没有声明路由器，这并不表示没有路由器的存在，消费者此时使用的是默认的路由器，即Default exchange，该路由器和所有的队列都进行绑定，并且使用队列的名字作为了路由键进行绑定。所以，生产者使用默认路由器以队列的名字作为了绑定键进行了消息发布，而消费者也使用了默认的路由器，并以队列的名字作为绑定键进行了绑定。而默认路由器是direct类型，路由键和绑定键完全匹配时，消费者才能接受到消息，所以教程1中的消费者可以接收到消息。（为了认证这一点，可以将代码①去掉，然后先运行消费者，让它等待监听，然后启动生产者，发送消息，消费者同样会收到消息。这里的生产者声明队列，只是让RabbitMQ服务器先创建这个队列，以免发送的消息因为找不到队列而丢失。）

---
### [教程二 Work Queues](http://www.jianshu.com/p/37c23ed0a5f1)
看主要代码
```
//生产者
channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
String message = "1.";
channel.basicPublish("", TASK_QUEUE_NAME,
        MessageProperties.PERSISTENT_TEXT_PLAIN,
        message.getBytes("UTF-8"));
//消费者
channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
channel.basicQos(1);---①
        ...channel.basicAck(envelope.getDeliveryTag(), false);...---③
boolean autoAck = false;---②
channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
```
这里也使用了默认的direct路由器。假如启动多个工作者（消费者），按道理这些工作者应该可以接收到所有的消息啊，但是不要忘了这几个工作者都是从同一个队列中取消息，消息取出一个，队列中就少一个，所以每个工作者都只是收到的消息的一部分。既然这几个工作者都从同一个队列中取消息，那每个工作者应该怎么取呢？

如果没有代码①，并且②设置为true，即自动确认收到消息，RabbitMQ只要发出消息就认为消费者收到了，此时RabbitMQ采取的是循环分发的策略，在这几个工作者中循环轮流分发消息。每个工作者接受到的消息数量都是相同的。
如果有代码①，并且②设置为false，则RabbitMQ会采取公平分发策略，即将消息发给空闲的工作者（空闲，工作者将消息处理完毕，执行了代码③；不空闲，即工作者还在处理消息，还没有给RabbitMQ发回确认信息，即还没有执行代码③）。
代码①中的参数1：`(prefetchCount)maximum number of messages that the server will deliver`。

为了防止队列丢失，在声明队列的时候指定了`durable`为`true`。为了防止消息丢失，设置了消息属性`BasicProperties`为`MessageProperties.PERSISTENT_TEXT_PLAIN`，让我们看看值是什么：
![MessageProperties.PERSISTENT_TEXT_PLAIN](http://upload-images.jianshu.io/upload_images/1932449-eb9b4ddf0aa91e01.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

可以看出里面包含了`deliveryMode=2`。从这张图也可以看到`BasicProperties`属性的全貌。

如果想让多个消费者共同消费某些消息，只要让他们共用同一队列即可（当然前提是你得保证消息可以都进到这个队列中来，如本例中使用`direct`路由器，消息的路由键和队列的绑定键设为一致，当然也可以使用`fanout`路由器，路由键和绑定键随意设置，不一致也能收到，因为`fanout`路由器会忽略路由键的设置）。

---
### [教程三 Publish/Subscribe](http://www.jianshu.com/p/9a5bde4798dd)
看主要代码
```
 //生产者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
 //消费者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
String queueName = channel.queueDeclare().getQueue();---①
channel.queueBind(queueName, EXCHANGE_NAME, "");
channel.basicConsume(queueName, true, consumer);
```
教程三才引出路由器的概念。生产者和消费者声明了同样的路由，并指明路由类型为`fanout`，该路由器会忽视路由键，将消息发布到所有绑定的队列中（仍需要绑定，只是绑定时绑定键任意就行了）。
假如启动多个消费者，因为代码①中调用无参的声明去恶劣方法`channel.queueDeclare()`，就会创建了一个非持久、独特的、自动删除的队列，并返回一个自动生成的名字。所以多个消费者取消息时使用的是各自的队列，不会存在多个消费者从同一个队列取消息的情况。
这样多个消费者就可以接收到同一消息。

如果想实现多个消费者都可以接收到所有的消息，只要让他们各自使用单独的队列即可（当然前提是保证路由键和绑定键的设置可以让消息都进入到队列，如本例中使用`fanout`路由器，无需考虑绑定键和路由键）。

---
### [教程4 Routing](http://www.jianshu.com/p/c6b922b7c8a5)
看主要代码：
```
//生产者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"));
//消费者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
String queueName = channel.queueDeclare().getQueue();
String[] severities = {"info", "warning", "error"};
for (String severity : severities) {
            channel.queueBind(queueName, EXCHANGE_NAME, severity);
} 
channel.basicConsume(queueName, true, consumer);
```
可以看出，教程3使用了`direct`路由器，该路由器的特点是可以设定路由键和绑定键，消费者只能从队列中取出两者匹配的消息。
在生产者发消息时，为消息设置不同的路由键（如例子中`severity`可以设为`info`、`warn`、`error`）。
消费者在通过为队列设置多个绑定关系，来选择想要接收的消息。
这里有一个概念叫做多重绑定，即多个队列以相同的绑定键binding key绑定到同一个路由器上，此时`direct`路由器就会像fanout路由器一样，将消息广播给所有符合路由规则的队列。

---
### [教程5 Topics](http://www.jianshu.com/p/603b3e8542c6)
看主要代码：
```
//生产者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
String routingKey = "";
String message = "msg...";
channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
//消费者
channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
String queueName = channel.queueDeclare().getQueue();
String bingingKeys[] = {""};
  for (String bindingKey : bingingKeys) {
        channel.queueBind(queueName, EXCHANGE_NAME,   bindingKey);
  }
channel.basicConsume(queueName, true, consumer);
```
这里使用了`topic`路由器，它与`direct`路由器类似，不同在于，`topic`路由器可以为路由键设置多重标准。一个消息有一个路由键，`direct`路由器只能为路由键指定一个关键字，但是`topic`路由器可以在路由键中通过点号分割多个单词来组成路由键，消费者在绑定的时候，可以设置多重标准来选择接受。
举个例子：假如日志根据严重级别`info`、`warn`、`error`，也可以根据来源分为`cron`、`kern`、`auth`。某个日志消息设置路由键为`kern.info`，表示来自`kern`的`info`级别的日志。想要选择接收消息的时候，`direct`路由器就办不到，它要么可以根据严重级别来筛选，要么根据来源来筛选，而`topic`路由器则可以轻松应对，只要将绑定键设置为`kern.info`就可以精准获取该类型的日志。

---
### [教程6 Remote Procedure Call](http://www.jianshu.com/p/a2f085c669ec)
教程6属于RabbitMQ在RPC领域的应用，与上面几个教程的内容没有多大衔接，可以直接阅读原文。
