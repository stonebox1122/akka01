package com.stone.akka.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.language.postfixOps

// 1.当继承Actor后，就是一个Actor，核心方法receive方法需要重写
class SayHelloActor extends Actor{
  // 1.receive方法，会被该Actor的MailBox（实现了Runnable接口)调用
  // 2.当该Actor的MailBox接收到消息，就会调用receive
  // 3.type Receive = scala.PartialFunction[scala.Any, scala.Unit]
  override def receive: Receive = {
    case "hello" => println("收到hello，回应hello too")
    case "ok" => println("收到ok，回应ok too")
    case "exit" => {
      println("接收到exit指令，退出系统...")
      context.stop(self) // 停止自己的actorRef
      context.system.shutdown() // 关闭 ActorSystem
    }
    case _ => println("匹配不到")
  }
}

object SayHelloActorDemo {
  // 1.先创建一个ActorSystem，专门用于创建Actor
  private val actorFactory = ActorSystem("actorFactory")
  // 2.创建一个Actor的同时，返回Actor的ActorRef
  // （1）Props[SayHelloActor] 使用反射创建了一个SayHelloActor实例
  // （2）"SayHelloActor"给actor取名
  // （3）sayHelloActorRef: ActorRef就是Props[SayHelloActor]的ActorRef
  // （4）创建SayHelloActor实例被ActorSystem接管
  private val sayHelloActorRef: ActorRef = actorFactory.actorOf(Props[SayHelloActor],"SayHelloActor")

  def main(args: Array[String]): Unit = {
    // 给SayHelloActor发消息（邮箱）
    sayHelloActorRef ! "hello"
    sayHelloActorRef ! "ok"
    sayHelloActorRef ! "ok..."
    // 研究异步如何退出ActorSystem
    sayHelloActorRef ! "exit"
  }
}
