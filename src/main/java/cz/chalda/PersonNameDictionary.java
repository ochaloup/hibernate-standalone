package cz.chalda;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Simple test entity with compound primary key
 * that contains name of a person.
 */
@Entity
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

    // id getter and setter is needed when hibernate.hbm.xml is used
    private PersonNameDictionaryId getId() {
		return id;
	}
	private void setId(PersonNameDictionaryId id) {
		this.id = id;
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
