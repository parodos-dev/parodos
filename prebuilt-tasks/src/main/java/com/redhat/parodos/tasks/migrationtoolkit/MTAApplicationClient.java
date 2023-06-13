package com.redhat.parodos.tasks.migrationtoolkit;

record Repository(String kind, String url, String branch) {
}

record App(int id, String name, Repository repository) {
}

record Mode(boolean binary, boolean withDeps, boolean diva, String artifact) {
}

record Rules(String path, String tags) {
}

record Packages(String[] included, String[] excluded) {
}

record Scope(boolean withKnown, Packages packages) {
}

record Data(Mode mode, String output, Rules rules, Scope scope, String[] sources, String[] targets) {
}

record Task(App application, String state, String name, String addon, Data data, String bucket) {
}

record TaskGroup(int id, String name, String state, String addon, Data data, String bucket, Task[] tasks) {
	static TaskGroup ofCloudReadiness(int appID) {
		return new TaskGroup(0, "taskgroups.windup", null, "windup",
				new Data(new Mode(false, false, false, ""), "/windup/report", new Rules("", null),
						new Scope(false, new Packages(new String[] {}, new String[] {})), new String[] {},
						new String[] { "cloud-readiness" }),
				null, new Task[] { new Task(new App(appID, "parodos", null), null,
						String.format("parodos.%s.windup", appID), null, null, null) });
	}
}

sealed interface Result<V> {

	record Success<V> (V value) implements Result<V> {
	}

	record Failure<V> (Throwable t) implements Result<V> {
	}

}

interface MTAApplicationClient {

	Result<App> get(String name);

	Result<App> create(App app);

}

interface MTATaskGroupClient {

	Result<TaskGroup> create(int appId);

	Result<TaskGroup> get(int id);

}
