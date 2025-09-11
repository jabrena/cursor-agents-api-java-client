# Essential Maven Goals:

```bash
# Analyze dependencies
./mvnw dependency:tree
./mvnw dependency:analyze
./mvnw dependency:resolve

./mvnw clean validate -U
./mvnw buildplan:list-plugin
./mvnw buildplan:list-phase
./mvnw help:all-profiles
./mvnw help:active-profiles
./mvnw license:third-party-report

# Clean the project
./mvnw clean

# Clean and package in one command
./mvnw clean package

# Run integration tests
./mvnw clean verify

# Check for dependency updates
./mvnw versions:display-property-updates
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates

# Generate project reports
./mvnw site
./mvnw clean verify
./mvnw clean verify -pl openapi
./mvnw clean package -Ppromote

jwebserver -p 8005 -d "$(pwd)/openapi/target/swagger-ui/"
jwebserver -p 8005 -d "$(pwd)/docs"

./mvnw clean compile exec:java -Pexamples -pl java-client -Dexec.args="your_api_key_here"
```
