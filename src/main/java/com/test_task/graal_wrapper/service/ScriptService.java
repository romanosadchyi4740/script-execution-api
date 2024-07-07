package com.test_task.graal_wrapper.service;

import com.test_task.graal_wrapper.entity.Script;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import java.util.concurrent.Executors;


@Service
public class ScriptService {
    public static final String STOPPED = "stopped";
    public static final String EXECUTING = "executing";
    public static final String COMPLETED = "completed";
    public static final String FAILED = "failed";
    public static final String QUEUED = "queued";
    private final Map<String, Script> scripts = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public String executeNonBlocking(String scriptText) {
        String scriptId = UUID.randomUUID().toString();
        Script scriptStatus = new Script(scriptId, scriptText, QUEUED);
        scripts.put(scriptId, scriptStatus);

        executorService.submit(() -> {
            scriptStatus.setStatus(EXECUTING);
            scriptStatus.setStartTime(new Date(System.currentTimeMillis()));
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            try (Context context = Context.newBuilder("js")
                    .out(stdout)
                    .err(stderr)
                    .build()) {
                Thread outputWriter = new Thread(() -> {
                    while (scriptStatus.getStatus().equals(EXECUTING)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        scriptStatus.setStdout(stdout.toString(StandardCharsets.UTF_8));
                        scriptStatus.setStderr(stderr.toString(StandardCharsets.UTF_8));
                    }

                    if (scriptStatus.getStatus().equals(STOPPED)) {
                        context.close(true);
                    }
                });
                outputWriter.start();
                String result = context.eval(Source.newBuilder("js", scriptText, scriptId).build()).toString();

                scriptStatus.setStatus(COMPLETED);
                scriptStatus.setOutput(result);
            } catch (Exception e) {
                if (!scriptStatus.getStatus().equals(STOPPED)) {
                    scriptStatus.setStatus(FAILED);
                    scriptStatus.setError(e.getMessage());
                    scriptStatus.setStackTrace(Arrays.toString(e.getStackTrace()));
                }
            }

            scriptStatus.setExecutionTime(System.currentTimeMillis() - scriptStatus.getStartTime().getTime() + " ms.");
            scriptStatus.setStdout(stdout.toString(StandardCharsets.UTF_8));
            scriptStatus.setStderr(stderr.toString(StandardCharsets.UTF_8));
        });

        return scriptId;
    }

    public String executeBlocking(String scriptText) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try (Context context = Context.newBuilder("js")
                .out(stdout)
                .err(stderr)
                .build()) {
            String result =
                    context.eval(Source.newBuilder("js", scriptText, "script").build()).toString();
            return "Output: "
                    + result
                    + "\nStdout: "
                    + stdout.toString(StandardCharsets.UTF_8)
                    + "\nStderr: "
                    + stderr.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nStderr: " + stderr.toString(StandardCharsets.UTF_8);
        }
    }

    public List<Script> listScripts(Optional<List<String>> status, Optional<String> orderBy) {
        List<Script> scriptsArray = new ArrayList<>(scripts.values());
        status.ifPresent(statusList -> scriptsArray.removeIf(script -> !statusList.contains(script.getStatus())));
        orderBy.ifPresent(order -> {
            if (order.equals("desc")) {
                scriptsArray.sort(Comparator.comparing(Script::getId).reversed());
            } else {
                scriptsArray.sort(Comparator.comparing(Script::getId));
            }
        });
        return scriptsArray;
    }

    public Script getScriptDetails(String id) {
        return scripts.get(id);
    }

    public void stopScript(String id) {
        Script script = scripts.get(id);
        if (script != null && (EXECUTING.equals(script.getStatus()) || QUEUED.equals(script.getStatus()))) {
            script.setStatus(STOPPED);
        }
    }

    public void cleanupScripts(List<String> ids) {
        ids.forEach(id -> {
            String tempStatus = scripts.get(id).getStatus();
            if (tempStatus.equals(STOPPED) || tempStatus.equals(COMPLETED) || tempStatus.equals(FAILED)) {
                scripts.remove(id);
            }
        });
    }
}