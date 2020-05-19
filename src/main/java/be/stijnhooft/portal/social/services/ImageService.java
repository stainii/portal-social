package be.stijnhooft.portal.social.services;


import be.stijnhooft.portal.social.dtos.ImageDto;
import be.stijnhooft.portal.social.dtos.ImageLabel;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;


/**
 * Service responsible for all communication with the recurring tasks module.
 */
@Service
@Slf4j
public class ImageService {

    public static final String SERVICE_ID = "image";
    public static final String API_CONTEXT_ROOT = "api/";
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public ImageService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    public void delete(@NonNull String imageName) {
        try {
            var url = findPortalImageUrl() + API_CONTEXT_ROOT + "remove/" + imageName;
            log.info("Deleting image at " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException.NotFound ex)   {
            log.warn("Image microservice returns 404 when deleting image {}. Image might already have been deleted.", imageName);
        }
    }

    public String createThumbnail(@NonNull String imageContent) {
        ByteArrayResource image = new ByteArrayResource(decodeBase64Image(imageContent)) {
            @Override
            public String getFilename() {
                return UUID.randomUUID().toString();
            }
        };
        String transformationDefinitions = String.format("[{ \"label\": \"%s\", \"transformations\": [{ \"name\": \"resize\", \"width\": \"300\", \"height\": \"300\", \"crop\": true}] }]", ImageLabel.THUMBNAIL.getValue());

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", image);
        requestBody.add("transformationDefinitions", transformationDefinitions);

        var url = findPortalImageUrl() + API_CONTEXT_ROOT + "transform/";
        log.info("Creating thumbnail at " + url);

        ResponseEntity<List<ImageDto>> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestBody), new ParameterizedTypeReference<>() {
        });

        return Optional.ofNullable(response.getBody()) // avoiding a NullPointerException. If body is null, orElseThrow will be triggered.
                .stream()
                .flatMap(Collection::stream) // transform Stream<List<ImageDto>> to Stream<ImageDto>
                .filter(imageDto -> imageDto.getLabel().equals(ImageLabel.THUMBNAIL.getValue()))
                .findFirst()
                .map(ImageDto::getName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find thumbnail in response of image: %s", response.getBody())));
    }

    public void rollbackCreateImage(String imageName) {
        log.warn("Creating or updating person failed. Reverting creation of image " + imageName);
        delete(imageName);
    }

    private byte[] decodeBase64Image(@NonNull String imageContent) {
        var beginIndex = imageContent.indexOf(",");
        var data = imageContent.substring(beginIndex + 1);
        return Base64.getDecoder().decode(data);
    }

    private String findPortalImageUrl() {
        List<ServiceInstance> portalImageInstances = discoveryClient.getInstances(SERVICE_ID);
        if (portalImageInstances != null && !portalImageInstances.isEmpty()) {
            return portalImageInstances.get(0).getUri().toString() + "/";
        } else {
            throw new IllegalStateException("No instance of portal-image registered with Eureka");
        }
    }

}
