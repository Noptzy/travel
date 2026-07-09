package com.makeyourjurney.application.actor;

public interface Actor<I, O> {
    O run(I input);
}
