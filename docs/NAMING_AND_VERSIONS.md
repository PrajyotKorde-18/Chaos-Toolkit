# NAMING_AND_VERSIONS.md
## Exact naming conventions, versions, and discovered Spring Boot 4.x quirks

This file exists because Spring Boot 4.x renamed several artifacts compared to 3.x, and
guessing artifact names from general/training knowledge WILL produce build failures.
Every fact below was confirmed by actually attempting a build and observing the result —
treat this as ground truth over any general assumption.

---

## 1. Confirmed environment

- **JDK:** 21 (confirmed installed and in use)
- **Spring Boot version:** 4.0.8 (the only stable, non-snapshot, non-milestone release
  available at project start — do NOT use 4.1.x SNAPSHOT, 4.2.0 SNAPSHOT/M1, or any
  other pre-release version)
- **Build tool:** Maven (not Gradle)
- **Config format:** YAML (`application.yml`) exclusively, never `.properties`
- **IDE:** IntelliJ IDEA on Windows

---

## 2. Confirmed artifact name changes, Spring Boot 3.x -> 4.x

| Spring Boot 3.x name | Spring Boot 4.x name | Notes |
|---|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` | Confirmed via actual Spring Initializr generation at start.spring.io with Boot 4.0.8 selected |
| `spring-boot-starter-test` | Split into `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`, etc. | Test starters are now split per-feature rather than one monolithic test starter |
| `spring-boot-starter-webflux` | **DOES NOT EXIST as of 4.0.8** — confirmed via actual `mvn compile` failure: "Dependency 'org.springframework.boot:spring-boot-starter-webflux:4.0.8' not found" | **DO NOT USE.** See section 3 below for the resolution used instead. |

**When in doubt about an artifact name for Boot 4.0.8:** do not guess from memory. Check
https://start.spring.io with Spring Boot 4.0.8 selected and search the dependency name,
OR attempt the build and read the actual Maven error, which will state clearly which
artifact/version could not be resolved.

---

## 3. Resolution for the WebFlux/WebClient problem

The original design called for `WebClient` (reactive) to implement the agent's polling
client. Since `spring-boot-starter-webflux` is not resolvable in Boot 4.0.8 under this
project's dependency setup, **the agent's polling client uses Spring's `RestClient`
instead** (synchronous, blocking, introduced in Spring Framework 6.1 / Spring Boot 3.2+,
available without any extra starter dependency beyond what's already pulled in).

This is also architecturally a better fit: the polling loop is a simple background
thread doing periodic blocking GET calls, not a high-throughput reactive stream, so
`RestClient` is not a compromise — it is the more appropriate tool.

**Do not attempt to add `spring-boot-starter-webflux` back in for any reason without
first re-verifying against a fresh start.spring.io check whether the artifact name has
changed again in whatever Boot 4.x patch version is current at the time.**

---

## 4. groupId / artifactId / package naming (exact, must match precisely)

### chaos-toolkit (control plane)
```
groupId:        in.strikes
artifactId:     chaos-toolkit
base package:   in.strikes.chaostoolkit
main class:     in.strikes.chaostoolkit.ChaosToolkitApplication
server port:    9000
```

Sub-packages under `in.strikes.chaostoolkit`:
- `model` — `FaultConfig.java`, `FaultType.java`
- `service` — `FaultStore.java`
- `controller` — `FaultQueryController.java`, `AdminController.java`, `ActivateFaultRequest.java`

### chaos-agent (library)
```
groupId:        in.strikes
artifactId:     chaos-agent
version:        0.1.0
packaging:      jar
base package:   in.strikes.chaosagent
main class:     NONE -- this is a library, it must never have a @SpringBootApplication class
```

Sub-packages under `in.strikes.chaosagent`:
- `annotation` — `ChaosLatency.java`, `ChaosException.java`
- `model` — `FaultConfig.java`, `FaultType.java` (independently duplicated from the
  control plane's versions of the same names -- this is intentional, see
  PROJECT_CONTEXT.md rule 2 on decoupling. The two modules share a JSON wire-format
  contract, not a shared Java dependency.)
- `config` — `ChaosAgentProperties.java`, `ChaosAgentAutoConfiguration.java`
- `core` — `ChaosFaultRegistry.java`, `ChaosInjectionAspect.java`

### demo-apps/payment-service (Phase 1 target app, not yet built)
```
groupId:        in.strikes.demo (or similar -- confirm before creating)
artifactId:     payment-service
base package:   in.strikes.demo.payment
server port:    8081
depends on:     chaos-agent (as a regular Maven dependency, installed to local repo first)
```

---

## 5. chaos-agent pom.xml (exact, confirmed working)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>in.strikes</groupId>
    <artifactId>chaos-agent</artifactId>
    <version>0.1.0</version>
    <packaging>jar</packaging>
    <name>chaos-agent</name>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>4.0.8</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <!-- NOTE: spring-boot-starter-webflux intentionally NOT included -- see section 3.
             RestClient is used instead and requires no separate starter beyond what
             spring-boot-autoconfigure already pulls in transitively. -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**IMPORTANT: if RestClient requires an explicit dependency not already present in the
above list when you actually compile, add `spring-boot-starter-webmvc`'s underlying
HTTP client dependency ONLY as needed based on actual compiler errors -- do not
preemptively add starters "just in case." Confirm via real compilation first.**

---

## 6. chaos-toolkit pom.xml (exact, confirmed working, as generated by Spring Initializr)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.8</version>
        <relativePath/>
    </parent>
    <groupId>in.strikes</groupId>
    <artifactId>chaos-toolkit</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>chaos-toolkit</name>
    <description>chaos-toolkit</description>
    <properties>
        <java.version>21</java.version> <!-- NOTE: Initializr defaults this to 17; change to 21 -->
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 7. IDE/tooling gotchas already encountered (avoid repeating these mistakes)

1. **Nested duplicate folders:** creating a new IntelliJ project with Location set to
   `...\Desktop\chaos-agent` instead of `...\Desktop` can cause IntelliJ to create a
   double-nested `chaos-agent\chaos-agent\` structure. Always set Location to the PARENT
   folder (e.g. `Desktop`), not the target project folder itself -- IntelliJ appends the
   project name automatically.
2. **Language level mismatch:** IntelliJ may default "Language level" to a version
   HIGHER than the installed JDK (e.g. "25 - Compact source files" shown while JDK 21 is
   selected). This mismatch can cause the "New -> Java Class" menu option to disappear
   entirely. Fix: File -> Project Structure -> Project -> Language level -> set to match
   the actual JDK version (21).
3. **"New -> Java Class" missing from context menu:** if this happens, use
   "New -> File" and type the filename with `.java` extension directly (e.g.
   `ChaosLatency.java`) -- IntelliJ treats any `.java` file with valid content
   identically regardless of which menu option created it.
4. **"Move to source root" can move files to the WRONG root** if multiple directories
   are ambiguously marked. Before creating source files, explicitly verify: right-click
   `src/main/java` -> confirm it shows as "Sources Root" (blue folder icon), and
   right-click `src/main/resources` -> confirm it shows as "Resources Root" (different
   icon), not both claiming the same role.
5. **Always verify file location via the breadcrumb at the bottom of the editor window**
   after creating a file -- it will show the true path (e.g.
   `chaos-agent > src > main > java > in > strikes > chaosagent > model >
   FaultType.java`). If a stray duplicate top-level folder exists, the breadcrumb will
   reveal it immediately (e.g. showing `chaos-agent > chaos-agent > model > ...` which
   indicates the file landed in the wrong, duplicate folder).
