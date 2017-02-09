package com.sample;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;

public class ProcessManagementUtils {

	private static final String USERNAME = "krisv";
	private static final String PASSWORD = "krisv";

	public static void main(String[] args) throws Exception {
		startProcessAndHandleTaskViaRestRemoteJavaAPI(new URL("http://localhost:8080/jbpm-console/"),
				"org.jbpm:Evaluation:1.0", USERNAME, PASSWORD);
	}

	public static void startProcessAndHandleTaskViaRestRemoteJavaAPI(URL serverRestUrl, String deploymentId,
			String user, String password) {
		// the serverRestUrl should contain a URL similar to
		// "http://localhost:8080/jbpm-console/"

		// Setup the factory class with the necessarry information to
		// communicate with the REST services
		RuntimeEngine engine = RemoteRuntimeEngineFactory.newRestBuilder().addUrl(serverRestUrl).addTimeout(5)
				.addDeploymentId(deploymentId).addUserName(user).addPassword(password).build();

		// Create KieSession and TaskService instances and use them
		KieSession ksession = engine.getKieSession();
		TaskService taskService = engine.getTaskService();

		// Each operation on a KieSession, TaskService or AuditLogService
		// (client) instance
		// sends a request for the operation to the server side and waits for
		// the response
		// If something goes wrong on the server side, the client will throw an
		// exception.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("employee", USERNAME);
		params.put("reason", "Mid term");
		ProcessInstance processInstance = ksession.startProcess("evaluation", params);
		long procId = processInstance.getId();

		String taskUserId = user;
		taskService = engine.getTaskService();
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");

		long taskId = -1;
		for (TaskSummary task : tasks) {
			System.out.println(task.getProcessInstanceId());
			if (task.getProcessInstanceId() == procId) {
				taskId = task.getId();
			}
		}

		if (taskId == -1) {
			throw new IllegalStateException("Unable to find task for " + user + " in process instance " + procId);
		}

		taskService.start(taskId, taskUserId);

		// resultData can also just be null
		Map<String, Object> resultData = new HashMap<String, Object>();
		taskService.complete(taskId, taskUserId, resultData);
	}

}
