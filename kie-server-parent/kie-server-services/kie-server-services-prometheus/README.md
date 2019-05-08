Kie Server Prometheus Extension
===============================

To enable this extension, all that is needed is to set the following system property to false: _org.kie.prometheus.server.ext.disabled=false_.
By default, this extension is disabled. Once the extension started, you can access the available metrics on _${context}/services/rest/metrics_.  
For example: 

``` 
curl -u wbadmin:wbadmin http://localhost:8080/kie-server/services/rest/metrics  
``` 

# Available metrics for Prometheus

## Kie Server metrics

kie_server_start_time: Kie Server Start Time (Gauge)  
kie_server_deployments_active_total: Kie Server Active Deployments (Gauge)  
kie_server_container_running_total: Kie Server Running Containers (Gauge)  
kie_server_container_started_total: Kie Server Started Containers (Counter)

## Data Sets

kie_server_data_set_registered_total: Kie Server Data Set Registered (Gauge)  
kie_server_data_set_lookups_total: Kie Server Data Set Running Lookups (Gauge)  
kie_server_data_set_execution_time_seconds: Kie Server Data Set Execution Time (Summary)  
kie_server_data_set_execution_total: Kie Server Data Set Execution (Counter)

## Case Management

kie_server_case_duration_seconds: Kie Server Case Duration (Summary)  
kie_server_case_running_total: Kie Server Running Cases (Gauge)  
kie_server_case_started_total: Kie Server Started Cases (Counter)

## Execution Error

kie_server_execution_error_total: Kie Server Execution Errors (Counter)

## Jobs

kie_server_job_running_total: Kie Server Running Jobs (Gauge)  
kie_server_job_scheduled_total: Kie Server Started Jobs (Counter)  
kie_server_job_executed_total: Kie Server Executed Jobs (Counter)  
kie_server_job_cancelled_total: Kie Server Cancelled Jobs (Counter)  
kie_server_job_duration_seconds: Kie Server Job Duration (Summary) 

## Processes

kie_server_process_instance_started_total: Kie Server Started Process Instances (Counter)  
kie_server_process_instance_sla_violated_total: Kie Server Process Instances SLA Violated (Counter)  
kie_server_process_instance_completed_total: Kie Server Completed Process Instances (Counter)  
kie_server_process_instance_running_total: Kie Server Running Process Instances (Gauge)  
kie_server_process_instance_duration_seconds: Kie Server Process Instances Duration (Summary)  
kie_server_work_item_duration_seconds: Kie Server Work Items Duration (Summary)

## Tasks

kie_server_task_added_total: Kie Server Added Tasks (Counter)  
kie_server_task_skipped_total: Kie Server Skipped Tasks (Counter)  
kie_server_task_completed_total: Kie Server Completed Tasks (Counter)  
kie_server_task_failed_total: Kie Server Failed Tasks (Counter)  
kie_server_task_exited_total: Kie Server Exited Tasks (Counter)  
kie_server_task_duration_seconds: Kie Server Task Duration (Summary)