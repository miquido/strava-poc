package com.miquido.stravapoc.library.usecase;

import com.miquido.stravapoc.library.data.repository.RouteRepository;
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
public final class GetRoutesUseCase_Factory implements Factory<GetRoutesUseCase> {
  private final Provider<RouteRepository> repositoryProvider;

  public GetRoutesUseCase_Factory(Provider<RouteRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetRoutesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetRoutesUseCase_Factory create(Provider<RouteRepository> repositoryProvider) {
    return new GetRoutesUseCase_Factory(repositoryProvider);
  }

  public static GetRoutesUseCase newInstance(RouteRepository repository) {
    return new GetRoutesUseCase(repository);
  }
}
