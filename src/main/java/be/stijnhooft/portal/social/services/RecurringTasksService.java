package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.dtos.ExecutionDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Service responsible for all communication with the recurring tasks module.
 */
@Service
@Slf4j
public class RecurringTasksService {

    public static final String API_CONTEXT_ROOT = "api/recurring-task/";
    private final RestTemplate restTemplate;
    private final String recurringTasksUri;

    public RecurringTasksService(RestTemplate restTemplate, @Value("${recurring-tasks.uri}") String recurringTasksUri) {
        this.restTemplate = restTemplate;
        this.recurringTasksUri = recurringTasksUri;
    }

    public Optional<RecurringTaskDto> findById(long recurringTaskId) {
        try {
            var response = restTemplate.getForEntity(recurringTasksUri + API_CONTEXT_ROOT + recurringTaskId, RecurringTaskDto.class);
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
        restTemplate.delete(recurringTasksUri + API_CONTEXT_ROOT + id + "/");
    }

    /**
     * Asks the recurring tasks microservice to create a returning task.
     *
     * @param recurringTaskDto
     * @return id of the created recurring task
     */
    public RecurringTaskDto create(RecurringTaskDto recurringTaskDto) {
        var result = restTemplate.postForEntity(recurringTasksUri + API_CONTEXT_ROOT, recurringTaskDto, RecurringTaskDto.class);
        return result.getBody();
    }

    public void update(RecurringTaskDto recurringTaskDto) {
        restTemplate.put(recurringTasksUri + API_CONTEXT_ROOT + recurringTaskDto.getId() + "/", recurringTaskDto);
    }

    public void addExecution(ExecutionDto execution, long recurringTaskId) {
        restTemplate.postForEntity(recurringTasksUri + API_CONTEXT_ROOT + recurringTaskId + "/execution/", execution, RecurringTaskDto.class);
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
}
