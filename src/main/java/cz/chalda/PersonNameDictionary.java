package cz.chalda;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Simple test entity with compound primary key
 * that contains name of a person.
 */
@Entity
@Table(name = "PERSON_DICTIONARY")
public class PersonNameDictionary {

    @EmbeddedId PersonNameDictionaryId id;

    PersonNameDictionary(String firstName, String surname) {
        this.id = new PersonNameDictionaryId()
            .setFirstName(firstName)
            .setSurname(surname);
    }

    @Override
    public String toString() {
        return String.format("name: %s, surname: %s",
            this.id.getFirstName(), this.id.getSurname());
    }
}

@Embeddable
class PersonNameDictionaryId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String surname;

    PersonNameDictionaryId setSurname(String surname) {
        this.surname = surname;
        return this;
    }
    PersonNameDictionaryId setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getSurname() {
        return surname;
    }
}
