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
public final class GetWorkoutResultByIdUseCase_Factory implements Factory<GetWorkoutResultByIdUseCase> {
  private final Provider<WorkoutResultRepository> repositoryProvider;

  public GetWorkoutResultByIdUseCase_Factory(Provider<WorkoutResultRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetWorkoutResultByIdUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetWorkoutResultByIdUseCase_Factory create(
      Provider<WorkoutResultRepository> repositoryProvider) {
    return new GetWorkoutResultByIdUseCase_Factory(repositoryProvider);
  }

  public static GetWorkoutResultByIdUseCase newInstance(WorkoutResultRepository repository) {
    return new GetWorkoutResultByIdUseCase(repository);
  }
}
