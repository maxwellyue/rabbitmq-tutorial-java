package com.maxwell.rabbitdemo;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    //模拟的耗时任务，即计算斐波那契数
    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) {
        //创建连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            //声明队列
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            //一次只从队列中取出一个消息
            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            //监听消息（即RPC请求）
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();

                    //收到RPC请求后开始处理
                    String response = "";
                    try {
                        String message = new String(body, "UTF-8");
                        int n = Integer.parseInt(message);
                        System.out.println(" [.] fib(" + message + ")");
                        response += fib(n);
                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } finally {
                        //处理完之后，返回响应（即发布消息）
                        System.out.println("[server current time] : " + System.currentTimeMillis());
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));

                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

            //loop to prevent reaching finally block
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException _ignore) {
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {
                }
        }
    }
}