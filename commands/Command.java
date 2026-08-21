package commands;

import network.Response;

public interface Command {
    Response execute(network.Request request);
    String getName();
    String getDescription();
}