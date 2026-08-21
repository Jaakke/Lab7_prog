package models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final java.util.Date birthday;
    private final Color eyeColor;
    private final Location location;

    public Person(String name, Date birthday, Color eyeColor, Location location) {
        this.name = name;
        this.birthday = birthday;
        this.eyeColor = eyeColor;
        this.location = location;
    }

    public boolean validate() {
        return name != null && !name.trim().isEmpty()
                && birthday != null
                && eyeColor != null
                && (location == null || location.validate());
    }

    public String getName() { return name; }
    public Date getBirthday() { return birthday; }
    public Color getEyeColor() { return eyeColor; }
    public Location getLocation() { return location; }

    @Override
    public String toString() {
        return String.format("Person{name='%s', birthday=%s, eyeColor=%s, location=%s}",
                name, birthday, eyeColor, location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(name, person.name) &&
                Objects.equals(birthday, person.birthday) &&
                eyeColor == person.eyeColor &&
                Objects.equals(location, person.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthday, eyeColor, location);
    }
}