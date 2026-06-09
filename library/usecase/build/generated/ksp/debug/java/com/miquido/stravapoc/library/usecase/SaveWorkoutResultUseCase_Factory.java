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
public final class SaveWorkoutResultUseCase_Factory implements Factory<SaveWorkoutResultUseCase> {
  private final Provider<WorkoutResultRepository> repositoryProvider;

  public SaveWorkoutResultUseCase_Factory(Provider<WorkoutResultRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SaveWorkoutResultUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static SaveWorkoutResultUseCase_Factory create(
      Provider<WorkoutResultRepository> repositoryProvider) {
    return new SaveWorkoutResultUseCase_Factory(repositoryProvider);
  }

  public static SaveWorkoutResultUseCase newInstance(WorkoutResultRepository repository) {
    return new SaveWorkoutResultUseCase(repository);
  }
}
