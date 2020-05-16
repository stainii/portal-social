package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.repositories.PersonRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@Slf4j
public class SavePersonHelper {

    private final PersonRepository personRepository;

    public SavePersonHelper(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public Person saveAndFlushAndCommit(@NonNull Person person) {
        return personRepository.saveAndFlush(person);
    }

}
