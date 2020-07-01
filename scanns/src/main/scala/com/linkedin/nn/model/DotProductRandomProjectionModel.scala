/**
 * Copyright 2018 LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.nn.model

import com.linkedin.nn.Types.BandedHashes
import com.linkedin.nn.distance.{DotProductDistance, Distance}
import com.linkedin.nn.lsh.{HashFunction, SignRandomProjectionHashFunction}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.ml.util.Identifiable

/**
 * Model to perform nearest neighbor search in cosine distance space
 *
 * @param hashFunctions Array of random projections that will be used for performing LSH
 */
class DotProductRandomProjectionModel(val uid: String = Identifiable.randomUID("DotRandomProjectionLSH"),
                                    private[nn] val hashFunctions: Array[SignRandomProjectionHashFunction])
  extends LSHNearestNeighborSearchModel[DotProductRandomProjectionModel] {

  override val distance: Distance = DotProductDistance

  override private[nn] def getHashFunctions: Array[SignRandomProjectionHashFunction] = hashFunctions

  /**
   * Given an input vector, get the banded hashes by hashing it using the hash functions
   *
   * @param x input vector
   * @return banded hashes
   */
  override def getBandedHashes(x: Vector): BandedHashes = {
    hashFunctions.map { h =>
      Array(
        h.compute(x)
          .reduceLeft((x, y) => (x << 1) + y) // Convert the binary signature to an int using Horner's evaluation
      )
    }
  }

  override def copy(extra: ParamMap): Params = defaultCopy(extra)
}