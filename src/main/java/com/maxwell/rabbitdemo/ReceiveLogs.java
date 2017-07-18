package com.maxwell.rabbitdemo;

import com.rabbitmq.client.*;

import java.io.IOException;

public class ReceiveLogs {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //声明路由器及类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //声明一个随机名字的队列
        String queueName = channel.queueDeclare().getQueue();
        //绑定队列到路由器上
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //开始监听消息
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}

