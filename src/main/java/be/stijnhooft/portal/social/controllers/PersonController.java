package be.stijnhooft.portal.social.controllers;

import be.stijnhooft.portal.social.dtos.ContactDto;
import be.stijnhooft.portal.social.dtos.PersonDto;
import be.stijnhooft.portal.social.dtos.Source;
import be.stijnhooft.portal.social.services.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @RequestMapping("/")
    public List<PersonDto> findAll() {
        return personService.findAll();
    }

    @SuppressWarnings("OptionalIsPresent")
    @RequestMapping("/{id}")
    public ResponseEntity<PersonDto> findById(@PathVariable("id") Long id) {
        Optional<PersonDto> person = personService.findById(id);
        if (person.isPresent()) {
            return ResponseEntity.ok(person.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<PersonDto> create(@RequestBody PersonDto person) {
        if (ObjectUtils.isEmpty(person.getNewImageContent())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(personService.create(person));
    }

    @PutMapping("/{id}/")
    public PersonDto update(@RequestBody PersonDto person, @PathVariable( "id") Long id) {
        if (!id.equals(person.getId())) {
            throw new IllegalArgumentException("Updating the id is not allowed");
        }
        return personService.update(person);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        var deleteStatus = personService.delete(id);
        return switch (deleteStatus) {
            case DELETED -> ResponseEntity.ok().build();
            case DOES_NOT_EXIST -> ResponseEntity.notFound().build();
        };
    }

    @PostMapping("/{id}/contact/")
    public ResponseEntity<PersonDto> addContact(@RequestBody ContactDto contact, @PathVariable("id") Long personId) {
        personService.addContact(contact, personId, Source.USER);
        return findById(personId);
    }

}
