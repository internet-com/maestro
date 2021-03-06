package com.adendamedia.metrics

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import com.adendamedia.{EventBus, MemoryScale}

object MemorySampler {
  def props(eventBus: ActorRef, memoryScale: MemoryScale): Props =
    Props(new MemorySampler(eventBus: ActorRef, memoryScale: MemoryScale))

  final case class Result(sample: Int)

  case object SampleMemory
  case object ResetMemorySampler
}

class MemorySampler(eventBus: ActorRef, memoryScale: MemoryScale) extends Actor with ActorLogging {
  import MemorySampler._
  import RedisSample._
  import EventBus._

  import context.dispatcher

  private val redisConfig = ConfigFactory.load().getConfig("redis")
  private val maxMemory = redisConfig.getInt("sampler.max.memory")
  private val minMemory = redisConfig.getInt("sampler.min.memory")

  private val logger = LoggerFactory.getLogger(this.getClass)

  implicit val timeout = Timeout(20 seconds)

  def receive = {
    case SampleMemory => sampleMemory
    case ResetMemorySampler =>
      log.info(s"Reseting memory scale")
      memoryScale.resetCounter
  }

  private def sampleMemory = {
    logger.debug("Sampling memory")
    val f: Future[RedisSample] = ask(eventBus, GetRedisMemoryUsage).mapTo[RedisSample]
    f map handleResponse
  }

  private def handleResponse(sample: RedisSample) = {
    logger.debug(s"got sample: $sample")

    // Take the average across all samples and if it's beyond maxMemory, then increment the maxMemory counter
    val averageMemory = (sample._1.sum.toFloat / sample._2).toInt

    if (averageMemory < minMemory) {
      logger.info(s"Sampled memory averaged across all nodes is $averageMemory, and lesser than minMemory=$minMemory: Decrementing memory scale")
      memoryScale.decrementCounter
    } else if (averageMemory > maxMemory) {
      logger.info(s"Sampled memory averaged across all nodes is $averageMemory, and greater than maxMemory=$maxMemory: Incrementing memory scale")
      memoryScale.incrementCounter
    } else {
      logger.info(s"Sampled memory averaged across all nodes is $averageMemory, is greater than minMemory=$minMemory, and is lesser than maxMemory=$maxMemory: Resetting memory scale")
      memoryScale.resetCounter
    }
  }

}

