package com.sql_test.test_heavy_write;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestTransactionalService {
    private final EngineerRepository engineerRepository;


    @Transactional(rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ)
    public void update(Integer id,Integer sleepTime) throws InterruptedException {
        EngineerEntity engineerEntity = engineerRepository.findAndLock(id);
        engineerEntity.setSyncStatus(0);
        Thread.sleep(sleepTime);
        engineerRepository.save(engineerEntity);
    }
}
