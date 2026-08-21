package models;

import java.io.Serializable;
import java.util.Objects;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final Float y;

    public Coordinates(int x, Float y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public Float getY() { return y; }

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return x == that.x && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}