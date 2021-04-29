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

package za.co.absa.hyperdrive.trigger.models.tables

import slick.lifted.ProvenShape
import za.co.absa.hyperdrive.trigger.models.enums.JobTypes.JobType
import za.co.absa.hyperdrive.trigger.models.{JobParameters, JobParametersTemplate, JobTemplate}

import scala.collection.immutable.SortedMap

trait JobTemplateTable extends SearchableTableQuery {
  this: Profile with JdbcTypeMapper =>
  import api._

  final class JobTemplateTable(tag: Tag) extends Table[JobTemplate](tag, _tableName = "job_template") with SearchableTable {

    def name: Rep[String] = column[String]("name", O.Unique)
    def jobType: Rep[JobType] = column[JobType]("job_type")
    def jobParameters: Rep[JobParametersTemplate] = column[JobParametersTemplate]("job_parameters", O.SqlType("JSON"))
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc, O.SqlType("BIGSERIAL"))
    def formConfig: Rep[String] = column[String]("form_config")

    def * : ProvenShape[JobTemplate] = (name, jobType, jobParameters, id, formConfig) <> (
      jobTemplateTuple =>
        JobTemplate.apply(
          name = jobTemplateTuple._1,
          jobType = jobTemplateTuple._2,
          jobParameters = jobTemplateTuple._3,
          id = jobTemplateTuple._4,
          formConfig = jobTemplateTuple._5
        ),
      (jobTemplate: JobTemplate) =>
        Option(
          jobTemplate.name,
          jobTemplate.jobType,
          jobTemplate.jobParameters,
          jobTemplate.id,
          jobTemplate.formConfig
        )
    )

    override def fieldMapping: Map[String, Rep[_]] = Map(
      "name" -> this.name,
      "jobType" -> this.jobType,
      "jobParameters" -> this.jobParameters,
      "id" -> this.id,
      "formConfig" -> this.formConfig
    )

    override def defaultSortColumn: Rep[_] = id
  }

  lazy val jobTemplateTable: TableQuery[JobTemplateTable] = TableQuery[JobTemplateTable]

}
