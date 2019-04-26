package com.stone.akka.spark.common

// 10.使用样例类来构建通信协议
// Worker 注册信息
case class RegisterWorkerInfo(id: String, cpu: Int, ram: Int)

// 这个是 WorkerInfo，是保存在 Master 的 HashMap 中的，该 HashMap 用于管理 Worker
// 将来这个 WorkerInfo 会扩展，比如 增加 Worker 上一次的心跳时间
class WorkerInfo(val id: String, val cpu: Int, val ram: Int) {
  var lastHeartBeat: Long = System.currentTimeMillis()
}

// 当 Worker 注册成功，服务器返回一个 RegisteredWorkerInfo 对象
case object RegisteredWorkerInfo


// 16.构建心跳协议
// worker每隔一段时间由定时器发给自己的一个消息
case object SendHeartBeat

// worker每隔一段时间由定时器触发，而向master发送的协议消息
case class HeartBeat(id: String)

// 20.构建master检查worker协议
// Master 给自己发送一个触发检查超时 Worker 的信息
case object StartTimeOutWorker

// Master 给自己发消息，检测 Worker，对于心跳超时的
case object RemoveTimeOutWorker

