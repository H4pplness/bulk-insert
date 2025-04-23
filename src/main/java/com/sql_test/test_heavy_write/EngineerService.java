package com.sql_test.test_heavy_write;

import com.github.javafaker.Faker;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public void insertEngineer(int numberOfEngineer) {
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

    public void batchInsertEngineer(List<EngineerEntity> listEngineer){
        System.out.println("Running in thread: " + Thread.currentThread());
        engineerRepository.batchInsertEngineer(listEngineer);
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


    public void syncEngineer(Integer strategy) {
        switch (strategy){
            case 1:
                syncSingleThread();
                break;

            case 2:
                syncSimpleMultiThread();
                break;

            case 3:
                syncMultiThreadByRedundant();
                break;

            case 4:
                syncMultiThreadByRange();
                break;

            case 5:
                syncMultiThreadByCursor();
                break;

            default: log.error("Not found !");
        }

//        ExecutorService executor = Executors.newFixedThreadPool(4);
//        long start = System.currentTimeMillis();
//
//        // 70%
//        long countEngineer = 1000000;
//        int batchSize = 1000;
//
//        for (int i = 4000; i < 5000; i++) {
//            int finalI = i;
//            executor.execute(()-> syncCursorBatch(start,finalI*batchSize));
////            executor.execute(()->syncBatchEngineers(start));
////            try {
////                Thread.sleep(50);
////            }catch (InterruptedException e){
////                log.error("Interrupted at task {} ",i);
////            }
//        }
//
//
//        // 100%
////        for (int i = 0; i < 4; i++) {
////            executor.execute(() -> {
////                while (true) {
////                    boolean processed = syncBatchEngineers(start);
////                    if (!processed) break;
////                    try {
////                        Thread.sleep(50); // optional throttling
////                    } catch (InterruptedException e) {
////                        Thread.currentThread().interrupt();
////                        break;
////                    }
////                }
////            });
////        }
//
//        executor.shutdown();
    }

    public void syncSingleThread(){
        long start = System.currentTimeMillis();
        for (int i=0;i<1000;i++){
            syncRandomBatch(start);
        }
    }

    public void syncSimpleMultiThread(){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        for (int i=0;i<1000;i++){
            executor.execute(()->syncRandomBatch(start));
        }


        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Timed out waiting for batch sync tasks");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void syncMultiThreadByRedundant(){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        for (int i=0;i<1000;i++){
            int finalI = i;
            executor.execute(()->syncRedundantBatch(start,finalI));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Timed out waiting for batch sync tasks");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void syncMultiThreadByRange(){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        long start = System.currentTimeMillis();
        for (int i=0;i<5000;i++){
            int finalI = i;
            executor.execute(()->syncRangeBatch(start,finalI*1000+1,finalI*1000+1000));;
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Timed out waiting for batch sync tasks");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void syncMultiThreadByCursor(){

    }


    @Transactional(rollbackOn = Exception.class)
    public void syncRandomBatch(long start){
        List<EngineerEntity> batch = engineerRepository.fetchSimpleBatch();
        if (batch.isEmpty()) return;
        engineerRepository.batchSyncEngineer(batch);
        log.info("Time task completed : {} ms",System.currentTimeMillis()-start);
    }


    @Transactional(rollbackOn = Exception.class)
    public void syncRedundantBatch(long start,int task){
        List<EngineerEntity> batch = engineerRepository.fetchRedundantBatch(task);
        if (batch.isEmpty())return;
        engineerRepository.batchSyncEngineer(batch);
        log.info("Time task completed : {} ms",System.currentTimeMillis()-start);
    }

    @Transactional(rollbackOn = Exception.class)
    public void syncCursorBatch(long start, Integer cursor){
        List<EngineerEntity> batch = engineerRepository.cursorFetchBatch(cursor,1000);
        if (batch.isEmpty())return;
        engineerRepository.batchSyncEngineer(batch);
        log.info("Time task completed : {} ms",System.currentTimeMillis()-start);
    }

    @Transactional(rollbackOn = Exception.class)
    public void syncRangeBatch(long start, Integer firstIdx, Integer lastIdx){
        List<EngineerEntity> batch = engineerRepository.fetchBatchInARange(firstIdx,lastIdx,lastIdx-firstIdx+1);
        if (batch.isEmpty())return;
        engineerRepository.batchSyncEngineer(batch);
        log.info("Time task completed : {} ms",System.currentTimeMillis()-start);
    }

}
