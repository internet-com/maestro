//package com.adendamedia
//
//import akka.testkit.TestKit
//import akka.actor.ActorSystem
//import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}
//
//class EventCounterTest extends TestKit(ActorSystem("EventCounterTest"))
//  with WordSpecLike with BeforeAndAfterAll with MustMatchers {
//  implicit val ec = system.dispatcher
//
//  override def afterAll(): Unit = {
//    system.terminate()
//  }
//
//  "ChannelEventCounter" must {
//    "roll over to zero after passing max_val" in {
//      implicit val max_val: Int = 5
//      val cntr = new ChannelEventCounter(system)
//      for (_ <- 1 to max_val + 1) {
//        cntr.incrementCounter
//      }
//      cntr.getEventCounterNumber() must be(new StateChannelEventCounter(0))
//    }
//  }
//
//  "PatternEventCounter" must {
//    "roll over to zero after passing max_val" in {
//      implicit val max_val: Int = 5
//      val cntr = new PatternEventCounter(system)
//      for (_ <- 1 to max_val + 1) {
//        cntr.incrementCounter
//      }
//      cntr.getEventCounterNumber() must be(new StatePatternEventCounter(0))
//    }
//  }
//}
