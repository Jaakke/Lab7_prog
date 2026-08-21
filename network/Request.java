package network;

import java.io.Serial;
import java.io.Serializable;

public record Request(
        String commandName,
        String argument,
        Object objectArgument,
        String username,
        String password
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String getCommandName() { return commandName; }
    public String getArgument() { return argument; }
    public Object getObjectArgument() { return objectArgument; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}