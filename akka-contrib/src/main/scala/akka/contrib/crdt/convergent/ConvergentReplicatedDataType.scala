/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.contrib.crdt.convergent

/**
 * Interface for implementing a state based convergent
 * replicated data type (CvRDT).
 */
trait ConvergentReplicatedDataType {
  type T <: ConvergentReplicatedDataType
  def merge(that: T): T
}

/**
 * Java API: Interface for implementing a [[ConvergentReplicatedDataType]] in
 * Java.
 */
abstract class ConvergentReplicatedDataTypeBase extends ConvergentReplicatedDataType {
  // it is not possible to use a more strict type, because it is erased somehow, and 
  // the implementation is anyway required to implement
  // merge(that: ConvergentReplicatedDataType): ConvergentReplicatedDataType
  type T = ConvergentReplicatedDataTypeBase

}