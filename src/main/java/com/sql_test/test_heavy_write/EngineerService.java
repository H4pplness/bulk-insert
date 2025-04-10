package com.sql_test.test_heavy_write;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private final LocalDate startTime = LocalDate.of(2020,1,1);
    private final LocalDate endTime = LocalDate.of(2020,12,31);

    private final List<String> listTitle = List.of("Backend Engineer","Data Engineer","Data Scientist","Frontend Engineer","QA","Fullstack Engineer","Solution Architect","Principal Engineer","Engineer Manager","BA");

    public void syncEngineer(){
        listFirstname = getRandomListFirstName(5000);
        listLastname = getRandomListLastName(5000);

        List<EngineerEntity> engineerEntityList = new ArrayList<>();
        int lastIdx = (int) engineerRepository.count();
        for (int i=lastIdx;i<lastIdx+1000000;i++){
            EngineerEntity engineerEntity = createRandomEngineers(i);
            engineerEntityList.add(engineerEntity);
        }

        List<List<EngineerEntity>> partitionListEngineer = partitionList(engineerEntityList,1000);

        long start = System.currentTimeMillis();

        for (int i = 0; i < partitionListEngineer.size(); i++) {
            final int finalI = i;
//            Thread.ofVirtual().start(()->{
//                batchInsertEngineer(partitionListEngineer.get(finalI));
//                log.info("Time task {} completed: {} ms", finalI, start-System.currentTimeMillis());
//            });
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


}
