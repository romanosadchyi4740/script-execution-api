package com.test_task.graal_wrapper.rest;

import com.test_task.graal_wrapper.entity.Script;
import com.test_task.graal_wrapper.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scripts")
public class ScriptController {
    private final ScriptService scriptService;

    @Autowired
    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeScript(
            @RequestBody String script,
            @RequestParam(defaultValue = "false") boolean blocking
    ) {
        if (blocking) {
            return ResponseEntity.ok(scriptService.executeBlocking(script));
        } else {
            return ResponseEntity.ok(scriptService.executeNonBlocking(script));
        }
    }

    @GetMapping
    public ResponseEntity<List<Script>> listScripts(@RequestParam Optional<List<String>> status,
                                                    @RequestParam Optional<String> orderBy) {
        return ResponseEntity.ok(scriptService.listScripts(status, orderBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptDetails(@PathVariable String id) {
        return ResponseEntity.ok(scriptService.getScriptDetails(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> stopScript(@PathVariable String id) {
        scriptService.stopScript(id);
        return ResponseEntity.ok("Script stopped");
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupScripts(@RequestBody List<String> ids) {
        scriptService.cleanupScripts(ids);
        return ResponseEntity.ok("Inactive scripts cleaned");
    }
}