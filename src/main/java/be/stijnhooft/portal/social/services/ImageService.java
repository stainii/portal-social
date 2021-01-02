package be.stijnhooft.portal.social.services;


import be.stijnhooft.portal.model.image.ImageDto;
import be.stijnhooft.portal.social.dtos.ImageLabel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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

    public void delete(@NonNull Collection<String> imageNames) {
        imageNames.forEach(this::delete);
    }

    public void delete(@NonNull String... imageNames) {
        delete(Arrays.asList(imageNames));
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

    public HashMap<ImageLabel, String> createThumbnails(@NonNull String imageContent) {
        ByteArrayResource image = new ByteArrayResource(decodeBase64Image(imageContent)) {
            @Override
            public String getFilename() {
                return UUID.randomUUID().toString();
            }
        };
        String transformationDefinitions = String.format("[{ \"label\": \"%s\", \"transformations\": [{ \"name\": \"resize\", \"width\": \"300\", \"height\": \"300\", \"crop\": true}] }, { \"label\": \"%s\", \"transformations\": [{ \"name\": \"resize\", \"width\": \"300\", \"height\": \"300\", \"crop\": true}, {\"name\": \"sepia\"}] }]", ImageLabel.COLOR_THUMBNAIL.getValue(), ImageLabel.SEPIA_THUMBNAIL.getValue());

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", image);
        requestBody.add("transformationDefinitions", transformationDefinitions);

        var url = findPortalImageUrl() + API_CONTEXT_ROOT + "transform/";
        log.info("Creating thumbnail at " + url);

        ResponseEntity<List<ImageDto>> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestBody), new ParameterizedTypeReference<>() {
        });

        HashMap<ImageLabel, String> result = new HashMap<>();
        result.put(ImageLabel.COLOR_THUMBNAIL, parseImageUrlFromResponse(response, ImageLabel.COLOR_THUMBNAIL));
        result.put(ImageLabel.SEPIA_THUMBNAIL, parseImageUrlFromResponse(response, ImageLabel.SEPIA_THUMBNAIL));
        return result;
    }

    private String parseImageUrlFromResponse(ResponseEntity<List<ImageDto>> response, ImageLabel imageLabel) {
        return Optional.ofNullable(response.getBody()) // avoiding a NullPointerException. If body is null, orElseThrow will be triggered.
                .stream()
                .flatMap(Collection::stream) // transform Stream<List<ImageDto>> to Stream<ImageDto>
                .filter(imageDto -> imageDto.getLabel().equals(imageLabel.getValue()))
                .findFirst()
                .map(ImageDto::getName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find thumbnail in response of image: %s", response.getBody())));
    }

    public void rollbackCreateImages(Collection<String> images) {
        log.warn("Creating or updating person failed. Reverting creation of images " + images);
        delete(images);
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
