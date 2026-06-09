package com.miquido.stravapoc.library.usecase;

import com.miquido.stravapoc.library.data.repository.WorkoutResultRepository;
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
public final class GetWorkoutHistoryUseCase_Factory implements Factory<GetWorkoutHistoryUseCase> {
  private final Provider<WorkoutResultRepository> repositoryProvider;

  public GetWorkoutHistoryUseCase_Factory(Provider<WorkoutResultRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetWorkoutHistoryUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetWorkoutHistoryUseCase_Factory create(
      Provider<WorkoutResultRepository> repositoryProvider) {
    return new GetWorkoutHistoryUseCase_Factory(repositoryProvider);
  }

  public static GetWorkoutHistoryUseCase newInstance(WorkoutResultRepository repository) {
    return new GetWorkoutHistoryUseCase(repository);
  }
}
