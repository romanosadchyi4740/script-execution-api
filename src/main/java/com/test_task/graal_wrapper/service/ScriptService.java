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

/**
 * Service to manage script execution and status.
 */
@Service
public class ScriptService {
    public static final String STOPPED = "stopped";
    public static final String EXECUTING = "executing";
    public static final String COMPLETED = "completed";
    public static final String FAILED = "failed";
    public static final String QUEUED = "queued";
    private final Map<String, Script> scripts = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Execute a JavaScript script in non-blocking mode.
     *
     * @param scriptText the script to execute
     * @return the script ID
     */
    public String executeNonBlocking(String scriptText) {
        String scriptId = UUID.randomUUID().toString();
        Script script = new Script(scriptId, scriptText, QUEUED);
        scripts.put(scriptId, script);

        executorService.submit(() -> executeSingleMethod(scriptText, scriptId, script));

        return scriptId;
    }

    /**
     * Execute a JavaScript script in blocking mode.
     *
     * @param scriptText the script to execute
     * @return the output, stdout, and stderr of the script
     */
    public Script executeBlocking(String scriptText) {
        String scriptId = executeNonBlocking(scriptText);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return scripts.get(scriptId);
    }

    /**
     * Executes a single JavaScript script using GraalVM and updates the script's status and output.
     *
     * @param scriptText the JavaScript code to execute
     * @param scriptId the unique identifier of the script
     * @param script the Script object to update with execution details
     */
    private void executeSingleMethod(String scriptText, String scriptId, Script script) {
        script.setStatus(EXECUTING);
        script.setStartTime(new Date(System.currentTimeMillis()));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try (Context context = Context.newBuilder("js")
                .out(stdout)
                .err(stderr)
                .build()) {
            Thread outputWriter = new Thread(() -> {
                while (script.getStatus().equals(EXECUTING)) {
                    script.setStdout(stdout.toString(StandardCharsets.UTF_8));
                    script.setStderr(stderr.toString(StandardCharsets.UTF_8));

                    try {
                        //noinspection BusyWait
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                }

                if (script.getStatus().equals(STOPPED)) {
                    context.close(true);
                }
            });
            outputWriter.start();
            String result = context.eval(Source.newBuilder("js", scriptText, scriptId).build()).toString();

            script.setStatus(COMPLETED);
            script.setOutput(result);
        } catch (Exception e) {
            if (!script.getStatus().equals(STOPPED)) {
                script.setStatus(FAILED);
                script.setError(e.getMessage());
                script.setStackTrace(Arrays.toString(e.getStackTrace()));
            }
        }

        script.setExecutionTime(System.currentTimeMillis() - script.getStartTime().getTime() + " ms.");
        script.setStdout(stdout.toString(StandardCharsets.UTF_8));
        script.setStderr(stderr.toString(StandardCharsets.UTF_8));
    }

    /**
     * List all scripts with optional filtering and ordering.
     *
     * @param status  optional list of statuses to filter by
     * @param orderBy optional field to order by
     * @return the list of scripts
     */
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

    /**
     * Get details of a specific script.
     *
     * @param id the script ID
     * @return the script details
     */
    public Script getScriptDetails(String id) {
        return scripts.get(id);
    }

    /**
     * Stop a running script.
     *
     * @param id the script ID
     */
    public void stopScript(String id) {
        Script script = scripts.get(id);
        if (script != null && (EXECUTING.equals(script.getStatus()) || QUEUED.equals(script.getStatus()))) {
            script.setStatus(STOPPED);
        }
    }

    /**
     * Remove inactive scripts by their IDs.
     *
     * @param ids the list of script IDs to remove
     */
    public void cleanupScripts(List<String> ids) {
        ids.forEach(id -> {
            String tempStatus = scripts.get(id).getStatus();
            if (tempStatus.equals(STOPPED) || tempStatus.equals(COMPLETED) || tempStatus.equals(FAILED)) {
                scripts.remove(id);
            }
        });
    }
}