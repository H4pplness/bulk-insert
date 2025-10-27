package com.sql_test.test_heavy_write;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/engineer")
@AllArgsConstructor
public class EngineerController {
    @Autowired
    private final EngineerService engineerService;
    @Autowired
    private TestIsolationService testIsolationService;
    @Autowired
    private TestTransactionalService testTransactionalService;

    @GetMapping("/insert-engineers")
    public void insertEngineers(@RequestParam("n")Integer numberOfEngineer) {
        engineerService.insertEngineer(numberOfEngineer);
    }

    /**
     * 1 : đơn luồng, 2 : đa luồng skip locked, 3 : đa luồng lấy id theo số dư khi chia 1000, 4 : đa luồng khi phân mảnh dữ liệu theo id từ bé đến lớn, mỗi batch lấy 1000 rows, 5: tận dụng cursor
     * @param strategy
     * @throws InterruptedException
     */

    @GetMapping("/sync-engineers")
    public void syncEngineers(@RequestParam("strategy")Integer strategy) {
        engineerService.syncEngineer(strategy);
    }


    @GetMapping("/isolation/sync")
    public void sync() {
        testIsolationService.syncData();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Integer id,@RequestParam("sleep") Integer sleep) throws InterruptedException {
        testTransactionalService.update(id,sleep);
    }

    @GetMapping("/sync-hashing")
    public void syncEngineersByConsistentHashing() {
        engineerService.syncEngineerByConsistentHashing();
    }
}
