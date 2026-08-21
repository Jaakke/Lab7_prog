package models;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

public class StudyGroup implements Serializable, Comparable<StudyGroup> {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Coordinates coordinates;
    private java.time.ZonedDateTime creationDate;
    private long studentsCount;
    private Integer expelledStudents;
    private FormOfEducation formOfEducation;
    private Semester semesterEnum;
    private Person groupAdmin;
    private String owner;

    public StudyGroup() {
        this.creationDate = ZonedDateTime.now();
    }

    public StudyGroup(String name, Coordinates coordinates, long studentsCount,
                      Integer expelledStudents, FormOfEducation formOfEducation,
                      Semester semesterEnum, Person groupAdmin) {
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = ZonedDateTime.now();
        this.studentsCount = studentsCount;
        this.expelledStudents = expelledStudents;
        this.formOfEducation = formOfEducation;
        this.semesterEnum = semesterEnum;
        this.groupAdmin = groupAdmin;
    }

    public StudyGroup(Long id, String name, Coordinates coordinates,
                      ZonedDateTime creationDate, long studentsCount,
                      Integer expelledStudents, FormOfEducation formOfEducation,
                      Semester semesterEnum, Person groupAdmin) {
        setId(id);
        setName(name);
        setCoordinates(coordinates);
        setCreationDateFromFile(creationDate);
        setStudentsCount(studentsCount);
        setExpelledStudents(expelledStudents);
        setFormOfEducation(formOfEducation);
        setSemesterEnum(semesterEnum);
        setGroupAdmin(groupAdmin);
    }

    public boolean validate() {
        return id != null && id > 0
                && name != null && !name.trim().isEmpty()
                && coordinates != null
                && creationDate != null
                && studentsCount > 0
                && (expelledStudents == null || expelledStudents > 0)
                && formOfEducation != null
                && groupAdmin != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть больше 0");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Имя не может быть null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть null");
        }
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDateFromFile(ZonedDateTime creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Дата создания не может быть null");
        }
        this.creationDate = creationDate;
    }

    public long getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(long studentsCount) {
        if (studentsCount <= 0) {
            throw new IllegalArgumentException("Количество студентов должно быть больше 0");
        }
        this.studentsCount = studentsCount;
    }

    public Integer getExpelledStudents() {
        return expelledStudents;
    }

    public void setExpelledStudents(Integer expelledStudents) {
        if (expelledStudents != null && expelledStudents <= 0) {
            throw new IllegalArgumentException("Количество исключенных студентов должно быть больше 0");
        }
        this.expelledStudents = expelledStudents;
    }

    public FormOfEducation getFormOfEducation() {
        return formOfEducation;
    }

    public void setFormOfEducation(FormOfEducation formOfEducation) {
        if (formOfEducation == null) {
            throw new IllegalArgumentException("Форма обучения не может быть null");
        }
        this.formOfEducation = formOfEducation;
    }

    public Semester getSemesterEnum() {
        return semesterEnum;
    }

    public void setSemesterEnum(Semester semesterEnum) {
        this.semesterEnum = semesterEnum;
    }

    public Person getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(Person groupAdmin) {
        if (groupAdmin == null) {
            throw new IllegalArgumentException("Администратор группы не может быть null");
        }
        this.groupAdmin = groupAdmin;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int compareTo(StudyGroup o) {
        if (o == null) throw new NullPointerException("Сравниваемый объект не может быть null");
        return Long.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StudyGroup that = (StudyGroup) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("StudyGroup{id=%d, name='%s', coordinates=%s, creationDate=%s, " +
                        "studentsCount=%d, expelledStudents=%d, formOfEducation=%s, " +
                        "semesterEnum=%s, groupAdmin=%s}",
                id, name, coordinates, creationDate, studentsCount,
                expelledStudents, formOfEducation, semesterEnum, groupAdmin);
    }
}