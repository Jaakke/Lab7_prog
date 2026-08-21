package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long x;
    private final Long y;
    private final Float z;
    private final String name;

    public Location(Long x, Long y, Float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public boolean validate() {
        return x != null && y != null && z != null && (name == null || !name.trim().isEmpty());
    }

    public Long getX() { return x; }
    public Long getY() { return y; }
    public Float getZ() { return z; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name == null ?
                String.format("Location{x=%d, y=%d, z=%f}", x, y, z) :
                String.format("Location{name='%s', x=%d, y=%d, z=%f}", name, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(x, location.x) &&
                Objects.equals(y, location.y) &&
                Objects.equals(z, location.z) &&
                Objects.equals(name, location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, name);
    }
}