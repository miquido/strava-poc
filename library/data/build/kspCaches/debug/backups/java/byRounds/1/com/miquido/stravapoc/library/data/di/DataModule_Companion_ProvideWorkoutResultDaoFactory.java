package com.miquido.stravapoc.library.data.di;

import com.miquido.stravapoc.library.data.db.AppDatabase;
import com.miquido.stravapoc.library.data.db.dao.WorkoutResultDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_Companion_ProvideWorkoutResultDaoFactory implements Factory<WorkoutResultDao> {
  private final Provider<AppDatabase> dbProvider;

  public DataModule_Companion_ProvideWorkoutResultDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public WorkoutResultDao get() {
    return provideWorkoutResultDao(dbProvider.get());
  }

  public static DataModule_Companion_ProvideWorkoutResultDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DataModule_Companion_ProvideWorkoutResultDaoFactory(dbProvider);
  }

  public static WorkoutResultDao provideWorkoutResultDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DataModule.Companion.provideWorkoutResultDao(db));
  }
}
