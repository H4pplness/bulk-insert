package com.sql_test.test_heavy_write;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/engineer")
@AllArgsConstructor
public class EngineerController {
    @Autowired
    private final EngineerService engineerService;

        @GetMapping("/sync-engineer")
    public void syncEngineers(){
        engineerService.syncEngineer();
    }
}
