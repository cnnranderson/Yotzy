package com.cnnranderson.yotzy.injection;

import android.util.SparseArray;

import java.util.LinkedList;

public class ScopeComponentStack<Scope, Component> {

    private final SparseArray<Component> mActivityComponents = new SparseArray<>();
    private final LinkedList<Integer> mScopeIdStack = new LinkedList<>();

    public Component getTop() {
        if (!mScopeIdStack.isEmpty()) {
            Integer id = mScopeIdStack.getFirst();
            return mActivityComponents.get(id);
        }
        return null;
    }

    public Component getComponent(Scope scopeInstance) {
        Integer id = System.identityHashCode(scopeInstance);
        setScopeTop(scopeInstance);
        return mActivityComponents.get(id);
    }

    public Component createComponentForScope(Scope scopeInstance, ComponentFactory<Component> factory) {
        Integer id = System.identityHashCode(scopeInstance);
        setScopeTop(scopeInstance);

        Component component = getComponent(scopeInstance);
        if (component == null) {
            component = factory.createComponent();
            mActivityComponents.put(id, component);
        }
        return component;
    }

    public void releaseComponent(Scope scopeInstance) {
        Integer id = System.identityHashCode(scopeInstance);
        mActivityComponents.remove(id);
        mScopeIdStack.remove(id);
    }

    private void setScopeTop(Scope scopeInstance) {
        Integer id = System.identityHashCode(scopeInstance);
        mScopeIdStack.remove(id);
        mScopeIdStack.addFirst(id);
    }

    public interface ComponentFactory<T> {
        T createComponent();
    }
}

