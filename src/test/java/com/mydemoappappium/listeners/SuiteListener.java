package com.mydemoappappium.listeners;

import com.mydemoappappium.driver.AppiumServerManager;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {

    private final AppiumServerManager server = new AppiumServerManager();

    @Override
    public void onStart(ISuite suite) {
        suite.setAttribute(ArtifactCollector.SERVER_LOG_ATTRIBUTE, server.start()
                .map(Object::toString)
                .orElse("external server reused - see its own log (e.g. target/artifacts/manual/ for scripts/start-appium.sh)"));
    }

    @Override
    public void onFinish(ISuite suite) {
        server.stop();
    }
}
