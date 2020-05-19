package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.dtos.ExecutionDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for all communication with the recurring tasks module.
 */
@Service
@Slf4j
public class RecurringTasksService {

    public static final String API_CONTEXT_ROOT = "api/recurring-task/";
    public static final String SERVICE_ID = "social-recurring-tasks";
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public RecurringTasksService(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public Optional<RecurringTaskDto> findById(long recurringTaskId) {
        try {
            var url = findRecurringTasksUri() + API_CONTEXT_ROOT + recurringTaskId;
            log.info("Retrieving recurring task at " + url);
            var response = restTemplate.getForEntity(url, RecurringTaskDto.class);
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Recurring tasks microservice responded with " + e.getStatusText(), e);
            }
        }
    }

    public void deleteById(long id) {
        restTemplate.delete(findRecurringTasksUri() + API_CONTEXT_ROOT + id + "/");
    }

    /**
     * Asks the recurring tasks microservice to create a returning task.
     *
     * @param recurringTaskDto dto
     * @return id of the created recurring task
     */
    public RecurringTaskDto create(RecurringTaskDto recurringTaskDto) {
        var url = findRecurringTasksUri() + API_CONTEXT_ROOT;
        log.info("Creating recurring task at " + url);

        var result = restTemplate.postForEntity(url, recurringTaskDto, RecurringTaskDto.class);
        return result.getBody();
    }

    public void update(RecurringTaskDto recurringTaskDto) {
        var url = findRecurringTasksUri() + API_CONTEXT_ROOT + recurringTaskDto.getId() + "/";
        log.info("Updating recurring task at " + url);

        restTemplate.put(url, recurringTaskDto);
    }

    public void addExecution(ExecutionDto execution, long recurringTaskId) {
        var url = findRecurringTasksUri() + API_CONTEXT_ROOT + recurringTaskId + "/execution/";
        log.info("Adding execution for recurring task at " + url);

        restTemplate.postForEntity(url, execution, RecurringTaskDto.class);
    }

    /**
     * Use this method to rollback a creation of a user, when other actions (like persisting the person's image) go wrong
     * @param recurringTaskId id of the recurring task that should be rolled back
     */
    public void rollbackCreateRecurringTask(long recurringTaskId) {
        log.warn("Creating person failed. Reverting creation of recurring task " + recurringTaskId);
        deleteById(recurringTaskId);
    }

    public void rollbackUpdateRecurringTask(RecurringTaskDto originalRecurringTaskDto) {
        log.warn("Updating person failed. Reverting update of recurring task " + originalRecurringTaskDto.getId());
        update(originalRecurringTaskDto);
    }

    private String findRecurringTasksUri() {
        List<ServiceInstance> portalImageInstances = discoveryClient.getInstances(SERVICE_ID);
        if (portalImageInstances != null && !portalImageInstances.isEmpty()) {
            return portalImageInstances.get(0).getUri().toString() + "/";
        } else {
            throw new IllegalStateException("No instance of portal-recurring-tasks with name SOCIAL-RECURRING-TASKS registered with Eureka");
        }
    }
}
