/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.hyperdrive.trigger.scheduler.executors.shell

import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}
import za.co.absa.hyperdrive.trigger.models.{JobInstance, ShellParameters}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import za.co.absa.hyperdrive.trigger.models.enums.JobStatuses.{Failed, InQueue, Lost, Running, Submitting, Succeeded}
import za.co.absa.hyperdrive.trigger.models.enums.JobTypes.Shell
import za.co.absa.hyperdrive.trigger.scheduler.utilities.ShellExecutorConfig

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class ShellExecutorTest extends FlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with MockitoSugar {

  private val updateJobStub: JobInstance => Future[Unit] = mock[JobInstance => Future[Unit]]

  private val testScriptLocation = "testShellScript.sh"
  private val testJobInstance = JobInstance(
    jobName = "jobName",
    jobType = Shell,
    jobParameters = ShellParameters(scriptLocation = ""),
    jobStatus = InQueue,
    executorJobId = None,
    applicationId = None,
    created = LocalDateTime.now(),
    updated = None,
    order = 0,
    dagInstanceId = 0
  )

  override def beforeEach: Unit = {
    org.mockito.Mockito.reset(updateJobStub)
  }

  "ShellExecutor.execute" should "succeeded job when everything is set correctly" in {
    when(updateJobStub.apply(any[JobInstance])).thenReturn(Future.successful((): Unit))
    val shellParameters = ShellParameters.apply(scriptLocation = Paths.get(ShellExecutorConfig.getExecutablesFolder, testScriptLocation).toString)
    val testInput = testJobInstance.copy(
      jobParameters = shellParameters
    )

    Await.result(ShellExecutor.execute(testInput, shellParameters, updateJobStub.apply), Duration(120, TimeUnit.SECONDS))

    verify(updateJobStub, times(2)).apply(ArgumentMatchers.any())
    verify(updateJobStub, times(1)).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Running)))
    verify(updateJobStub, times(1)).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Succeeded)))
  }

  "ShellExecutor.execute" should "fail job when job with running status is executed" in {
    when(updateJobStub.apply(any[JobInstance])).thenReturn(Future.successful((): Unit))
    val testInput = testJobInstance.copy(jobStatus = Running)
    val shellParameters: ShellParameters = testInput.jobParameters match {
      case shellParameters: ShellParameters =>shellParameters
      case _ => throw new Exception("Incorrect job instance parameters")
    }

    Await.result(ShellExecutor.execute(testInput, shellParameters, updateJobStub.apply), Duration(120, TimeUnit.SECONDS))

    verify(updateJobStub).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Failed)))
  }

  "ShellExecutor.execute" should "fail job when script cant be found" in {
    when(updateJobStub.apply(any[JobInstance])).thenReturn(Future.successful((): Unit))
    val shellParameters = ShellParameters(scriptLocation = "/invalidLocation/invalidScriptName.sh")
    val testInput = testJobInstance.copy(
      jobParameters = shellParameters
    )
    Await.result(ShellExecutor.execute(testInput, shellParameters, updateJobStub.apply), Duration(120, TimeUnit.SECONDS))

    verify(updateJobStub, times(2)).apply(ArgumentMatchers.any())
    verify(updateJobStub, times(1)).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Running)))
    verify(updateJobStub, times(1)).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Failed)))
  }

  "ShellExecutor.execute" should "fail job when job with incorrect status is executed" in {
    when(updateJobStub.apply(any[JobInstance])).thenReturn(Future.successful((): Unit))
    val testInput = testJobInstance.copy(jobStatus = Submitting)

    val shellParameters: ShellParameters = testInput.jobParameters match {
      case shellParameters: ShellParameters =>shellParameters
      case _ => throw new Exception("Incorrect job instance parameters")
    }

    Await.result(ShellExecutor.execute(testInput, shellParameters, updateJobStub.apply), Duration(120, TimeUnit.SECONDS))

    verify(updateJobStub).apply(ArgumentMatchers.eq(testInput.copy(jobStatus = Lost)))
  }

}
