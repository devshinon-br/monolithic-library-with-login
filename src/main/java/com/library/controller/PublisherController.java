package com.library.controller;

import com.library.exception.EntityNotFoundException;
import com.library.model.Publisher;
import com.library.model.dto.request.PublisherRequest;
import com.library.model.dto.response.PublisherResponse;
import com.library.service.PublisherService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;
    private final ModelMapper modelMapper;

    @Autowired
    public PublisherController(final PublisherService publisherService, final ModelMapper modelMapper) {
        this.publisherService = publisherService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "application/yaml"})
    public List<PublisherResponse> getAllPublishers() {
        List<Publisher> publishers = publisherService.getAllPublishers();
        return publishers.stream()
            .map(publisher -> modelMapper.map(publisher, PublisherResponse.class))
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PublisherResponse>> getPublisherById(@PathVariable final Long id) {
        final Publisher publisher = publisherService.getPublisherById(id);

        if (publisher == null) {
            throw new EntityNotFoundException("Publisher with ID " + id + " not found");
        }

        final PublisherResponse publisherResponse = modelMapper.map(publisher, PublisherResponse.class);
        final EntityModel<PublisherResponse> resource = EntityModel.of(publisherResponse);

        final Link selfLink = Link.of(WebMvcLinkBuilder.linkTo(PublisherController.class).slash(id).withSelfRel().getHref());
        resource.add(selfLink);

        final Link publishersLink = Link.of(WebMvcLinkBuilder.linkTo(PublisherController.class).withRel("publishers").getHref());
        resource.add(publishersLink);

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    public ResponseEntity<PublisherResponse> savePublisher(@RequestBody final PublisherRequest publisherRequest) {
        final Publisher publisher = modelMapper.map(publisherRequest, Publisher.class);
        final Publisher savedPublisher = publisherService.savePublisher(publisher);
        PublisherResponse publisherResponse = modelMapper.map(savedPublisher, PublisherResponse.class);
        return new ResponseEntity<>(publisherResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherResponse> updatePublisher(@PathVariable final Long id, @RequestBody final PublisherRequest publisherRequest) {
        final Publisher existingPublisher = publisherService.getPublisherById(id);

        if (existingPublisher == null) {
            throw new EntityNotFoundException("Publisher with ID " + id + " not found");
        }

        modelMapper.map(publisherRequest, existingPublisher);
        final Publisher savedPublisher = publisherService.savePublisher(existingPublisher);
        PublisherResponse publisherResponse = modelMapper.map(savedPublisher, PublisherResponse.class);
        return ResponseEntity.ok(publisherResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable final Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}


