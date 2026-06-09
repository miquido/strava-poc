package com.miquido.stravapoc.library.data.repository;

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RouteRepositoryImpl_Factory implements Factory<RouteRepositoryImpl> {
  private final Provider<RouteLocalDataSource> dataSourceProvider;

  public RouteRepositoryImpl_Factory(Provider<RouteLocalDataSource> dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public RouteRepositoryImpl get() {
    return newInstance(dataSourceProvider.get());
  }

  public static RouteRepositoryImpl_Factory create(
      Provider<RouteLocalDataSource> dataSourceProvider) {
    return new RouteRepositoryImpl_Factory(dataSourceProvider);
  }

  public static RouteRepositoryImpl newInstance(RouteLocalDataSource dataSource) {
    return new RouteRepositoryImpl(dataSource);
  }
}
