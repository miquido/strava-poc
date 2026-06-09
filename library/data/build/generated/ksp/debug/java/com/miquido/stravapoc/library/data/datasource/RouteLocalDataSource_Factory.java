package com.miquido.stravapoc.library.data.datasource;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class RouteLocalDataSource_Factory implements Factory<RouteLocalDataSource> {
  @Override
  public RouteLocalDataSource get() {
    return newInstance();
  }

  public static RouteLocalDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RouteLocalDataSource newInstance() {
    return new RouteLocalDataSource();
  }

  private static final class InstanceHolder {
    private static final RouteLocalDataSource_Factory INSTANCE = new RouteLocalDataSource_Factory();
  }
}
