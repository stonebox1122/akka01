package com.stone.akka.spark.master

import akka.actor.{Actor, ActorSystem, Props}
import com.stone.akka.spark.common._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import scala.collection.mutable

/**
  * 1-15完成注册功能
  * 16-19完成心跳功能
  * 20-23完成检测功能
  * 24-25完成参数配置
  */
class SparkMaster extends Actor {

  // 12.定义HashMap，管理workers
  val workers = mutable.Map[String, WorkerInfo]()

  override def receive: Receive = {
    // 4.获取启动信息，确认启动
    case "start" => {
      println("master服务器启动了...")

      // 21.给自己发送开始检查worker心跳的消息
      self ! StartTimeOutWorker
    }
    // 13.接收客户端的注册信息
    case RegisterWorkerInfo(id, cpu, ram) => {
      if (!workers.contains(id)) {
        val workerInfo = new WorkerInfo(id, cpu, ram)
        // 加入到workers
        workers += ((id, workerInfo))
        println("服务器的workers=" + workers)

        // 14.回复客户端消息
        sender() ! RegisteredWorkerInfo
      }
    }

    // 19.服务器处理接收的心跳
    case HeartBeat(id) => {
      // 更新对应的worker的心跳时间
      // 先从workers取出workerInfo
      val workerInfo = workers(id)
      workerInfo.lastHeartBeat = System.currentTimeMillis()
      println("master更新了" + id + "心跳时间...")
    }

    // 22.收到开始检查worker心跳的消息后，每隔9秒检查一次
    case StartTimeOutWorker => {
      println("开始了定时检测worker心跳的任务")
      import context.dispatcher
      // 0 millis：不延时，立即执行定时器
      // 3000 millis：每隔3秒执行一次
      // self：表示发给自己
      // RemoveTimeOutWorker：发送的内容
      context.system.scheduler.schedule(0 millis, 9000 millis, self, RemoveTimeOutWorker)
    }

    // 23.对RemoveTimeOutWorker消息处理
    // 需要检查哪些worker心跳超时(now - lastHeartBeat > 6000)，并从map中删除
    case RemoveTimeOutWorker => {
      // 首先将所有的workers的所有workerInfo取出来
      val workerInfos = workers.values
      val nowTime = System.currentTimeMillis()
      // 再把超时的所有workerInfo删除即可
      workerInfos.filter(workerInfo => (nowTime - workerInfo.lastHeartBeat) > 6000)
        .foreach(workerInfo => workers.remove(workerInfo.id))
      println("当前有" + workers.size + "个worker正常")
    }
  }
}

object SparkMaster {
  def main(args: Array[String]): Unit = {
    // 24.服务器端参数可配置化，3个参数：host，port，sparkMasterActor
    if (args.length != 3){
      println("请输入参数：host port sparkMasterActor")
      sys.exit()
    }
    val host = args(0)
    val port = args(1)
    val name = args(2)

    // 1.创建服务器端ActorSystem
    //val host = "127.0.0.1" // 服务端ip地址
    //val port = 10005 // 端口
    // 创建 config 对象，指定协议类型、监听的ip和端口
    val config = ConfigFactory.parseString(
      s"""
         |akka.actor.provider="akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname=$host
         |akka.remote.netty.tcp.port=$port
        """.stripMargin)
    val sparkMasterSystem = ActorSystem("SparkMaster", config)

    // 2.创建SparkMaster - actor
    val sparkMasterRef = sparkMasterSystem.actorOf(Props[SparkMaster], s"${name}")

    // 3.启动SparkMaster
    sparkMasterRef ! "start"
  }
}
