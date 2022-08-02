package org.sunbird.cb.preprocessor.task

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.scala.OutputTag
import org.sunbird.dp.core.job.BaseJobConfig
import org.sunbird.cb.preprocessor.domain.Event

class CBPreprocessorConfig(override val config: Config) extends BaseJobConfig(config, "CBPreprocessorJob") {

  private val serialVersionUID = 2905979434303791379L  // TODO: change this?

  implicit val eventTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
  implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

   val schemaPath: String = config.getString("telemetry.schema.path")

  // Kafka Topic Configuration
  val kafkaInputTopic: String = config.getString("kafka.input.topic")
  val kafkaOutputCbAuditTopic: String = config.getString("kafka.output.cb.audit.topic")
  val kafkaOutputCbWorkOrderRowTopic: String = config.getString("kafka.output.cb.work.order.row.topic")
  val kafkaOutputCbWorkOrderOfficerTopic: String = config.getString("kafka.output.cb.work.order.officer.topic")
  val kafkaFailedTopic: String = config.getString("kafka.output.failed.topic")

  val defaultChannel: String = config.getString("default.channel")

  // Output tags
  val cbAuditEventsOutputTag: OutputTag[Event] = OutputTag[Event]("cb-audit-events")
  val cbWorkOrderRowOutputTag: OutputTag[Event] = OutputTag[Event]("cb-work-order-row")
  val cbWorkOrderOfficerOutputTag: OutputTag[Event] = OutputTag[Event]("cb-work-order-officer")
  val cbFailedOutputTag: OutputTag[Event] = OutputTag[Event]("cb-failed-events")
  val validationFailedEventsOutputTag: OutputTag[Event] = OutputTag[Event]("validation-failed-events")
  val duplicateEventsOutputTag: OutputTag[Event] = OutputTag[Event]("duplicate-events")

  override val kafkaConsumerParallelism: Int = config.getInt("task.consumer.parallelism")
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

  // Router job metrics
  val cbWorkOrderRowMetricCount = "cb-work-order-row-count"
  val cbWorkOrderOfficerMetricCount = "cb-work-order-officer-count"
  val cbAuditEventMetricCount = "cb-audit-route-success-count"
  val cbAuditFailedMetricCount = "cb-audit-route-failed-count"

  // Validation job metrics
   val validationSuccessMetricsCount = "validation-success-event-count"
   val validationFailureMetricsCount = "validation-failed-event-count"

  // Consumers
  val cbPreprocessorConsumer = "cb-preprocessor-consumer"

  // Producers
  val cbAuditProducer = "cb-audit-sink"
  val cbWorkOrderRowProducer = "cb-work-order-row-sink"
  val cbWorkOrderOfficerProducer = "cb-work-order-officer-sink"
  val cbFailedEventProducer = "cb-failed-events-sink"

   val defaultSchemaFile = "envelope.json"

  val dedupStore: Int = config.getInt("redis.database.duplicationstore.id")
  val cacheExpirySeconds: Int = config.getInt("redis.database.key.expiry.seconds")
  val DEDUP_FLAG_NAME = "cb_duplicate"

  val kafkaDuplicateTopic: String = config.getString("kafka.output.duplicate.topic")
  val duplicateEventProducer = "duplicate-events-sink"

  val VALIDATION_FLAG_NAME = "cbp_validation_processed"

}
