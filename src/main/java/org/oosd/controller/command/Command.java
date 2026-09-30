package org.oosd.controller.command;

/*
Command pattern: a request wrapped up as an object
each command already knows its receiver, so the invoker (KeyBindings)
only ever calls execute() and never needs to know what the key does
functional interface, so a one-off command can also be written as a lambda
*/
@FunctionalInterface
public interface Command {
    void execute();
}
