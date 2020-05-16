package be.stijnhooft.portal.social.repositories;

import be.stijnhooft.portal.social.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByName(String name);

}
