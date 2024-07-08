package com.test_task.graal_wrapper.service;

import com.test_task.graal_wrapper.entity.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScriptServiceTest {
    @Autowired
    private ScriptService scriptService;

    @Test
    public void helloWorldInStdout() throws InterruptedException {
        String scriptText = "console.log('Hello, World!')";
        String scriptId = scriptService.executeNonBlocking(scriptText);
        assertNotNull(scriptId);

        Thread.sleep(2500);

        Script script = scriptService.getScriptDetails(scriptId);
        assertNotNull(script);
        assertEquals(ScriptService.COMPLETED, script.getStatus());
        assertEquals(script.getStdout(), "Hello, World!\n");
    }

    @Test
    public void helloWorldInStderr() throws InterruptedException {
        String scriptText = "console.error('Hello, World!')";
        String scriptId = scriptService.executeNonBlocking(scriptText);
        assertNotNull(scriptId);

        Thread.sleep(2500);

        Script script = scriptService.getScriptDetails(scriptId);
        assertNotNull(script);
        assertEquals(ScriptService.COMPLETED, script.getStatus());
        assertEquals(script.getStderr(), "Hello, World!\n");
    }

    @Test
    public void aPlusB() throws InterruptedException {
        String scriptText = """
                let a = 5;
                let b = 2;
                let c = a + b;
                console.log(c);
                console.error("errorrr");""";
        String scriptId = scriptService.executeNonBlocking(scriptText);
        assertNotNull(scriptId);

        Thread.sleep(2500);

        Script script = scriptService.getScriptDetails(scriptId);
        assertNotNull(script);
        assertEquals(ScriptService.COMPLETED, script.getStatus());
        assertEquals(script.getStdout(), "7\n");
        assertEquals(script.getStderr(), "errorrr\n");
    }

    @Test
    public void testExecuteBlocking() throws InterruptedException {
        String scriptText = """
                function slowFunction() {
                    for (let i = 0; i < 1000000000; i++) {
                        if (i % 1000000 == 0) {
                            console.log(i / 1000000);
                        }
                    }
                    return "Function complete";
                }

                console.log(slowFunction());""";
        String scriptId = scriptService.executeNonBlocking(scriptText);
        assertNotNull(scriptId);

        Thread.sleep(2500);

        Script script = scriptService.getScriptDetails(scriptId);
        assertNotNull(script);
        assertEquals(ScriptService.EXECUTING, script.getStatus());
    }

    @Test
    public void testListScripts() {
        List<Script> scripts = scriptService.listScripts(Optional.empty(), Optional.empty());
        assertNotNull(scripts);
    }
}
