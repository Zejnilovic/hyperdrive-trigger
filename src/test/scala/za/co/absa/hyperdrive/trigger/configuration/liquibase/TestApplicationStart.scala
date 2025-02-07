
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

package za.co.absa.hyperdrive.trigger.configuration.liquibase

import javax.inject.Inject
import org.scalatest.{FlatSpec, Matchers}
import za.co.absa.hyperdrive.trigger.api.rest.services.WorkflowFixture
import za.co.absa.hyperdrive.trigger.persistance._
import za.co.absa.hyperdrive.trigger.{HyperDriverManager, SpringIntegrationTest}

import scala.concurrent.ExecutionContext.Implicits.global

class TestApplicationStart extends FlatSpec with Matchers with SpringIntegrationTest with RepositoryTestBase {

  @Inject() var hyperDriverManager: HyperDriverManager = _

  private val workflowHistoryRepository: WorkflowHistoryRepository = new WorkflowHistoryRepositoryImpl {
    override val profile = h2Profile
  }

  private val workflowRepository: WorkflowRepository = new WorkflowRepositoryImpl(workflowHistoryRepository) {
    override val profile = h2Profile
  }

  it should "start the application, including sql migrations, and be able to insert and select from the DB" in {
    hyperDriverManager.isManagerRunning shouldBe true
    val workflowJoined = WorkflowFixture.createWorkflowJoined()
    await(workflowRepository.insertWorkflow(workflowJoined, "test-user"))
    val workflows = await(workflowRepository.getWorkflows())
    workflows.size shouldBe 1
    workflows.head.name shouldBe workflowJoined.name

    // cleanup
    h2SchemaDrop()
  }
}
