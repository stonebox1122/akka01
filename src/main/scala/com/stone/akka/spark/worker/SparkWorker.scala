package com.stone.akka.spark.worker

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.stone.akka.spark.common.{HeartBeat, RegisterWorkerInfo, RegisteredWorkerInfo, SendHeartBeat}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

class SparkWorker(masterHost: String, masterPort: Int, sparkMasterActor: String) extends Actor {

  val id = UUID.randomUUID().toString

  // 6.获取masterProxy，masterProxy是master的代理/引用
  var masterProxy: ActorSelection = _

  override def preStart(): Unit = {
    masterProxy = context.actorSelection(s"akka.tcp://SparkMaster@${masterHost}:${masterPort}/user/${sparkMasterActor}")
  }

  override def receive: Receive = {
    // 9.获取启动信息，确认启动
    case "start" => {
      println("worker启动了")
      // 11.发出注册消息到服务器
      masterProxy ! RegisterWorkerInfo(id, 16, 16 * 1024)
    }

    // 15.收到服务器回送消息
    case RegisteredWorkerInfo => {
      println("workerid=" + id + "注册成功...")

      // 17.当注册成功后，就定义一个定时器，每隔一段时间发送SendHeartBeat给自己
      import context.dispatcher
      // 0 millis：不延时，立即执行定时器
      // 3000 millis：每隔3秒执行一次
      // self：表示发给自己
      // SendHeartBeat：发送的内容
      context.system.scheduler.schedule(0 millis, 3000 millis, self, SendHeartBeat)
    }

    // 18.向服务器发送心跳
    case SendHeartBeat => {
      println("worker=" + id + "给master发送心跳")
      masterProxy ! HeartBeat(id)
    }
  }
}

object SparkWorker {
  def main(args: Array[String]): Unit = {
    // 25.客户器端参数可配置化，6个参数：workerHost，workerPort，sparkWorkerActor，masterHost，masterPort，sparkMasterActor
    if (args.length != 6) {
      println("请输入参数：workerHost workerPort sparkWorkerActor masterHost masterPort sparkMasterActor")
    }
    val (workerHost, workerPort, sparkWorkerActor, masterHost, masterPort, sparkMasterActor) = (args(0), args(1), args(2), args(3), args(4), args(5))

    // 5.创建客户端ActorSystem
    //val (workerHost, workerPort, masterHost, masterPort) = ("127.0.0.1", 10001, "127.0.0.1", 10005)
    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$workerHost
         |akka.remote.netty.tcp.port=$workerPort
        """.stripMargin)
    val sparkWorkerSystem = ActorSystem("SparkWorker", config)

    // 7.创建SparkWorker - actor引用
    val sparkWorkActorRef = sparkWorkerSystem.actorOf(Props(new SparkWorker(masterHost, masterPort.toInt, sparkMasterActor)), s"${sparkWorkerActor}")

    // 8.启动SparkWork
    sparkWorkActorRef ! "start"
  }
}
