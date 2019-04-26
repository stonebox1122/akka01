package com.stone.akka.yellowchicken.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.stone.akka.yellowchicken.common.{ClientMessage, ServerMessage}
import com.typesafe.config.ConfigFactory

class YellowChickenServer extends Actor{
  override def receive: Receive = {
    case "start" => println("start 小黄鸡客服开始工作了...")
      // 如果接收到ClientMessage
    case ClientMessage(mes) => {
      // 使用match -- case 匹配
      mes match {
        case "大数据学费" => sender() ! ServerMessage("35000")
        case "学校地址" => sender() ! ServerMessage("北京")
        case "学习什么技术" => sender() ! ServerMessage("大数据，前端，PYTHON")
        case _ => sender() ! ServerMessage("你说的啥子")
      }
    }
  }
}

// 主程序-入口
object YellowChickenServer extends App{
  val host = "127.0.0.1" // 服务端ip地址
  val port = 9999 // 端口
  // 创建 config 对象，指定协议类型、监听的ip和端口
  val config = ConfigFactory.parseString(
    s"""
       |akka.actor.provider="akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname=$host
       |akka.remote.netty.tcp.port=$port
        """.stripMargin)

  // 创建ActorSystem
  val serverActorSystem = ActorSystem("server",config)
  // 创建YellowChickenServer的actor和返回actorRef
  val yellowChickenServerRef: ActorRef = serverActorSystem.actorOf(Props[YellowChickenServer],"YellowChickenServer")

  // 启动
  yellowChickenServerRef ! "start"
}
