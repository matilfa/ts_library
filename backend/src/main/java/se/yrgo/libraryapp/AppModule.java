package se.yrgo.libraryapp;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PasswordEncoder.class).to(Argon2PasswordEncoder.class);
    }
}
