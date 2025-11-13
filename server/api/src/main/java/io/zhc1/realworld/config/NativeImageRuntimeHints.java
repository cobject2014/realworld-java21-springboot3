package io.zhc1.realworld.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.sql.Statement;

@Configuration
@ImportRuntimeHints(NativeImageRuntimeHints.Hints.class)
public class NativeImageRuntimeHints {

    static class Hints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
            // Register java.sql.Statement array for reflection and unsafe allocation
            hints.reflection()
                    .registerType(Statement[].class, builder -> 
                        builder.withMembers(
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS,
                            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
                        )
                    );
            
            // Register HikariCP ConcurrentBag$IConcurrentBagEntry array for reflection and unsafe allocation
            // Use TypeReference to register the array type
            hints.reflection()
                    .registerType(
                        TypeReference.of("com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry[]"),
                        builder -> builder.withMembers(
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS,
                            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
                        )
                    );
            
            // Register Hibernate EntityManagerMessageLogger for JBoss logging
            hints.reflection()
                    .registerType(
                        TypeReference.of("org.hibernate.internal.EntityManagerMessageLogger"),
                        builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS
                        )
                    );
            
            // Register the implementation class  
            hints.reflection()
                    .registerType(
                        TypeReference.of("org.hibernate.internal.EntityManagerMessageLogger_$logger"),
                        builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS
                        )
                    );
            
            // Register Hibernate CoreMessageLogger for JBoss logging
            hints.reflection()
                    .registerType(
                        TypeReference.of("org.hibernate.internal.CoreMessageLogger"),
                        builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS
                        )
                    );
            
            // Register the implementation class
            hints.reflection()
                    .registerType(
                        TypeReference.of("org.hibernate.internal.CoreMessageLogger_$logger"),
                        builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS
                        )
                    );
            
            // Register Hibernate strategy classes for SPI instantiation
            registerHibernateStrategyClass(hints, "org.hibernate.boot.model.relational.ColumnOrderingStrategyStandard");
            registerHibernateStrategyClass(hints, "org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorBuilderImpl");
            registerHibernateStrategyClass(hints, "org.hibernate.id.enhanced.SequenceStyleGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.IdentityGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.Assigned");
            registerHibernateStrategyClass(hints, "org.hibernate.id.IncrementGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.SelectGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.UUIDGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.GUIDGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.UUIDHexGenerator");
            registerHibernateStrategyClass(hints, "org.hibernate.id.enhanced.TableGenerator");
            
            // Register Hibernate DTD and XSD schema files as resources
            hints.resources()
                    .registerPattern("org/hibernate/*.dtd")
                    .registerPattern("org/hibernate/*.xsd")
                    .registerPattern("*.pub")
                    .registerPattern("*.key");
        }
        
        private void registerHibernateStrategyClass(@NonNull RuntimeHints hints, @NonNull String className) {
            hints.reflection()
                    .registerType(
                        TypeReference.of(className),
                        builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.PUBLIC_FIELDS,
                            MemberCategory.DECLARED_FIELDS
                        )
                    );
        }
    }
}
