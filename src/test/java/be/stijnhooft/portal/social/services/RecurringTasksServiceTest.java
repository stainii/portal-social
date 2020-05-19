package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.dtos.ExecutionDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import be.stijnhooft.portal.social.dtos.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class RecurringTasksServiceTest {

    @Autowired
    private RecurringTasksService recurringTasksService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    public void mockDiscoverClient() {
        when(discoveryClient.getInstances(RecurringTasksService.SERVICE_ID))
                .thenReturn(List.of(new ServiceInstance() {
                    @Override
                    public String getServiceId() {
                        return null;
                    }

                    @Override
                    public String getHost() {
                        return null;
                    }

                    @Override
                    public int getPort() {
                        return 0;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public URI getUri() {
                        return URI.create("http://localhost:2011");
                    }

                    @Override
                    public Map<String, String> getMetadata() {
                        return null;
                    }
                }));
    }

    @Test
    void findByIdWhenFound() {
        // arrange
        var recurringTaskDto = RecurringTaskDto.builder()
                .id(999L)
                .build();

        when(restTemplate.getForEntity("http://localhost:2011/api/recurring-task/100", RecurringTaskDto.class))
                .thenReturn(ResponseEntity.ok(recurringTaskDto));

        // act
        var result = recurringTasksService.findById(100);

        // assert
        assertTrue(result.isPresent());
        assertEquals(recurringTaskDto, result.get());
    }

    @Test
    void findByIdWhenNotFound() {
        // arrange
        when(restTemplate.getForEntity("http://localhost:2011/api/recurring-task/100", RecurringTaskDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // act
        var result = recurringTasksService.findById(100);

        // assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByIdWhenError() {
        // arrange
        when(restTemplate.getForEntity("http://localhost:2011/api/recurring-task/100", RecurringTaskDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // act, assert
        assertThrows(RuntimeException.class, () -> recurringTasksService.findById(100), "Recurring tasks microservice responded with BAD_REQUEST");
    }

    @Test
    void deleteById() {
        recurringTasksService.deleteById(100);
        verify(restTemplate).delete("http://localhost:2011/api/recurring-task/100/");
    }

    @Test
    void create() {
        // arrange
        var recurringTaskId = 100L;
        var recurringTaskDtoRequest = RecurringTaskDto.builder().build();
        var recurringTaskDtoResponse = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .build();

        when(restTemplate.postForEntity("http://localhost:2011/api/recurring-task/", recurringTaskDtoRequest, RecurringTaskDto.class))
                .thenReturn(ResponseEntity.ok(recurringTaskDtoResponse));

        // act
        var createdRecurringTaskDto = recurringTasksService.create(recurringTaskDtoRequest);

        // assert
        verify(restTemplate).postForEntity("http://localhost:2011/api/recurring-task/", recurringTaskDtoRequest, RecurringTaskDto.class);
        assertEquals(createdRecurringTaskDto, recurringTaskDtoResponse);
    }

    @Test
    void update() {
        // arrange
        var recurringTaskDto = RecurringTaskDto.builder()
                .id(100L)
                .build();

        // act
        recurringTasksService.update(recurringTaskDto);

        // assert
        verify(restTemplate).put("http://localhost:2011/api/recurring-task/100/", recurringTaskDto);
    }

    @Test
    void addExecution() {
        // arrange
        var contactDto = ExecutionDto.builder()
                .date(LocalDate.now())
                .source(Source.USER)
                .build();

        // act
        recurringTasksService.addExecution(contactDto, 100);

        // assert
        verify(restTemplate).postForEntity("http://localhost:2011/api/recurring-task/100/execution/", contactDto, RecurringTaskDto.class);
    }

    @Test
    void rollbackCreateRecurringTask() {
        recurringTasksService.rollbackCreateRecurringTask(100);
        verify(restTemplate).delete("http://localhost:2011/api/recurring-task/100/");
    }
}