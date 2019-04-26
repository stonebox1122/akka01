package com.stone.akka.yellowchicken.client

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import akka.remote.transport.ThrottlerTransportAdapter.Direction.Receive
import com.stone.akka.yellowchicken.common.{ClientMessage, ServerMessage}
import com.typesafe.config.ConfigFactory

class CustomerActor(serverHost: String, serverPort: Int) extends Actor {
  // 定义一个YellowChickenServerRef
  var serverActorRef: ActorSelection = _

  // 在Actor中有一个PreStart方法，会在actor运行前执行，通常将初始化的工作方针PreStart方法
  override def preStart(): Unit = {
    println("preStart() 执行")
    serverActorRef = context.actorSelection(s"akka.tcp://server@${serverHost}:${serverPort}/user/YellowChickenServer")
    println(serverActorRef)
  }

  override def receive: Receive = {
    case "start" => println("start 客户端运行，可以咨询问题...")
    case mes: String => {
      // 转发给服务器
      serverActorRef ! ClientMessage(mes)  // 使用ClientMessage 样例类的apply方法
    }
      // 如果接收到服务器的回复
    case ServerMessage(mes) => {
      println("收到小黄鸡客服（server）：" + mes)
    }
  }
}

object CustomerActor extends App {
  val (clientHost, clientPort, serverHost, serverPort) = ("127.0.0.1", 9990, "127.0.0.1", 9999)
  val config = ConfigFactory.parseString(
    s"""
       |akka.actor.provider="akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname=$clientHost
       |akka.remote.netty.tcp.port=$clientPort
        """.stripMargin)

  // 创建ActorSystem
  val clientActorSystem = ActorSystem("client", config)
  // 创建CustomerActor的实例和引用
  val customerActorRef: ActorRef = clientActorSystem.actorOf(Props(new CustomerActor(serverHost, serverPort)), "customerActor")
  // 启动customerActorRef
  customerActorRef ! "start"

  // 客户端可以发送消息给服务器
  while (true) {
    val mes = Console.readLine()
    customerActorRef ! mes
  }
}
