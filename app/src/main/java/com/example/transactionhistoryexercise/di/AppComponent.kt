package com.example.transactionhistoryexercise.di

import android.app.Application
import com.example.transactionhistoryexercise.MainApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    BuildersModule::class,
    ViewModelModule::class])
interface AppComponent: AndroidInjector<MainApplication> {

    @Component.Builder
    interface Bulider{
        @BindsInstance
        fun application(application: Application): Bulider
        fun build(): AppComponent
    }
}