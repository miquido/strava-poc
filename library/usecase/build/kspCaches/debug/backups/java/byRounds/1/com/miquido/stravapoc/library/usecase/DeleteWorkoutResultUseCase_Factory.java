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
public final class DeleteWorkoutResultUseCase_Factory implements Factory<DeleteWorkoutResultUseCase> {
  private final Provider<WorkoutResultRepository> repositoryProvider;

  public DeleteWorkoutResultUseCase_Factory(Provider<WorkoutResultRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteWorkoutResultUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteWorkoutResultUseCase_Factory create(
      Provider<WorkoutResultRepository> repositoryProvider) {
    return new DeleteWorkoutResultUseCase_Factory(repositoryProvider);
  }

  public static DeleteWorkoutResultUseCase newInstance(WorkoutResultRepository repository) {
    return new DeleteWorkoutResultUseCase(repository);
  }
}
