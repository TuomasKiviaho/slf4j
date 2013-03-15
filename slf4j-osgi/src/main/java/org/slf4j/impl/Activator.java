package org.slf4j.impl;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggerFactoryBinder;

public class Activator implements BundleActivator, ManagedService {

  private LoggerFactoryImpl loggerFactory;

  public Activator() {
    LoggerFactoryBinder loggerFactoryBinder = StaticLoggerBinder.getSingleton();
    loggerFactory = (LoggerFactoryImpl) loggerFactoryBinder.getLoggerFactory();
  }

  private void setLogLevel(Object logLevel) throws ConfigurationException {
    try {
      loggerFactory.logLevel = logLevel == null ? null
          : Integer
              .valueOf(LocationAwareLogger.ERROR_INT
                  - ((logLevel instanceof String ? Integer
                      .valueOf((String) logLevel) : (Integer) logLevel)
                      .intValue() - 1) * 10);
    } catch (RuntimeException e) {
      throw new ConfigurationException(LogService.class.getPackage().getName(),
          e.getMessage(), e);
    }
  }

  public void start(BundleContext bundleContext) throws Exception {
    ServiceTracker logServiceTracker = new ServiceTracker(bundleContext,
        LogService.class.getName(), null);
    logServiceTracker.open();
    Object logLevel = bundleContext.getProperty(LogService.class.getPackage()
        .getName());
    synchronized (loggerFactory) {
      loggerFactory.bundleContext = bundleContext;
      loggerFactory.logServiceTracker = logServiceTracker;
      setLogLevel(logLevel == null ? Integer.valueOf(3) : logLevel);
      for (Iterator iterator = loggerFactory.loggers.entrySet().iterator(); iterator
          .hasNext();) {
        Map.Entry entry = (Map.Entry) iterator.next();
        LoggerImpl logger = (LoggerImpl) entry.getValue();
        logger.logLevel = loggerFactory.logLevel;
        String className = (String) entry.getKey();
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext,
            className, null);
        serviceTracker.open();
        logger.serviceTracker = serviceTracker;
        logger.logServiceTracker = logServiceTracker;
      }
    }
    Properties properties = new Properties();
    Bundle bundle = bundleContext.getBundle();
    String symbolicName = bundle.getSymbolicName();
    properties.put(Constants.SERVICE_PID, symbolicName);
    bundleContext.registerService(ManagedService.class.getName(), this, null);
  }

  public void updated(Dictionary properties) throws ConfigurationException {
    Object logLevel = properties.get(LogService.class.getPackage().getName());
    synchronized (loggerFactory) {
      setLogLevel(logLevel);
      for (Iterator iterator = loggerFactory.loggers.values().iterator(); iterator
          .hasNext();) {
        LoggerImpl logger = (LoggerImpl) iterator.next();
        logger.logLevel = loggerFactory.logLevel;
      }
    }
  }

  public void stop(BundleContext bundleContext) throws Exception {
    synchronized (loggerFactory) {
      loggerFactory.logServiceTracker.close();
      loggerFactory.logServiceTracker = null;
      for (Iterator iterator = loggerFactory.loggers.values().iterator(); iterator
          .hasNext();) {
        LoggerImpl logger = (LoggerImpl) iterator.next();
        logger.logServiceTracker = null;
        if (logger.serviceTracker != null) {
          logger.serviceTracker.close();
          logger.serviceTracker = null;
        }
        logger.logLevel = null;
      }
      loggerFactory.logLevel = null;
      loggerFactory.bundleContext = null;
    }
  }

}
