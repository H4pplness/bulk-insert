package com.sql_test.test_heavy_write;

import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sql_test.test_heavy_write.Utils.*;

@Service
@AllArgsConstructor
@Slf4j
public class EngineerService {
    @Autowired
    private final EngineerRepository engineerRepository;

    @Autowired
    private final Faker faker;

    @Autowired
    private final Random random;

    private List<String> listFirstname;
    private List<String> listLastname;

    private final LocalDate startTime = LocalDate.of(2021,1,1);
    private final LocalDate endTime = LocalDate.of(2025,12,31);

    private final List<String> listTitle = List.of("Backend Engineer","Data Engineer","Data Scientist","Frontend Engineer","QA","Fullstack Engineer","Solution Architect","Principal Engineer","Engineer Manager","BA");

    public void insertEngineer(int numberOfEngineer) throws InterruptedException {
        listFirstname = getRandomListFirstName(5000);
        listLastname = getRandomListLastName(5000);

        List<EngineerEntity> engineerEntityList = new ArrayList<>();
        int lastIdx = (int) engineerRepository.count();
        for (int i=lastIdx+1;i<=lastIdx+numberOfEngineer;i++){
            EngineerEntity engineerEntity = createRandomEngineers(i);
            engineerEntityList.add(engineerEntity);
        }

        // tạo 1tr bản ghi để insert vào db
        // thông thường thì khi mà insert vào db, thì phải đợi response từ db phản hồi rồi mới chạy tiếp
        // Khi mà insert data vào db , phải đợi phản hồi từ db

        // Non-blocking và blocking

        // Non-blocking : Khi mà gặp những tác vụ I/O -> nếu mà chạy 1 thread thì nó sẽ block


        List<List<EngineerEntity>> partitionListEngineer = partitionList(engineerEntityList,1000);

        long start = System.currentTimeMillis();

        for (int i = 0; i < partitionListEngineer.size(); i++) {
            final int finalI = i;
//            Thread.ofVirtual().start(()->{
//                batchInsertEngineer(partitionListEngineer.get(finalI));
//                log.info("Time task {} completed: {} ms", finalI, start-System.currentTimeMillis());
//            });
//            tuần tự
//            batchInsertEngineer(partitionListEngineer.get(finalI));
//            log.info("Time task {} completed: {} ms", finalI, start-System.currentTimeMillis());
            // chạy đa luồng
            new Thread(()->{
                batchInsertEngineer(partitionListEngineer.get(finalI));
                log.info("Time task {} completed: {} ms", finalI, start-System.currentTimeMillis());
            }).start();
        }
    }

    public EngineerEntity createRandomEngineers(int id){
        int randomFirstName = random.nextInt(5000);
        int randomLastName = random.nextInt(5000);
        int randomTitle = random.nextInt(listTitle.size());

        int gender = random.nextInt(4)+1;
        int countryId = random.nextInt(240)+1;

        LocalDate startedDate = getRandomDate(startTime,endTime);

        EngineerEntity engineerEntity = new EngineerEntity();
        engineerEntity.setId(id);
        engineerEntity.setFirstname(listFirstname.get(randomFirstName));
        engineerEntity.setLastname(listLastname.get(randomLastName));
        engineerEntity.setTitle(listTitle.get(randomTitle));
        engineerEntity.setGender(gender);
        engineerEntity.setCountryId(countryId);
        engineerEntity.setStartedDate(startedDate);
        engineerEntity.setSyncStatus(0);

        return engineerEntity;
    }




    @Async("VirtualThreadExecutor")
    public void batchInsertEngineer(List<EngineerEntity> listEngineer){
        System.out.println("Running in thread: " + Thread.currentThread());
        engineerRepository.batchInsertEngineer(listEngineer);

        // insert sequentially
//        for (EngineerEntity engineer : listEngineer){
//            engineerRepository.save(engineer);
//        }

    }

    private List<String> getRandomListFirstName(int size){
        List<String> listFirstName = new ArrayList<>();
        for (int i=0;i<size;i++){
            listFirstName.add(faker.name().firstName());
        }

        return listFirstName;
    }

    private List<String> getRandomListLastName(int size){
        List<String> listLastName = new ArrayList<>();
        for (int i=0;i<size;i++){
            listLastName.add(faker.name().lastName());
        }

        return listLastName;
    }


    public void syncEngineer() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();

        // 70%
//        long countEngineer = engineerRepository.count();
//        int batchSize = 1000;
//
//        for (int i = 0; i < countEngineer / batchSize; i++) {
//            executor.execute(()->syncBatchEngineers(start));
//            try {
//                Thread.sleep(50);
//            }catch (InterruptedException e){
//                log.error("Interrupted at task {} ",i);
//            }
//        }


        // 100%
        for (int i = 0; i < 4; i++) {
            executor.execute(() -> {
                while (true) {
                    boolean processed = syncBatchEngineers(start);
                    if (!processed) break;
                    try {
                        Thread.sleep(50); // optional throttling
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean syncBatchEngineers(long start){
        List<EngineerEntity> batch = engineerRepository.fetchBatchForProcessing();
        if (batch.isEmpty()) return false;
        engineerRepository.batchSyncEngineer(batch);
        log.info("Synced {} engineers in {} ms", batch.size(), System.currentTimeMillis() - start);
        return true;
    }

    // 70%
//    @Transactional(rollbackOn = Exception.class)
//    public void syncBatchEngineers(long start){
//        List<EngineerEntity> batch = engineerRepository.fetchBatchForProcessing();
//        if (batch.isEmpty())return;
//        engineerRepository.batchSyncEngineer(batch);
//        log.info("Time task completed : {} ms",System.currentTimeMillis()-start);
//    }
}
