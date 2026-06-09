package com.miquido.stravapoc.library.data.repository;

import com.miquido.stravapoc.library.data.db.dao.WorkoutResultDao;
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
public final class WorkoutResultRepositoryImpl_Factory implements Factory<WorkoutResultRepositoryImpl> {
  private final Provider<WorkoutResultDao> daoProvider;

  public WorkoutResultRepositoryImpl_Factory(Provider<WorkoutResultDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public WorkoutResultRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static WorkoutResultRepositoryImpl_Factory create(Provider<WorkoutResultDao> daoProvider) {
    return new WorkoutResultRepositoryImpl_Factory(daoProvider);
  }

  public static WorkoutResultRepositoryImpl newInstance(WorkoutResultDao dao) {
    return new WorkoutResultRepositoryImpl(dao);
  }
}
