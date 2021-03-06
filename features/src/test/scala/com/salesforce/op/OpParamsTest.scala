/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.op

import com.salesforce.op.test._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.slf4j.LoggerFactory

import scala.util.Failure


@RunWith(classOf[JUnitRunner])
class OpParamsTest extends FlatSpec with TestCommon {

  val log = LoggerFactory.getLogger(this.getClass)

  val expectedParamsSimple = OpParams(
    stageParams = Map(
      "TestClass1" -> Map("param1" -> 11, "param2" -> "blarg", "param3" -> false),
      "TestClass2" -> Map("param1" -> List("a", "b", "c"), "param2" -> 0.25)
    ),
    readerParams = Map("Passenger" -> new ReaderParams()),
    customParams = Map("custom1" -> 1, "custom2" -> "2"),
    customTagName = Some("myTag")
  )

  Spec[OpParams] should "correctly load parameters from a json file" in {
    val workflowParams = OpParams.fromFile(resourceFile(name = "OpParams.json"))
    assertParams(workflowParams.get)
  }

  it should "correctly swap reader params" in {
    val switched = expectedParamsSimple.switchReaderParams()
    readerParamsCompare(expectedParamsSimple.readerParams, switched.alternateReaderParams)
    readerParamsCompare(expectedParamsSimple.alternateReaderParams, switched.readerParams)
  }

  it should "load parameters with two sets of reader params specified" in {
    val workflowParams = OpParams.fromFile(resourceFile(name = "OpParamsWithAltReader.json"))
    val expectedWithAlt = expectedParamsSimple.withValues(alternateReadLocations = Map("Passenger" -> "abc"))
    assertParams(workflowParams.get, expectedWithAlt)
  }

  it should "correctly load parameters with a complex reader format" in {
    val params = OpParams.fromFile(resourceFile(name = "OpParamsComplex.json"))
    val readerParams = params.get.readerParams
    readerParams("Passenger").partitions shouldEqual Some(5)
    readerParams("Passenger").customParams.head shouldEqual("test" -> 1)
  }

  it should "correctly load parameters from a yaml file" in {
    val workflowParams = OpParams.fromFile(resourceFile(name = "OpParams.yaml"))
    assertParams(workflowParams.get)
  }

  it should "fail to load parameters from an invalid file" in {
    val workflowParams = OpParams.fromFile(resourceFile(name = "log4j.properties"))
    workflowParams shouldBe a[Failure[_]]
    workflowParams.failed.get shouldBe a[IllegalArgumentException]
  }

  it should "correctly load parameters from a json string" in {
    val workflowParams = OpParams.fromString(loadResource("/OpParams.json", noSpaces = true))
    assertParams(workflowParams.get)
  }

  it should "correctly load parameters from a yaml string" in {
    val workflowParams = OpParams.fromString(loadResource("/OpParams.yaml"))
    assertParams(workflowParams.get)
  }

  it should "fail to load parameters from an invalid string" in {
    val workflowParams = OpParams.fromString(loadResource("/log4j.properties", noSpaces = true))
    workflowParams shouldBe a[Failure[_]]
    workflowParams.failed.get shouldBe a[IllegalArgumentException]
  }

  private def readerParamsCompare(rp1: Map[String, ReaderParams], rp2: Map[String, ReaderParams]): Unit = {
    rp1.keySet shouldBe rp2.keySet
    rp1.values zip rp2.values foreach { case (r1, r2) =>
      r1.partitions shouldBe r2.partitions
      r1.path shouldBe r2.path
      r1.customParams shouldBe r2.customParams
    }
  }

  private def assertParams(loaded: OpParams, expected: OpParams = expectedParamsSimple): Unit = {
    log.info("loaded:\n" + loaded)
    log.info("expected:\n" + expected)
    expected.stageParams shouldBe loaded.stageParams
    readerParamsCompare(expected.readerParams, loaded.readerParams)
    expected.modelLocation shouldBe loaded.modelLocation
    expected.writeLocation shouldBe loaded.writeLocation
    expected.metricsLocation shouldBe loaded.metricsLocation
    expected.batchDurationSecs shouldBe loaded.batchDurationSecs
    expected.awaitTerminationTimeoutSecs shouldBe loaded.awaitTerminationTimeoutSecs
    expected.metricsCompress shouldBe loaded.metricsCompress
    expected.metricsCodec shouldBe loaded.metricsCodec
    expected.customTagName shouldBe loaded.customTagName
    expected.customTagValue shouldBe loaded.customTagValue
    expected.logStageMetrics shouldBe loaded.logStageMetrics
    expected.collectStageMetrics shouldBe loaded.collectStageMetrics
    expected.customParams shouldBe loaded.customParams
    readerParamsCompare(expected.alternateReaderParams, loaded.alternateReaderParams)
  }

}
