package com.sql_test.test_heavy_write;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/engineer")
@AllArgsConstructor
public class EngineerController {
    @Autowired
    private final EngineerService engineerService;

    @GetMapping("/insert-engineers")
    public void insertEngineers(@RequestParam("n")Integer numberOfEngineer) throws InterruptedException {
        engineerService.insertEngineer(numberOfEngineer);
    }

    @GetMapping("/sync-engineers")
    public void syncEngineers() throws InterruptedException {
        engineerService.syncEngineer();
    }
}
