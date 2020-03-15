// IPackageDataObserver.aidl
package com.example.pluginlib;

// Declare any non-default types here with import statements

interface IPackageDataObserver {
    void onRemoveCompleted(in String packageName, boolean succeeded);
}
