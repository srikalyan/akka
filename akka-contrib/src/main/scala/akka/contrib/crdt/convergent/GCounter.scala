/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.contrib.crdt.convergent

import akka.cluster.VectorClock
import akka.actor.Address
import akka.cluster.Cluster

object GCounter {
  val empty: GCounter = new GCounter
  def apply(): GCounter = empty
}

/**
 * Implements a ConvergentReplicatedDataType 'Growing Counter' also called a 'G-Counter'.
 *
 * A G-Counter is a increment-only counter (inspired by vector clocks) in
 * which only increment and merge are possible. Incrementing the counter
 * adds 1 to the count for the current actor. Divergent histories are
 * resolved by taking the maximum count for each actor (like a vector
 * clock merge). The value of the counter is the sum of all actor counts.
 */
case class GCounter(
  private[crdt] val state: Map[Address, Long] = Map.empty[Address, Long])
  extends ConvergentReplicatedDataType {

  type T = GCounter

  def value: Long = state.values.sum

  def :+(delta: Long)(implicit cluster: Cluster): GCounter = increment(cluster, delta)

  def increment(node: Cluster, delta: Long = 1): GCounter =
    increment(node.selfAddress, delta)

  private[crdt] def increment(key: Address): GCounter = increment(key, 1)

  private[crdt] def increment(key: Address, delta: Long): GCounter = {
    require(delta >= 0, "Can't decrement a GCounter")
    state.get(key) match {
      case Some(v) ⇒
        val tot = v + delta
        require(tot >= 0, "Number overflow")
        copy(state = state + (key -> tot))
      case None ⇒ copy(state = state + (key -> delta))
    }
  }

  override def merge(that: GCounter): GCounter = {
    var merged = that.state
    for ((key, thisValue) ← state) {
      val thatValue = merged.getOrElse(key, 0L)
      if (thisValue > thatValue)
        merged = merged.updated(key, thisValue)
    }
    GCounter(merged)
  }
}

