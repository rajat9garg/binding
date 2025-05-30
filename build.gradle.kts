plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.5.0"
    id("nu.studer.jooq") version "7.1"
    id("org.flywaydb.flyway") version "9.16.1"
}

group = "com.biding"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

// Database configuration from gradle.properties
val dbUrl: String by project
val dbUser: String by project
val dbPassword: String by project

// Flyway Configuration
flyway {
    url = "jdbc:postgresql://localhost:5432/biding"
    user = "postgres"
    password = "postgres"
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
    cleanDisabled = false
    driver = "org.postgresql.Driver"
    loggers = arrayOf("slf4j")
}



// JOOQ Configuration
jooq {
    version.set("3.19.3")
    edition = nu.studer.gradle.jooq.JooqEdition.OSS
    
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/biding"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                        includes = ".*"
                        recordVersionFields = "version"
                        recordTimestampFields = "created_at|updated_at"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.biding.infrastructure.jooq"
                        directory = "build/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

// OpenAPI Generator Configuration
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("com.biding.generated.api")
    modelPackage.set("com.biding.generated.model")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useBeanValidation" to "true",
            "documentationProvider" to "none",
            "serializationLibrary" to "jackson",
            "useTags" to "true",
            "enumPropertyNaming" to "UPPERCASE"
        )
    )
    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "",
            "modelDocs" to "false"
        )
    )
}

// Source sets for generated code
sourceSets {
    main {
        java {
            srcDirs(
                "${buildDir}/generated/openapi/src/main/kotlin",
                "${buildDir}/generated/jooq"
            )
        }
    }
}

// Ensure OpenAPI generation happens before compilation
tasks.compileKotlin {
    dependsOn("openApiGenerate")
}

// Ensure code generation tasks run before compilation
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
    dependsOn("generateJooq")
}

tasks.named("generateJooq") {
    dependsOn("flywayMigrate")
}
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.data:spring-data-commons")
    
    // JOOQ
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:3.19.3")
    
    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core")
    
    // JOOQ Generator
    jooqGenerator("org.postgresql:postgresql:42.6.0")
    
    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.webjars:sockjs-client:1.5.1")
    implementation("org.webjars:stomp-websocket:2.3.4")

	jooqGenerator("org.jooq:jooq-codegen:3.19.3")
jooqGenerator("org.jooq:jooq-meta:3.19.3")

    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("io.mockk:mockk-agent-jvm:1.13.11")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

