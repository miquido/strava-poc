package com.miquido.stravapoc.library.data.di;

import android.content.Context;
import com.miquido.stravapoc.library.data.db.AppDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataModule_Companion_ProvideAppDatabaseFactory implements Factory<AppDatabase> {
  private final Provider<Context> contextProvider;

  public DataModule_Companion_ProvideAppDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppDatabase get() {
    return provideAppDatabase(contextProvider.get());
  }

  public static DataModule_Companion_ProvideAppDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DataModule_Companion_ProvideAppDatabaseFactory(contextProvider);
  }

  public static AppDatabase provideAppDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DataModule.Companion.provideAppDatabase(context));
  }
}
