package org.oosd.ui;

import javafx.scene.Parent;

/*
 abstract class rather than another interface because it holds
 state (the navigator reference and the root node) and provides an
 implementation subclasses inherit rather than repeat
*/
public abstract class BaseScreen implements Screen {

    protected final Navigator navigator;
    private Parent root;

    protected BaseScreen(Navigator navigator) {
        this.navigator = navigator;
    }

    //builds this screens node tree, called at most once per instance
    protected abstract Parent buildRoot();

    @Override
    public final Parent getRoot() {
        if (root == null) {
            root = buildRoot();
        }
        return root;
    }
}
