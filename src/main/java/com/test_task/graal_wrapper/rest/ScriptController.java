package com.test_task.graal_wrapper.rest;

import com.test_task.graal_wrapper.entity.Script;
import com.test_task.graal_wrapper.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller to handle script-related API requests.
 */
@RestController
@RequestMapping("/api/scripts")
public class ScriptController {
    private final ScriptService scriptService;

    /**
     * Constructor
     *
     * @param scriptService the ScriptService to be injected
     */
    @Autowired
    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    /**
     * Execute a JavaScript script.
     *
     * @param scriptText  the script to execute
     * @param blocking whether to execute in blocking mode
     * @return the script ID
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeScript(
            @RequestBody String scriptText,
            @RequestParam(defaultValue = "false") boolean blocking
    ) {
        if (blocking) {
            return ResponseEntity.ok(scriptService.executeBlocking(scriptText));
        } else {
            return ResponseEntity.ok(scriptService.executeNonBlocking(scriptText));
        }
    }

    /**
     * List all scripts with optional filtering and ordering.
     *
     * @param status  optional list of statuses to filter by
     * @param orderBy optional field to order by
     * @return the list of scripts
     */
    @GetMapping
    public ResponseEntity<List<Script>> listScripts(@RequestParam Optional<List<String>> status,
                                                    @RequestParam Optional<String> orderBy) {
        return ResponseEntity.ok(scriptService.listScripts(status, orderBy));
    }

    /**
     * Get details of a specific script.
     *
     * @param id the script ID
     * @return the script details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptDetails(@PathVariable String id) {
        return ResponseEntity.ok(scriptService.getScriptDetails(id));
    }

    /**
     * Stop a running script.
     *
     * @param id the script ID
     * @return string response
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> stopScript(@PathVariable String id) {
        scriptService.stopScript(id);
        return ResponseEntity.ok("Script stopped");
    }

    /**
     * Remove inactive scripts by their IDs.
     *
     * @param ids the list of script IDs to remove
     * @return string response
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupScripts(@RequestBody List<String> ids) {
        scriptService.cleanupScripts(ids);
        return ResponseEntity.ok("Inactive scripts cleaned");
    }
}